package com.adsmaker.app.data.video

/**
 * Provider-agnostic contract for the video-generation step.
 *
 * The rest of the app depends only on this interface, never on a concrete
 * model API — so swapping providers is a one-class change. ACTIVE provider:
 * [VeoVideoGenerator] (Veo 3.1 Fast). DORMANT: [SeedanceVideoGenerator]
 * (Seedance 2.0 Fast, early-access gated on fal.ai as of 2026-07).
 */
interface VideoGenerator {
    /**
     * Kicks off a generation and suspends until a video URL is ready or it fails.
     * [onProgress] is invoked as the job moves through the provider's queue.
     */
    suspend fun generate(
        request: VideoRequest,
        onProgress: (VideoProgress) -> Unit = {},
    ): VideoGenerationResult
}

/** Provider-neutral generation request assembled by the repository. */
data class VideoRequest(
    val prompt: String,
    /** Data URI or remote URL of the reference image, if any (image-to-video). */
    val imageDataUri: String?,
    val durationSeconds: Int,
    val aspectRatio: String,
    /** "720p" / "1080p" for Veo 3.1 Fast ("480p"/"720p" on dormant Seedance). */
    val resolution: String = "720p",
    /** Native audio synthesis (used for the voice-over narration). */
    val generateAudio: Boolean = true,
)

/** Coarse progress for the loading UI. */
data class VideoProgress(
    val stage: Stage,
    val queuePosition: Int? = null,
    val note: String? = null,
) {
    enum class Stage { UPLOADING, QUEUED, GENERATING, DOWNLOADING }
}

sealed interface VideoGenerationResult {
    /** [videoUrl] is a remote URL the caller then downloads to local storage. */
    data class Success(val videoUrl: String, val seed: Long?) : VideoGenerationResult
    data class Error(val message: String, val cause: Throwable? = null) : VideoGenerationResult
}
