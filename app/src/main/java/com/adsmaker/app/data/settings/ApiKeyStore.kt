package com.adsmaker.app.data.settings

import android.content.Context

/**
 * On-device storage for a user-entered fal.ai API key. Lets someone run the
 * CI-built APK on a phone (no Android Studio) by pasting their key in the app.
 */
interface ApiKeyStore {
    /** Decrypted user key, or null if none saved. */
    fun getUserKey(): String?
    fun saveUserKey(key: String)
    fun clearUserKey()
    fun hasUserKey(): Boolean
}

/**
 * [ApiKeyStore] that persists only the *encrypted* key (via [KeystoreCrypto]) in
 * private SharedPreferences. The key is never written in plaintext and never logged.
 */
class SecureApiKeyStore(context: Context) : ApiKeyStore {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getUserKey(): String? {
        val encrypted = prefs.getString(PREF_KEY, null) ?: return null
        return KeystoreCrypto.decrypt(encrypted)?.takeIf { it.isNotBlank() }
    }

    override fun saveUserKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            clearUserKey()
            return
        }
        prefs.edit().putString(PREF_KEY, KeystoreCrypto.encrypt(trimmed)).apply()
    }

    override fun clearUserKey() {
        prefs.edit().remove(PREF_KEY).apply()
    }

    override fun hasUserKey(): Boolean = getUserKey() != null

    private companion object {
        const val PREFS_NAME = "adsmaker_secure_prefs"
        const val PREF_KEY = "fal_api_key_enc"
    }
}
