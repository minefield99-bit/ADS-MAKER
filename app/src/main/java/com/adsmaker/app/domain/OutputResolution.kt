package com.adsmaker.app.domain

/**
 * Output resolutions supported by the active provider (Veo 3.1 Fast: 720p and
 * 1080p, same per-second price). [apiValue] is what the API expects in the
 * `resolution` field; the pixel dimensions are the 9:16 sizes, kept for
 * display and future per-pixel providers (dormant Seedance billed by pixels).
 */
enum class OutputResolution(val apiValue: String, val width: Int, val height: Int) {
    P720("720p", 720, 1280),
    P1080("1080p", 1080, 1920),
}
