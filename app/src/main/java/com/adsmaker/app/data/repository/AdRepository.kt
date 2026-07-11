package com.adsmaker.app.data.repository

import android.content.Context
import com.adsmaker.app.core.AppResult
import com.adsmaker.app.data.session.SessionProvider
import com.adsmaker.app.data.usage.GenerationAttempt
import com.adsmaker.app.data.usage.UsageLogger
import com.adsmaker.app.data.video.VideoDownloader
import com.adsmaker.app.data.video.VideoGenerationResult
import com.adsmaker.app.data.video.VideoGenerator
import com.adsmaker.app.data.video.VideoProgress
import com.adsmaker.app.data.video.VideoRequest
import com.adsmaker.app.domain.AdInputs
import com.adsmaker.app.domain.AdPromptBuilder
import com.adsmaker.app.domain.CostEstimator
import com.adsmaker.app.domain.GeneratedAd
import com.adsmaker.app.util.MediaUtils

/**
 * Orchestrates one end-to-end ad generation:
 *   read notes → build prompt → encode image → call the generator → download →
 *   log the attempt (success *or* failure) for cost tracking.
 *
 * Depends only on the [VideoGenerator] abstraction, so the provider is swappable.
 * The [clock] is injectable to keep this unit-testable.
 */
class AdRepository(
    private val appContext: Context,
    private val generator: VideoGenerator,
    private val downloader: VideoDownloader,
    private val usageLogger: UsageLogger,
    private val sessionProvider: SessionProvider,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun generateAd(
        inputs: AdInputs,
        onProgress: (VideoProgress) -> Unit = {},
    ): AppResult<GeneratedAd> {
        val platform = inputs.platform
        val estimate = CostEstimator.estimate(platform)
        val user = sessionProvider.current()

        // Build the prompt from the user's notes + proven ad patterns.
        val prompt = AdPromptBuilder.build(
            productNotes = inputs.productNotes,
            platform = platform,
            hasReferenceImage = inputs.hasImage,
        )

        // Encode the reference image (if any) into a data URI for image-to-video.
        onProgress(VideoProgress(VideoProgress.Stage.UPLOADING))
        val imageDataUri = inputs.imageUri?.let { uri ->
            MediaUtils.imageToDataUri(appContext, uri)
        }

        val request = VideoRequest(
            prompt = prompt,
            imageDataUri = imageDataUri,
            durationSeconds = platform.durationSeconds,
            aspectRatio = platform.aspectRatio,
        )

        val result = generator.generate(request, onProgress)

        return when (result) {
            is VideoGenerationResult.Error -> {
                // A failed attempt can still cost API time — log it for accounting.
                logAttempt(user.userId, platform, estimate, GenerationAttempt.Outcome.FAILURE)
                AppResult.Failure(result.message, result.cause)
            }

            is VideoGenerationResult.Success -> {
                try {
                    val fileName = "adsmaker_${platform.name.lowercase()}_${clock()}.mp4"
                    val file = downloader.download(appContext, result.videoUrl, fileName)
                    logAttempt(user.userId, platform, estimate, GenerationAttempt.Outcome.SUCCESS)
                    AppResult.Success(
                        GeneratedAd(
                            file = file,
                            platform = platform,
                            durationSeconds = platform.durationSeconds,
                            estimatedCostUsd = estimate.usd,
                            seed = result.seed,
                        ),
                    )
                } catch (e: Exception) {
                    // The generation was billed even though the download failed.
                    logAttempt(user.userId, platform, estimate, GenerationAttempt.Outcome.SUCCESS)
                    AppResult.Failure("Generated the video but couldn't download it. Please retry.", e)
                }
            }
        }
    }

    private fun logAttempt(
        userId: String,
        platform: com.adsmaker.app.domain.PlatformFormat,
        estimate: CostEstimator.Estimate,
        outcome: GenerationAttempt.Outcome,
    ) {
        usageLogger.record(
            GenerationAttempt(
                userId = userId,
                platform = platform,
                durationSeconds = estimate.seconds,
                estimatedCostUsd = estimate.usd,
                timestampMillis = clock(),
                outcome = outcome,
            ),
        )
    }
}
