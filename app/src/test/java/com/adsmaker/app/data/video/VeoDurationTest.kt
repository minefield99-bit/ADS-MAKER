package com.adsmaker.app.data.video

import org.junit.Assert.assertEquals
import org.junit.Test

class VeoDurationTest {

    @Test
    fun `durations snap to veo's allowed 4-6-8 tiers`() {
        assertEquals("4s", VeoVideoGenerator.toVeoDuration(1))
        assertEquals("4s", VeoVideoGenerator.toVeoDuration(4))
        assertEquals("6s", VeoVideoGenerator.toVeoDuration(5))
        assertEquals("6s", VeoVideoGenerator.toVeoDuration(6))
        assertEquals("8s", VeoVideoGenerator.toVeoDuration(7))
        assertEquals("8s", VeoVideoGenerator.toVeoDuration(8))
        assertEquals("8s", VeoVideoGenerator.toVeoDuration(15))
    }

    @Test
    fun `app modes map to valid veo durations`() {
        // Draft (4s) and Final (8s) must land exactly on allowed tiers.
        assertEquals("4s", VeoVideoGenerator.toVeoDuration(4))
        assertEquals("8s", VeoVideoGenerator.toVeoDuration(8))
    }
}
