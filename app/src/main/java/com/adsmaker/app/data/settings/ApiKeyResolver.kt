package com.adsmaker.app.data.settings

/**
 * Pure precedence + masking logic for the fal.ai API key. Kept Android-free so
 * it's unit-testable. A key the user pastes in the app always wins over the
 * build-time [com.adsmaker.app.BuildConfig.FAL_API_KEY] (which stays as a dev
 * fallback).
 */
object ApiKeyResolver {

    /** User-entered key if present, else the build-time fallback. */
    fun resolve(userKey: String?, buildConfigKey: String): String {
        val trimmed = userKey?.trim().orEmpty()
        return if (trimmed.isNotEmpty()) trimmed else buildConfigKey.trim()
    }

    /** Masked form for display after saving — never shows the full secret. */
    fun mask(key: String): String {
        val k = key.trim()
        if (k.isEmpty()) return ""
        val last4 = if (k.length <= 4) k else k.takeLast(4)
        return "••••••••$last4"
    }
}
