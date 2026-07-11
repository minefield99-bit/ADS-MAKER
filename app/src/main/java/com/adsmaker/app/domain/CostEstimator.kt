package com.adsmaker.app.domain

import java.util.Locale

/**
 * Cost awareness, built in from day one. Every generation attempt costs real
 * money, so the UI shows an estimate and requires explicit confirmation before
 * calling the API.
 *
 * Seedance 2.0 Fast is billed per second of generated video. The rate moves, so
 * it lives in one constant — re-check fal.ai's live pricing before launch.
 */
object CostEstimator {

    /** USD per generated second for Seedance 2.0 Fast (estimate — verify at launch). */
    const val USD_PER_SECOND = 0.09

    /** Soft budget guard per ad, from the product spec. */
    const val PER_AD_BUDGET_USD = 10.0

    data class Estimate(
        val seconds: Int,
        val usd: Double,
    ) {
        val formatted: String
            get() = "$" + String.format(Locale.US, "%.2f", usd)
    }

    fun estimate(platform: PlatformFormat): Estimate {
        val usd = platform.durationSeconds * USD_PER_SECOND
        return Estimate(seconds = platform.durationSeconds, usd = usd)
    }
}
