package com.adsmaker.app.data.settings

import android.content.Context
import com.adsmaker.app.domain.GenerationMode

/**
 * Small non-secret preference store: the last-used generation mode, the
 * owner-mode flag (clean, unwatermarked videos for Luke), and the count of
 * free watermarked generations used on this device.
 *
 * The free counter is per-install for now; it moves server-side when real
 * accounts/payments arrive.
 */
interface AppPreferences {
    fun getLastMode(): GenerationMode
    fun setLastMode(mode: GenerationMode)

    /** Owner mode: generations come out clean (no watermark) and uncapped. */
    fun isOwnerMode(): Boolean
    fun setOwnerMode(enabled: Boolean)

    /** How many free (watermarked) videos this install has already produced. */
    fun getFreeGenerationsUsed(): Int
    fun incrementFreeGenerationsUsed()
}

class SharedPrefsAppPreferences(context: Context) : AppPreferences {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getLastMode(): GenerationMode {
        val name = prefs.getString(KEY_LAST_MODE, null) ?: return GenerationMode.DEFAULT
        return runCatching { GenerationMode.valueOf(name) }.getOrDefault(GenerationMode.DEFAULT)
    }

    override fun setLastMode(mode: GenerationMode) {
        prefs.edit().putString(KEY_LAST_MODE, mode.name).apply()
    }

    override fun isOwnerMode(): Boolean = prefs.getBoolean(KEY_OWNER_MODE, false)

    override fun setOwnerMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OWNER_MODE, enabled).apply()
    }

    override fun getFreeGenerationsUsed(): Int = prefs.getInt(KEY_FREE_USED, 0)

    override fun incrementFreeGenerationsUsed() {
        prefs.edit().putInt(KEY_FREE_USED, getFreeGenerationsUsed() + 1).apply()
    }

    private companion object {
        const val PREFS_NAME = "adsmaker_prefs"
        const val KEY_LAST_MODE = "last_generation_mode"
        const val KEY_OWNER_MODE = "owner_mode"
        const val KEY_FREE_USED = "free_generations_used"
    }
}
