package com.adsmaker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CostEstimatorTest {

    @Test
    fun `720p per-second cost matches fal token pricing`() {
        // 720x1280x24/1024 = 21600 tokens/sec, * 0.0112/1000 = $0.24192/sec.
        val perSecond = CostEstimator.costUsd(720, 1280, 1)
        assertEquals(0.24192, perSecond, 1e-5)
    }

    @Test
    fun `480p is much cheaper per second than 720p`() {
        val p480 = CostEstimator.costUsd(480, 854, 1)
        val p720 = CostEstimator.costUsd(720, 1280, 1)
        assertTrue(p480 < p720 / 2)
    }

    @Test
    fun `final tiktok estimate is 15s at 720p`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL)
        assertEquals(15, estimate.seconds)
        assertEquals("720p", estimate.resolutionLabel)
        // 15 * 0.24192 = 3.6288
        assertEquals(3.6288, estimate.usd, 1e-3)
    }

    @Test
    fun `draft estimate is 5s at 480p and far cheaper than final`() {
        val draft = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.DRAFT)
        val final = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL)
        assertEquals(5, draft.seconds)
        assertEquals("480p", draft.resolutionLabel)
        assertTrue("draft should be well under a third of final", draft.usd < final.usd / 3)
    }

    @Test
    fun `both modes stay under the per-ad budget`() {
        assertTrue(
            CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.FINAL).usd
                < CostEstimator.PER_AD_BUDGET_USD,
        )
    }

    @Test
    fun `formatted cost has two decimals and dollar sign`() {
        val estimate = CostEstimator.estimate(PlatformFormat.TIKTOK, GenerationMode.DRAFT)
        assertTrue(estimate.formatted.startsWith("$"))
        assertTrue(Regex("""\$\d+\.\d{2}""").matches(estimate.formatted))
    }
}
