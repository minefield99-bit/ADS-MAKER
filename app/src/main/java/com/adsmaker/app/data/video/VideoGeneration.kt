package com.adsmaker.app.data.video

/**
 * Provider-agnostic contract for the video-generation step.
 *
 * The rest of the app depends only on this interface, never on fal.ai/Seedance
 * directly — so swapping providers later (if Seedance's pricing or access
 * changes) is a one-class change. See [com.adsmaker.app.data.video.SeedanceVideoGenerator].
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
    /** "480p" / "720p" — Seedance 2.0 Fast tops out at 720p. */
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
