package com.adsmaker.app.data.usage

import com.adsmaker.app.domain.PlatformFormat

/** One generation attempt — success or failure — for later billing/quota use. */
data class GenerationAttempt(
    val userId: String,
    val platform: PlatformFormat,
    val durationSeconds: Int,
    val estimatedCostUsd: Double,
    val timestampMillis: Long,
    val outcome: Outcome,
) {
    enum class Outcome { SUCCESS, FAILURE }
}

/**
 * Records every generation attempt, including failed ones — a failed generation
 * can still consume paid API time, so it must be tracked for cost accounting.
 *
 * Week 1 keeps an in-memory + logcat record. The interface is intentionally the
 * shape a Room/remote-backed usage store would have, so wiring persistence and
 * per-user billing later is a drop-in replacement.
 */
interface UsageLogger {
    fun record(attempt: GenerationAttempt)
    fun all(): List<GenerationAttempt>
    fun totalSpendUsd(): Double
}
