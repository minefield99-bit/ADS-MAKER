package com.adsmaker.app.domain

import java.io.File

/** A finished ad video sitting in local storage, ready to preview and export. */
data class GeneratedAd(
    val file: File,
    val platform: PlatformFormat,
    val mode: GenerationMode,
    val durationSeconds: Int,
    val estimatedCostUsd: Double,
    val seed: Long?,
    /** True when the free-trial watermark was burned into the file. */
    val watermarked: Boolean = false,
)
