package com.adsmaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CostEstimatorTest {

    @Test
    fun `veo per-second rate with audio is applied flat`() {
        assertEquals(0.15, CostEstimator.costUsd(1), 1e-9)
        assertEquals(1.20, CostEstimator.costUsd(8), 1e-9)
    }

    @Test
    fun `final tiktok estimate is 8s at 720p costing 1_20`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL)
        assertEquals(8, estimate.seconds)
        assertEquals("720p", estimate.resolutionLabel)
        assertEquals(1.20, estimate.usd, 1e-9)
    }

    @Test
    fun `draft estimate is 4s costing 0_60 - half of final`() {
        val draft = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.DRAFT)
        val final = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL)
        assertEquals(4, draft.seconds)
        assertEquals(0.60, draft.usd, 1e-9)
        assertEquals(final.usd / 2, draft.usd, 1e-9)
    }

    @Test
    fun `platform durations never exceed veo max clip of 8s`() {
        PlatformFormat.entries.forEach { platform ->
            assertTrue("${platform.name} exceeds Veo max", platform.durationSeconds <= 8)
        }
    }

    @Test
    fun `both modes stay well under the per-ad budget`() {
        assertTrue(
            CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL).usd
                < CostEstimator.PER_AD_BUDGET_USD,
        )
    }

    @Test
    fun `formatted cost has two decimals and dollar sign`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.DRAFT)
        assertEquals("$0.60", estimate.formatted)
    }
}
