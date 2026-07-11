package com.adsmaker.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for the fal.ai queue API hosting Seedance 2.0 Fast.
 * Verified request/response shape (fal.ai Seedance 2.0 docs):
 *   POST https://queue.fal.run/bytedance/seedance-2.0-fast/image-to-video
 *   Authorization: Key <FAL_KEY>
 *   Body: { prompt, image_url, resolution, duration, aspect_ratio, generate_audio }
 *   Result: { video: { url }, seed }
 */

@Serializable
data class SeedanceRequest(
    val prompt: String,
    // Optional for text-to-video; a data URI or remote URL for image-to-video.
    @SerialName("image_url") val imageUrl: String? = null,
    // fal expects duration as a string: "auto" or "4".."15".
    val duration: String,
    @SerialName("aspect_ratio") val aspectRatio: String,
    val resolution: String,
    @SerialName("generate_audio") val generateAudio: Boolean,
)

/** Response from submitting to the queue. */
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

/** Final generation result. */
@Serializable
data class SeedanceResult(
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
