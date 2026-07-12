package com.adsmaker.app.domain

/**
 * Draft vs Final generation. Draft is a cheap test run — shorter and lower
 * resolution — so the user can iterate without paying full price each time.
 * Audio stays on for both. Final is the real deliverable (unchanged behaviour:
 * the platform's full duration at 720p).
 */
enum class GenerationMode(val label: String, val resolution: OutputResolution) {
    DRAFT("Draft", OutputResolution.P480),
    FINAL("Final", OutputResolution.P720);

    /** Effective clip length: Draft is fixed-short; Final follows the platform. */
    fun durationSeconds(platform: PlatformFormat): Int = when (this) {
        DRAFT -> DRAFT_DURATION_SECONDS
        FINAL -> platform.durationSeconds
    }

    companion object {
        const val DRAFT_DURATION_SECONDS = 5
        val DEFAULT = FINAL
    }
}
