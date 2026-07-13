package com.adsmaker.app.domain

/**
 * Per-platform output rules. Week 1 ships TikTok only; Instagram and YouTube
 * are defined here (but not selectable in the UI yet) so adding them later is
 * a UI change, not an architecture change.
 *
 * Durations are capped by the active provider: Veo 3.1 Fast generates single
 * clips of at most 8 seconds. Longer formats (e.g. a ~60s YouTube ad) would
 * need multi-clip stitching — a Week 2+ project, noted in the spec.
 */
enum class PlatformFormat(
    val displayName: String,
    /** Duration in seconds of one generated clip (provider max: 8s on Veo). */
    val durationSeconds: Int,
    /** Vertical/square/landscape ratio string in the model's vocabulary. */
    val aspectRatio: String,
    /** Short creative-direction note folded into the generation prompt. */
    val pacingNote: String,
    val enabledInWeek1: Boolean,
) {
    TIKTOK(
        displayName = "TikTok",
        durationSeconds = 8,
        aspectRatio = "9:16",
        pacingNote = "fast-paced, casual, punchy energy with an immediate hook",
        enabledInWeek1 = true,
    ),
    INSTAGRAM_REELS(
        displayName = "Instagram Reels",
        durationSeconds = 8,
        aspectRatio = "9:16",
        pacingNote = "fast, visually punchy, trendy and polished",
        enabledInWeek1 = false,
    ),
    YOUTUBE(
        // Target format is ~60s; a single Veo clip maxes at 8s, so the full
        // YouTube length needs clip stitching (Week 2+).
        displayName = "YouTube",
        durationSeconds = 8,
        aspectRatio = "16:9",
        pacingNote = "slower, professional and informative tone",
        enabledInWeek1 = false,
    );

    companion object {
        /** The single format Week 1 targets. */
        val WEEK1_DEFAULT = TIKTOK
    }
}
