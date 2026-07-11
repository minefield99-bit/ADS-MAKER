package com.adsmaker.app.domain

/**
 * Per-platform output rules. Week 1 ships TikTok only; Instagram and YouTube
 * are defined here (but not selectable in the UI yet) so adding them later is
 * a UI change, not an architecture change.
 */
enum class PlatformFormat(
    val displayName: String,
    /** Duration in seconds sent to the video model. */
    val durationSeconds: Int,
    /** Vertical/square/landscape ratio string in the model's vocabulary. */
    val aspectRatio: String,
    /** Short creative-direction note folded into the generation prompt. */
    val pacingNote: String,
    val enabledInWeek1: Boolean,
) {
    TIKTOK(
        displayName = "TikTok",
        durationSeconds = 15,
        aspectRatio = "9:16",
        pacingNote = "fast-paced, casual, punchy energy with an immediate hook",
        enabledInWeek1 = true,
    ),
    INSTAGRAM_REELS(
        displayName = "Instagram Reels",
        durationSeconds = 18,
        aspectRatio = "9:16",
        pacingNote = "fast, visually punchy, trendy and polished",
        enabledInWeek1 = false,
    ),
    YOUTUBE(
        displayName = "YouTube",
        durationSeconds = 60,
        aspectRatio = "16:9",
        pacingNote = "slower, professional and informative tone",
        enabledInWeek1 = false,
    );

    companion object {
        /** The single format Week 1 targets. */
        val WEEK1_DEFAULT = TIKTOK
    }
}
