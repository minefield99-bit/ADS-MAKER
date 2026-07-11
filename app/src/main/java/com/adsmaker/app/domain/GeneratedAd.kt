package com.adsmaker.app.domain

import java.io.File

/** A finished ad video sitting in local storage, ready to preview and export. */
data class GeneratedAd(
    val file: File,
    val platform: PlatformFormat,
    val durationSeconds: Int,
    val estimatedCostUsd: Double,
    val seed: Long?,
)
