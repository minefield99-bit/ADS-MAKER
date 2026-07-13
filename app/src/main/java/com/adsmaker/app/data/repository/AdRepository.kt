package com.adsmaker.app.data.repository

import android.content.Context
import com.adsmaker.app.core.AppResult
import com.adsmaker.app.data.session.SessionProvider
import com.adsmaker.app.data.settings.AppPreferences
import com.adsmaker.app.data.usage.GenerationAttempt
import com.adsmaker.app.data.usage.UsageLogger
import com.adsmaker.app.data.video.VideoDownloader
import com.adsmaker.app.data.video.VideoGenerationResult
import com.adsmaker.app.data.video.VideoGenerator
import com.adsmaker.app.data.video.VideoProgress
import com.adsmaker.app.data.video.VideoRequest
import com.adsmaker.app.data.video.VideoWatermarker
import com.adsmaker.app.domain.AdInputs
import com.adsmaker.app.domain.AdPromptBuilder
import com.adsmaker.app.domain.CostEstimator
import com.adsmaker.app.domain.FreemiumPolicy
import com.adsmaker.app.domain.GeneratedAd
import com.adsmaker.app.util.MediaUtils
import java.io.File

/**
 * Orchestrates one end-to-end ad generation:
 *   freemium check → build prompt → encode image → call the generator →
 *   download → watermark (free tier only) → log the attempt for cost tracking.
 *
 * Depends only on the [VideoGenerator] abstraction, so the provider is
 * swappable. The [clock] is injectable to keep this unit-testable.
 */
class AdRepository(
    private val appContext: Context,
    private val generator: VideoGenerator,
    private val downloader: VideoDownloader,
    private val watermarker: VideoWatermarker,
    private val usageLogger: UsageLogger,
    private val sessionProvider: SessionProvider,
    private val preferences: AppPreferences,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun generateAd(
        inputs: AdInputs,
        onProgress: (VideoProgress) -> Unit = {},
    ): AppResult<GeneratedAd> {
        // Freemium gate FIRST — a blocked attempt must not reach the paid API.
        val decision = FreemiumPolicy.evaluate(
            ownerMode = preferences.isOwnerMode(),
            freeGenerationsUsed = preferences.getFreeGenerationsUsed(),
        )
        if (decision is FreemiumPolicy.Decision.Blocked) {
            return AppResult.Failure(decision.message)
        }
        val needsWatermark = (decision as FreemiumPolicy.Decision.Allowed).watermark

        val platform = inputs.platform
        val mode = inputs.mode
        val durationSeconds = mode.durationSeconds(platform)
        val estimate = CostEstimator.estimate(platform, mode)
        val user = sessionProvider.current()

        // Build the prompt from the user's notes + their style + ad patterns.
        val prompt = AdPromptBuilder.build(
            productNotes = inputs.productNotes,
            styleDescription = inputs.styleDescription,
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
            durationSeconds = durationSeconds,
            aspectRatio = platform.aspectRatio,
            resolution = mode.resolution.apiValue,
        )

        val result = generator.generate(request, onProgress)

        return when (result) {
            is VideoGenerationResult.Error -> {
                // A failed attempt can still cost API time — log it for accounting.
                logAttempt(user.userId, platform, mode, estimate, GenerationAttempt.Outcome.FAILURE)
                AppResult.Failure(result.message, result.cause)
            }

            is VideoGenerationResult.Success -> {
                // The API cost is incurred at this point regardless of what
                // happens locally — log it before any post-processing.
                logAttempt(user.userId, platform, mode, estimate, GenerationAttempt.Outcome.SUCCESS)

                val cleanFile = try {
                    val fileName = "adsmaker_${platform.name.lowercase()}_${clock()}.mp4"
                    downloader.download(appContext, result.videoUrl, fileName)
                } catch (e: Exception) {
                    return AppResult.Failure(
                        "Generated the video but couldn't download it. Please retry.", e,
                    )
                }

                val (finalFile, watermarked) = if (needsWatermark) {
                    try {
                        val trialFile = File(
                            cleanFile.parentFile,
                            cleanFile.nameWithoutExtension + "_trial.mp4",
                        )
                        val wm = watermarker.watermark(appContext, cleanFile, trialFile)
                        // Never leave the clean copy around for a free user.
                        cleanFile.delete()
                        preferences.incrementFreeGenerationsUsed()
                        wm to true
                    } catch (e: Exception) {
                        cleanFile.delete()
                        return AppResult.Failure(
                            "The video was generated but trial processing failed. Please retry.", e,
                        )
                    }
                } else {
                    cleanFile to false
                }

                AppResult.Success(
                    GeneratedAd(
                        file = finalFile,
                        platform = platform,
                        mode = mode,
                        durationSeconds = durationSeconds,
                        estimatedCostUsd = estimate.usd,
                        seed = result.seed,
                        watermarked = watermarked,
                    ),
                )
            }
        }
    }

    private fun logAttempt(
        userId: String,
        platform: com.adsmaker.app.domain.PlatformFormat,
        mode: com.adsmaker.app.domain.GenerationMode,
        estimate: CostEstimator.Estimate,
        outcome: GenerationAttempt.Outcome,
    ) {
        usageLogger.record(
            GenerationAttempt(
                userId = userId,
                platform = platform,
                mode = mode,
                durationSeconds = estimate.seconds,
                estimatedCostUsd = estimate.usd,
                timestampMillis = clock(),
                outcome = outcome,
            ),
        )
    }
}
