package com.adsmaker.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for the fal.ai queue API. The queue envelope (submit/status) and
 * the result shape ({ video: { url } }) are shared across models; each model
 * has its own request body.
 */

/**
 * Request body for Veo 3.1 Fast (ACTIVE provider). Verified schema (2026-07):
 *   POST https://queue.fal.run/fal-ai/veo3.1/fast/image-to-video
 *   duration: "4s" | "6s" | "8s" (max clip is 8s)
 *   aspect_ratio: "auto" | "16:9" | "9:16"
 *   resolution: "720p" | "1080p"  ($0.15/sec with audio on both)
 */
@Serializable
data class VeoRequest(
    val prompt: String,
    // Omitted for text-to-video; a data URI or remote URL for image-to-video.
    @SerialName("image_url") val imageUrl: String? = null,
    // Veo expects a suffixed string: "4s" | "6s" | "8s".
    val duration: String,
    @SerialName("aspect_ratio") val aspectRatio: String,
    val resolution: String,
    @SerialName("generate_audio") val generateAudio: Boolean,
)

/**
 * Request body for Seedance 2.0 Fast (DORMANT provider — see FalConfig).
 *   duration: "auto" or "4".."15" (unsuffixed), resolution: "480p" | "720p".
 */
@Serializable
data class SeedanceRequest(
    val prompt: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val duration: String,
    @SerialName("aspect_ratio") val aspectRatio: String,
    val resolution: String,
    @SerialName("generate_audio") val generateAudio: Boolean,
)

/** Response from submitting to the queue (same envelope for all models). */
@Serializable
data class QueueSubmitResponse(
    @SerialName("request_id") val requestId: String,
    @SerialName("status_url") val statusUrl: String? = null,
    @SerialName("response_url") val responseUrl: String? = null,
    @SerialName("cancel_url") val cancelUrl: String? = null,
)

/** Response from polling the status URL. */
@Serializable
data class QueueStatusResponse(
    // IN_QUEUE | IN_PROGRESS | COMPLETED
    val status: String,
    @SerialName("queue_position") val queuePosition: Int? = null,
    @SerialName("response_url") val responseUrl: String? = null,
) {
    val isCompleted: Boolean get() = status.equals("COMPLETED", ignoreCase = true)
    val isInProgress: Boolean get() = status.equals("IN_PROGRESS", ignoreCase = true)
}

/** Final generation result — shared shape across fal video models. */
@Serializable
data class FalVideoResult(
    val video: FalFile? = null,
    val seed: Long? = null,
)

@Serializable
data class FalFile(
    val url: String,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
)
