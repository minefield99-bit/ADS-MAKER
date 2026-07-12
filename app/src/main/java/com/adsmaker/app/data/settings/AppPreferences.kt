package com.adsmaker.app.data.settings

import android.content.Context
import com.adsmaker.app.domain.GenerationMode

/**
 * Small non-secret preference store. Week 1 only remembers the last-used
 * generation mode (Draft vs Final) so the toggle survives app restarts.
 */
interface AppPreferences {
    fun getLastMode(): GenerationMode
    fun setLastMode(mode: GenerationMode)
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

    private companion object {
        const val PREFS_NAME = "adsmaker_prefs"
        const val KEY_LAST_MODE = "last_generation_mode"
    }
}
