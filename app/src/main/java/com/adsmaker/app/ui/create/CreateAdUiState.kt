package com.adsmaker.app.ui.create

import com.adsmaker.app.data.video.VideoProgress
import com.adsmaker.app.domain.CostEstimator
import com.adsmaker.app.domain.GeneratedAd
import com.adsmaker.app.domain.GenerationMode
import com.adsmaker.app.domain.PlatformFormat

/** UI state for the create/generate flow, observed by both screens. */
data class CreateAdUiState(
    val platform: PlatformFormat = PlatformFormat.WEEK1_DEFAULT,
    val mode: GenerationMode = GenerationMode.DEFAULT,
    val hasImage: Boolean = false,
    val hasVideo: Boolean = false,
    val notesFileName: String? = null,
    val notesPreview: String? = null,
    val apiKeyMissing: Boolean = false,

    val showCostDialog: Boolean = false,
    val phase: Phase = Phase.Idle,

    /** Populated on success; consumed by the preview screen. */
    val generatedAd: GeneratedAd? = null,
    val errorMessage: String? = null,
) {
    val costEstimate: CostEstimator.Estimate get() = CostEstimator.estimate(platform, mode)

    val isDraft: Boolean get() = mode == GenerationMode.DRAFT

    val canGenerate: Boolean
        get() = (hasImage || !notesPreview.isNullOrBlank()) &&
            phase !is Phase.Generating &&
            !apiKeyMissing

    sealed interface Phase {
        data object Idle : Phase
        data class Generating(val progress: VideoProgress?) : Phase
        data object Done : Phase
    }
}
