package com.adsmaker.app.domain

import java.util.Locale

/**
 * Cost awareness, built in from day one. Every generation attempt costs real
 * money, so the UI shows an estimate and requires explicit confirmation before
 * calling the API.
 *
 * ACTIVE pricing — Veo 3.1 Fast on fal.ai (verified 2026-07): flat per-second,
 * $0.15/sec WITH audio ($0.10 without) at either 720p or 1080p. We always
 * generate with audio. So: Final 8s ≈ $1.20, Draft 4s ≈ $0.60.
 *
 * (Dormant Seedance 2.0 Fast used token billing that scaled with pixels:
 * tokens = w*h*seconds*24/1024 at $0.0112/1000 tokens ≈ $0.24/sec at 720p.
 * If Seedance is reactivated, this estimator must be switched back too.)
 *
 * Re-check the live rate before launch; only the constants below need updating.
 */
object CostEstimator {

    /** Veo 3.1 Fast, per generated second, audio on (what the app always uses). */
    const val USD_PER_SECOND_WITH_AUDIO = 0.15

    /** Veo 3.1 Fast without audio — unused today, kept for reference. */
    const val USD_PER_SECOND_NO_AUDIO = 0.10

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
        val seconds = mode.durationSeconds(platform)
        return Estimate(
            seconds = seconds,
            resolutionLabel = mode.resolution.apiValue,
            usd = costUsd(seconds),
        )
    }

    /** Flat Veo pricing: seconds × rate (audio always on). */
    fun costUsd(seconds: Int): Double = seconds * USD_PER_SECOND_WITH_AUDIO
}
