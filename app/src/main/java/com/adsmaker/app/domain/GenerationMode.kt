package com.adsmaker.app.domain

/**
 * Draft vs Final generation. Draft is a cheap test run — the shortest clip the
 * provider allows — so the user can iterate without paying full price each
 * time. Audio stays on for both. Final is the real deliverable.
 *
 * On Veo 3.1 Fast the cost lever is duration only ($0.15/sec with audio at
 * either resolution), so Draft = 4s and Final = 8s, both 720p.
 */
enum class GenerationMode(val label: String, val resolution: OutputResolution) {
    DRAFT("Draft", OutputResolution.P720),
    FINAL("Final", OutputResolution.P720);

    /** Effective clip length: Draft is fixed-short; Final follows the platform. */
    fun durationSeconds(platform: PlatformFormat): Int = when (this) {
        DRAFT -> DRAFT_DURATION_SECONDS
        FINAL -> platform.durationSeconds
    }

    companion object {
        /** Veo's shortest allowed clip. */
        const val DRAFT_DURATION_SECONDS = 4
        val DEFAULT = FINAL
    }
}
