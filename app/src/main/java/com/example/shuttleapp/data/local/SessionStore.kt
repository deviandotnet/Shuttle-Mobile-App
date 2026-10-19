package com.example.shuttleapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSession(
    val accountId: String,
    val username: String,
    val displayName: String,
    val token: String?,
    val expiresAtMillis: Long?,
    val tokenExpired: Boolean
)

/** Persists the signed-in session and token. Reads are synchronous so the OkHttp interceptor can use them. */
interface SessionStore {
    /** The stored session; first access loads it from disk, so call it off the main thread. */
    val state: StateFlow<StoredSession?>
    fun write(session: StoredSession)
    fun markTokenExpired()
    fun clear()
    fun token(): String? = state.value?.token
}

/**
 * [SessionStore] backed by EncryptedSharedPreferences (tokens and account ids are sensitive).
 * security-crypto is deprecated upstream; keeping it behind [SessionStore] lets it move to DataStore + Tink later.
 */
@Singleton
class EncryptedSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionStore {

    private val prefs: SharedPreferences by lazy { openPrefs() }
    private val _state: MutableStateFlow<StoredSession?> by lazy { MutableStateFlow(readPrefs()) }

    override val state: StateFlow<StoredSession?> get() = _state

    override fun write(session: StoredSession) {
        prefs.edit {
            putString(KEY_ACCOUNT_ID, session.accountId)
            putString(KEY_USERNAME, session.username)
            putString(KEY_DISPLAY_NAME, session.displayName)
            putString(KEY_TOKEN, session.token)
            if (session.expiresAtMillis != null) putLong(KEY_EXPIRES_AT, session.expiresAtMillis) else remove(KEY_EXPIRES_AT)
            putBoolean(KEY_TOKEN_EXPIRED, session.tokenExpired)
        }
        _state.value = session
    }

    override fun markTokenExpired() {
        if (_state.value?.token == null) return
        prefs.edit { putBoolean(KEY_TOKEN_EXPIRED, true) }
        _state.update { it?.copy(tokenExpired = true) }
    }

    override fun clear() {
        prefs.edit { clear() }
        _state.value = null
    }

    private fun readPrefs(): StoredSession? {
        val accountId = prefs.getString(KEY_ACCOUNT_ID, null) ?: return null
        return StoredSession(
            accountId = accountId,
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            displayName = prefs.getString(KEY_DISPLAY_NAME, "") ?: "",
            token = prefs.getString(KEY_TOKEN, null),
            expiresAtMillis = if (prefs.contains(KEY_EXPIRES_AT)) prefs.getLong(KEY_EXPIRES_AT, 0L) else null,
            tokenExpired = prefs.getBoolean(KEY_TOKEN_EXPIRED, false)
        )
    }

    private fun openPrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (ex: Exception) {
            // A lost or corrupted Keystore key makes the file unreadable; start clean (driver signs in once more).
            Log.e("SessionStore", "Resetting unreadable session store", ex)
            context.deleteSharedPreferences(FILE_NAME)
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        // Excluded from backup in res/xml/backup_rules.xml and data_extraction_rules.xml.
        const val FILE_NAME = "session_prefs"
        const val KEY_ACCOUNT_ID = "account_id"
        const val KEY_USERNAME = "username"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_TOKEN_EXPIRED = "token_expired"
    }
}
