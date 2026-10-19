package com.example.shuttleapp.data.local

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.security.SecureRandom

/**
 * Keeps shuttle.db encrypted with SQLCipher (driver PII and the offline pass history live there).
 *
 * The passphrase is random per install and stored in its own EncryptedSharedPreferences file (Keystore-backed).
 * A database created by an older, unencrypted build is converted in place with `sqlcipher_export`, keeping
 * every row (including unsynced passes) and Room's schema version, so no destructive migration runs.
 */
object DatabaseEncryption {

    const val DATABASE_NAME = "shuttle.db"

    private const val TAG = "DatabaseEncryption"
    // Excluded from backup in res/xml/backup_rules.xml and data_extraction_rules.xml.
    private const val KEY_FILE = "db_key_prefs"
    private const val KEY_PASSPHRASE = "passphrase"
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    /**
     * Returns the passphrase Room must open the database with, after making sure the file on disk is
     * encrypted with it. Returns null only if encrypted key storage is unusable on this device; the
     * database then stays unencrypted rather than becoming unreadable after every restart.
     * Does disk I/O: call it off the main thread when possible.
     */
    fun prepare(context: Context): ByteArray? {
        System.loadLibrary("sqlcipher")
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val prefs = EncryptedPrefs.open(context, KEY_FILE)

        if (prefs == null) {
            Log.e(TAG, "Key storage unavailable; database stays unencrypted on this device")
            if (dbFile.exists() && !isPlaintext(dbFile)) quarantine(dbFile)
            return null
        }

        val stored = prefs.getString(KEY_PASSPHRASE, null)
        val passphrase = stored ?: newPassphrase().also { prefs.edit(commit = true) { putString(KEY_PASSPHRASE, it) } }

        when {
            !dbFile.exists() -> Unit
            isPlaintext(dbFile) -> encryptInPlace(dbFile, passphrase)
            // Encrypted with a key this install no longer has (e.g. restored from backup): it can't be read.
            stored == null -> quarantine(dbFile)
        }
        return passphrase.toByteArray(Charsets.UTF_8)
    }

    internal fun isPlaintext(file: File): Boolean {
        if (file.length() < SQLITE_HEADER.size) return false
        val header = ByteArray(SQLITE_HEADER.size)
        file.inputStream().use { it.read(header) }
        return header.contentEquals(SQLITE_HEADER)
    }

    private fun encryptInPlace(dbFile: File, passphrase: String) {
        val encrypted = File(dbFile.parentFile, "$DATABASE_NAME.encrypting")
        encrypted.delete()

        // An empty password opens a plaintext database; reading through it also picks up any pending WAL pages.
        // CREATE_IF_NECESSARY is required for ATTACH to create the encrypted copy (attached files inherit the flags).
        val plain = SQLiteDatabase.openDatabase(
            dbFile.path, "", null, SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY, null
        )
        try {
            val version = plain.version
            plain.execSQL("ATTACH DATABASE ${quote(encrypted.path)} AS encrypted KEY ${quote(passphrase)}")
            plain.rawQuery("SELECT sqlcipher_export('encrypted')", null).use { it.moveToFirst() }
            // sqlcipher_export does not copy user_version; Room needs it to skip the destructive migration.
            plain.execSQL("PRAGMA encrypted.user_version = $version")
            plain.execSQL("DETACH DATABASE encrypted")
        } finally {
            plain.close()
        }

        sidecars(dbFile).forEach { it.delete() }
        check(dbFile.delete() && encrypted.renameTo(dbFile)) { "Could not replace $DATABASE_NAME with its encrypted copy" }
        Log.i(TAG, "Encrypted existing $DATABASE_NAME")
    }

    private fun quarantine(dbFile: File) {
        val suffix = ".unreadable-${System.currentTimeMillis()}"
        (sidecars(dbFile) + dbFile).forEach { it.renameTo(File(it.path + suffix)) }
        Log.e(TAG, "$DATABASE_NAME was encrypted with a lost key; moved aside and starting a new database")
    }

    private fun sidecars(dbFile: File): List<File> =
        listOf("-wal", "-shm", "-journal").map { File(dbFile.path + it) }.filter { it.exists() }

    private fun newPassphrase(): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun quote(value: String): String = "'" + value.replace("'", "''") + "'"
}
