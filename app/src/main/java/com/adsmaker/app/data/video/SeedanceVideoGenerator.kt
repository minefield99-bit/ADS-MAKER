package com.adsmaker.app.data.video

import com.adsmaker.app.data.remote.FalApiService
import com.adsmaker.app.data.remote.FalConfig
import com.adsmaker.app.data.remote.SeedanceRequest
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

/**
 * Seedance 2.0 Fast implementation of [VideoGenerator], backed by the fal.ai
 * queue API. Chooses image-to-video when a reference image is present, otherwise
 * text-to-video. All fal-specific detail is contained here.
 */
class SeedanceVideoGenerator(
    private val api: FalApiService,
) : VideoGenerator {

    override suspend fun generate(
        request: VideoRequest,
        onProgress: (VideoProgress) -> Unit,
    ): VideoGenerationResult {
        return try {
            val useImage = !request.imageDataUri.isNullOrBlank()
            val modelPath = if (useImage) {
                FalConfig.MODEL_IMAGE_TO_VIDEO
            } else {
                FalConfig.MODEL_TEXT_TO_VIDEO
            }

            onProgress(VideoProgress(VideoProgress.Stage.QUEUED))

            val body = SeedanceRequest(
                prompt = request.prompt,
                imageUrl = request.imageDataUri.takeIf { useImage },
                duration = request.durationSeconds.coerceIn(4, 15).toString(),
                aspectRatio = request.aspectRatio,
                resolution = request.resolution,
                generateAudio = request.generateAudio,
            )

            val submit = api.submit(modelPath, body)
            val statusUrl = submit.statusUrl
                ?: "${FalConfig.BASE_URL}$modelPath/requests/${submit.requestId}/status"
            val responseUrl = submit.responseUrl
                ?: "${FalConfig.BASE_URL}$modelPath/requests/${submit.requestId}"

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
        401, 403 -> "Your API key was rejected. Check the fal.ai key in local.properties."
        402 -> "Your fal.ai account is out of credit. Add funds to keep generating."
        422 -> "The request was rejected by the video service. Try different inputs."
        429 -> "Too many requests right now. Wait a moment and try again."
        in 500..599 -> "The video service is having trouble. Please try again shortly."
        else -> "The video service returned an error (code $code). Please try again."
    }
}
