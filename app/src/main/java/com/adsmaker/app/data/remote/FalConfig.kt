package com.adsmaker.app.data.remote

/**
 * fal.ai endpoint configuration. Model slugs are isolated here so a provider or
 * model change is a one-file edit.
 *
 * ACTIVE provider: Google Veo 3.1 Fast — open to all fal.ai accounts, native
 * audio, $0.15/sec with audio at 720p/1080p, clips of 4/6/8 seconds.
 *
 * DORMANT provider: Seedance 2.0 Fast — parked 2026-07 because fal.ai gates it
 * behind an early-access wall with business-only terms. The implementation
 * (SeedanceVideoGenerator) stays in the tree behind the VideoGenerator
 * interface; reactivating it is a one-line change in ServiceLocator.
 */
object FalConfig {
    const val BASE_URL = "https://queue.fal.run/"

    // --- Veo 3.1 Fast (ACTIVE) ---
    const val VEO_IMAGE_TO_VIDEO = "fal-ai/veo3.1/fast/image-to-video"
    const val VEO_TEXT_TO_VIDEO = "fal-ai/veo3.1/fast"

    /**
     * Queue status/result URLs for nested fal endpoints hang off the BASE app id
     * (fal-ai/veo3.1), not the full subpath. Used only as a fallback when the
     * submit response omits its status_url/response_url.
     */
    const val VEO_QUEUE_BASE_ID = "fal-ai/veo3.1"

    // --- Seedance 2.0 Fast (DORMANT, early-access gated on fal.ai) ---
    const val SEEDANCE_IMAGE_TO_VIDEO = "bytedance/seedance-2.0-fast/image-to-video"
    const val SEEDANCE_TEXT_TO_VIDEO = "bytedance/seedance-2.0-fast/text-to-video"

    /** How often to poll the queue while a job runs. */
    const val POLL_INTERVAL_MS = 3_000L

    /** Give up after this long so a stuck job doesn't hang the UI forever. */
    const val MAX_POLL_DURATION_MS = 10 * 60 * 1_000L
}
