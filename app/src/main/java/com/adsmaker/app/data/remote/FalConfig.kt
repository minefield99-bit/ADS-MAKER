package com.adsmaker.app.data.remote

/**
 * fal.ai endpoint configuration. Model slugs are isolated here so a provider or
 * model change is a one-file edit.
 */
object FalConfig {
    const val BASE_URL = "https://queue.fal.run/"

    // Seedance 2.0 Fast — cheapest tier that still looks good (~$0.09/sec).
    const val MODEL_IMAGE_TO_VIDEO = "bytedance/seedance-2.0-fast/image-to-video"
    const val MODEL_TEXT_TO_VIDEO = "bytedance/seedance-2.0-fast/text-to-video"

    /** How often to poll the queue while a job runs. */
    const val POLL_INTERVAL_MS = 3_000L

    /** Give up after this long so a stuck job doesn't hang the UI forever. */
    const val MAX_POLL_DURATION_MS = 10 * 60 * 1_000L
}
