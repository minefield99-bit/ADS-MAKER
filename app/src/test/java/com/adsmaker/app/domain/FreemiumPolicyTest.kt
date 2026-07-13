package com.adsmaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreemiumPolicyTest {

    @Test
    fun `owner mode always generates clean and unlimited`() {
        val decision = FreemiumPolicy.evaluate(ownerMode = true, freeGenerationsUsed = 999)
        assertEquals(FreemiumPolicy.Decision.Allowed(watermark = false), decision)
    }

    @Test
    fun `first free generation is allowed but watermarked`() {
        val decision = FreemiumPolicy.evaluate(ownerMode = false, freeGenerationsUsed = 0)
        assertEquals(FreemiumPolicy.Decision.Allowed(watermark = true), decision)
    }

    @Test
    fun `free usage beyond the cap is blocked`() {
        val decision = FreemiumPolicy.evaluate(
            ownerMode = false,
            freeGenerationsUsed = FreemiumPolicy.FREE_GENERATION_LIMIT,
        )
        assertTrue(decision is FreemiumPolicy.Decision.Blocked)
        assertTrue(FreemiumPolicy.isBlocked(false, FreemiumPolicy.FREE_GENERATION_LIMIT))
    }

    @Test
    fun `blocked message explains what to do`() {
        val decision = FreemiumPolicy.evaluate(ownerMode = false, freeGenerationsUsed = 5)
            as FreemiumPolicy.Decision.Blocked
        assertTrue(decision.message.contains("Free trial", ignoreCase = true))
    }
}
