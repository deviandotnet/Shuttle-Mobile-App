package com.example.shuttleapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Opens EncryptedSharedPreferences files, each with its own Keystore master key so that resetting one
 * (after Keystore corruption) never makes another file unreadable.
 * security-crypto is deprecated upstream; this is the single place to swap it for DataStore + Tink later.
 */
object EncryptedPrefs {

    private const val TAG = "EncryptedPrefs"

    /**
     * Returns the file, recovering from an unreadable file or a broken master key by resetting them,
     * or null if encrypted storage is unusable on this device (callers must cope without persistence).
     */
    fun open(context: Context, fileName: String): SharedPreferences? {
        val keyAlias = "${fileName}_master_key"
        return try {
            create(context, fileName, keyAlias)
        } catch (first: Exception) {
            Log.e(TAG, "Resetting unreadable $fileName", first)
            context.deleteSharedPreferences(fileName)
            try {
                create(context, fileName, keyAlias)
            } catch (second: Exception) {
                Log.e(TAG, "Resetting master key for $fileName", second)
                deleteKey(keyAlias)
                context.deleteSharedPreferences(fileName)
                try {
                    create(context, fileName, keyAlias)
                } catch (third: Exception) {
                    Log.e(TAG, "Encrypted storage unavailable for $fileName", third)
                    null
                }
            }
        }
    }

    private fun create(context: Context, fileName: String, keyAlias: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context, keyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteKey(alias: String) {
        try {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(alias)
        } catch (ex: Exception) {
            Log.e(TAG, "Could not delete Keystore entry $alias", ex)
        }
    }
}
