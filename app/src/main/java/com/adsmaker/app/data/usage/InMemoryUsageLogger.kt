package com.adsmaker.app.data.usage

import android.util.Log
import java.util.Collections

/**
 * Week 1 [UsageLogger]: thread-safe in-memory list plus a logcat line per
 * attempt. Good enough to verify cost tracking works end-to-end before real
 * persistence and billing arrive.
 */
class InMemoryUsageLogger : UsageLogger {

    private val attempts = Collections.synchronizedList(mutableListOf<GenerationAttempt>())

    override fun record(attempt: GenerationAttempt) {
        attempts.add(attempt)
        Log.i(
            TAG,
            "attempt user=${attempt.userId} platform=${attempt.platform.displayName} " +
                "mode=${attempt.mode.label} tier=${if (attempt.freeTier) "free" else "owner"} " +
                "secs=${attempt.durationSeconds} est=$${"%.2f".format(attempt.estimatedCostUsd)} " +
                "outcome=${attempt.outcome} totalSpend=$${"%.2f".format(totalSpendUsd())}",
        )
    }

    override fun all(): List<GenerationAttempt> = synchronized(attempts) { attempts.toList() }

    override fun totalSpendUsd(): Double =
        synchronized(attempts) { attempts.sumOf { it.estimatedCostUsd } }

    private companion object {
        const val TAG = "AdsMakerUsage"
    }
}
