package com.adsmaker.app.domain

import java.util.Locale

/**
 * Cost awareness, built in from day one. Every generation attempt costs real
 * money, so the UI shows an estimate and requires explicit confirmation before
 * calling the API.
 *
 * fal.ai bills Seedance 2.0 Fast by generated *tokens*, not a flat per-second
 * rate, and tokens scale with resolution:
 *
 *   tokens = width * height * durationSeconds * 24 / 1024
 *   cost   = tokens / 1000 * USD_PER_1000_TOKENS
 *
 * Verified against fal.ai's published Seedance 2.0 Fast pricing (2026-07): this
 * yields $0.2419/sec at 720p and ~$0.108/sec at 480p — which is why Draft mode
 * (5s @ 480p) is dramatically cheaper than Final (15s @ 720p). Re-check the live
 * rate before launch; only USD_PER_1000_TOKENS needs updating if it moves.
 */
object CostEstimator {

    /** fal.ai Seedance 2.0 Fast token rate (USD per 1000 tokens). */
    const val USD_PER_1000_TOKENS = 0.0112

    private const val TOKEN_FPS_FACTOR = 24
    private const val TOKEN_DIVISOR = 1024.0

    /** Soft budget guard per ad, from the product spec. */
    const val PER_AD_BUDGET_USD = 10.0

    data class Estimate(
        val seconds: Int,
        val resolutionLabel: String,
        val usd: Double,
    ) {
        val formatted: String
            get() = "$" + String.format(Locale.US, "%.2f", usd)
    }

    fun estimate(platform: PlatformFormat, mode: GenerationMode): Estimate {
        val res = mode.resolution
        val seconds = mode.durationSeconds(platform)
        return Estimate(
            seconds = seconds,
            resolutionLabel = res.apiValue,
            usd = costUsd(res.width, res.height, seconds),
        )
    }

    /** Raw fal.ai token-billing cost for a given resolution and duration. */
    fun costUsd(width: Int, height: Int, seconds: Int): Double {
        val tokens = width.toLong() * height * seconds * TOKEN_FPS_FACTOR / TOKEN_DIVISOR
        return tokens / 1000.0 * USD_PER_1000_TOKENS
    }
}
