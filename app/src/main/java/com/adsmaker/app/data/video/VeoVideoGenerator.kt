package com.adsmaker.app.data.video

import com.adsmaker.app.data.remote.FalApiService
import com.adsmaker.app.data.remote.FalConfig
import com.adsmaker.app.data.remote.VeoRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

/**
 * Google Veo 3.1 Fast implementation of [VideoGenerator], backed by the fal.ai
 * queue API — the ACTIVE provider. Chooses image-to-video when a reference
 * image is present, otherwise text-to-video. All Veo-specific detail lives here.
 *
 * Veo constraints (verified 2026-07): clips are 4, 6 or 8 seconds; resolutions
 * 720p/1080p; aspect ratios auto/16:9/9:16; native audio. $0.15/sec with audio.
 */
class VeoVideoGenerator(
    private val api: FalApiService,
) : VideoGenerator {

    override suspend fun generate(
        request: VideoRequest,
        onProgress: (VideoProgress) -> Unit,
    ): VideoGenerationResult {
        return try {
            val useImage = !request.imageDataUri.isNullOrBlank()
            val modelPath = if (useImage) {
                FalConfig.VEO_IMAGE_TO_VIDEO
            } else {
                FalConfig.VEO_TEXT_TO_VIDEO
            }

            onProgress(VideoProgress(VideoProgress.Stage.QUEUED))

            val body = VeoRequest(
                prompt = request.prompt,
                imageUrl = request.imageDataUri.takeIf { useImage },
                duration = toVeoDuration(request.durationSeconds),
                aspectRatio = request.aspectRatio,
                resolution = request.resolution,
                generateAudio = request.generateAudio,
            )

            val submit = api.submitVeo(modelPath, body)
            // Nested fal endpoints report queue URLs under the base app id
            // (fal-ai/veo3.1), so prefer the URLs fal returns; the constructed
            // fallback uses the base id, not the full subpath.
            val statusUrl = submit.statusUrl
                ?: "${FalConfig.BASE_URL}${FalConfig.VEO_QUEUE_BASE_ID}/requests/${submit.requestId}/status"
            val responseUrl = submit.responseUrl
                ?: "${FalConfig.BASE_URL}${FalConfig.VEO_QUEUE_BASE_ID}/requests/${submit.requestId}"

            pollUntilDone(statusUrl, onProgress)

            onProgress(VideoProgress(VideoProgress.Stage.DOWNLOADING))
            val result = api.result(responseUrl)
            val url = result.video?.url
                ?: return VideoGenerationResult.Error(
                    "The generator finished but returned no video. Please try again.",
                )
            VideoGenerationResult.Success(videoUrl = url, seed = result.seed)
        } catch (e: TimeoutCancellationException) {
            VideoGenerationResult.Error(
                "Generation is taking longer than expected. Please try again in a moment.", e,
            )
        } catch (e: CancellationException) {
            // Real coroutine cancellation must propagate, never become a
            // "failed attempt" (which would also mis-log cost accounting).
            throw e
        } catch (e: HttpException) {
            VideoGenerationResult.Error(mapHttpError(e.code()), e)
        } catch (e: IOException) {
            VideoGenerationResult.Error(
                "Couldn't reach the video service. Check your connection and try again.", e,
            )
        } catch (e: Exception) {
            VideoGenerationResult.Error("Something went wrong while generating the video.", e)
        }
    }

    private suspend fun pollUntilDone(
        statusUrl: String,
        onProgress: (VideoProgress) -> Unit,
    ) {
        withTimeout(FalConfig.MAX_POLL_DURATION_MS) {
            while (true) {
                val status = api.status(statusUrl)
                when {
                    status.isCompleted -> return@withTimeout
                    status.isInProgress -> onProgress(
                        VideoProgress(VideoProgress.Stage.GENERATING),
                    )
                    else -> onProgress(
                        VideoProgress(
                            VideoProgress.Stage.QUEUED,
                            queuePosition = status.queuePosition,
                        ),
                    )
                }
                delay(FalConfig.POLL_INTERVAL_MS)
            }
        }
    }

    private fun mapHttpError(code: Int): String = when (code) {
        401, 403 -> "Your API key was rejected. Check the fal.ai key in Settings."
        402 -> "Your fal.ai account is out of credit. Add funds to keep generating."
        422 -> "The request was rejected by the video service. Try different inputs."
        429 -> "Too many requests right now. Wait a moment and try again."
        in 500..599 -> "The video service is having trouble. Please try again shortly."
        else -> "The video service returned an error (code $code). Please try again."
    }

    companion object {
        /** Veo only accepts 4, 6 or 8 second clips — snap to the nearest tier. */
        fun toVeoDuration(seconds: Int): String = when {
            seconds <= 4 -> "4s"
            seconds <= 6 -> "6s"
            else -> "8s"
        }
    }
}
