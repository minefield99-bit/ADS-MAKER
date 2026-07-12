package com.adsmaker.app.domain

/**
 * Output resolutions Seedance 2.0 Fast supports (it tops out at 720p). Pixel
 * dimensions are the 9:16 sizes and are used to compute fal.ai's token-based
 * cost. [apiValue] is what the API expects in the `resolution` field.
 */
enum class OutputResolution(val apiValue: String, val width: Int, val height: Int) {
    P480("480p", 480, 854),
    P720("720p", 720, 1280),
}
