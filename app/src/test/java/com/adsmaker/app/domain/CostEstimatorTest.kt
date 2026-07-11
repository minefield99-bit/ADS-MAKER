package com.adsmaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CostEstimatorTest {

    @Test
    fun `tiktok estimate matches 15s at rate`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK)
        assertEquals(15, estimate.seconds)
        assertEquals(15 * CostEstimator.USD_PER_SECOND, estimate.usd, 0.0001)
    }

    @Test
    fun `tiktok estimate stays under per-ad budget`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK)
        assertTrue(estimate.usd < CostEstimator.PER_AD_BUDGET_USD)
    }

    @Test
    fun `formatted cost has two decimals and dollar sign`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK)
        assertEquals("$1.35", estimate.formatted)
    }
}
