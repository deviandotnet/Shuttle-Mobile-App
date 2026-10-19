package com.example.shuttleapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase as PlainDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Upgrading from an unencrypted build must keep every row and Room's schema version. */
@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbFile get() = context.getDatabasePath(DatabaseEncryption.DATABASE_NAME)

    @Before
    fun setUp() = clean()

    @After
    fun tearDown() = clean()

    private fun clean() {
        context.deleteDatabase(DatabaseEncryption.DATABASE_NAME)
        context.deleteSharedPreferences("db_key_prefs")
    }

    @Test
    fun plaintextDatabaseIsEncryptedWithDataAndVersionKept() {
        dbFile.parentFile?.mkdirs()
        PlainDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL("CREATE TABLE shuttle_pass (id TEXT PRIMARY KEY, isSynced INTEGER)")
            db.execSQL("INSERT INTO shuttle_pass VALUES ('unsynced-1', 0)")
            db.version = 26
        }
        assertTrue(DatabaseEncryption.isPlaintext(dbFile))

        val passphrase = DatabaseEncryption.prepare(context)

        assertNotNull(passphrase)
        assertFalse(DatabaseEncryption.isPlaintext(dbFile))
        SQLiteDatabase.openDatabase(dbFile.path, passphrase, null, SQLiteDatabase.OPEN_READONLY, null, null).use { db ->
            assertEquals(26, db.version)
            db.rawQuery("SELECT id FROM shuttle_pass WHERE isSynced = 0", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("unsynced-1", cursor.getString(0))
            }
        }
    }

    @Test
    fun passphraseIsStableAcrossLaunches() {
        val first = DatabaseEncryption.prepare(context)
        val second = DatabaseEncryption.prepare(context)
        assertTrue(first!!.contentEquals(second))
    }
}
