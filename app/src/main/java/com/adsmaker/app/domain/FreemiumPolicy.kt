package com.adsmaker.app.domain

/**
 * Freemium rules, kept pure so they're unit-testable and easy to evolve when
 * real accounts/payments arrive.
 *
 * Business reality (from the spec): EVERY generation — including a free-trial
 * one — bills the owner's fal.ai account (~$0.60 for a 4s draft at Veo Fast
 * prices). So free usage is capped hard, free videos carry a watermark, and
 * only owner mode (later: a paid plan) produces clean videos.
 */
object FreemiumPolicy {

    /** Free watermarked generations allowed per install before payment exists. */
    const val FREE_GENERATION_LIMIT = 1

    sealed interface Decision {
        /** Go ahead; [watermark] says whether the result must be watermarked. */
        data class Allowed(val watermark: Boolean) : Decision
        data class Blocked(val message: String) : Decision
    }

    fun evaluate(ownerMode: Boolean, freeGenerationsUsed: Int): Decision = when {
        ownerMode -> Decision.Allowed(watermark = false)
        freeGenerationsUsed < FREE_GENERATION_LIMIT -> Decision.Allowed(watermark = true)
        else -> Decision.Blocked(
            "Free trial used. Paid plans are coming soon — the owner can enable " +
                "clean generations in Settings.",
        )
    }

    fun isBlocked(ownerMode: Boolean, freeGenerationsUsed: Int): Boolean =
        evaluate(ownerMode, freeGenerationsUsed) is Decision.Blocked
}
