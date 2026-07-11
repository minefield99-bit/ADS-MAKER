package com.adsmaker.app.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class AdPromptBuilderTest {

    @Test
    fun `prompt includes platform pacing and vertical framing for tiktok`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "A meditation app that helps you sleep.",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = true,
        )
        assertTrue(prompt.contains("TikTok"))
        assertTrue(prompt.contains("9:16"))
        assertTrue(prompt.contains("meditation app", ignoreCase = true))
    }

    @Test
    fun `prompt encodes the sharp-to-blur signature transition`() {
        val prompt = AdPromptBuilder.build(
            productNotes = null,
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = true,
        )
        assertTrue(prompt.contains("blur", ignoreCase = true))
        assertTrue(prompt.contains("voice-over", ignoreCase = true))
    }

    @Test
    fun `call-to-action is derived from notes keywords`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "Users can download it free on the Play Store.",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = false,
        )
        assertTrue(prompt.contains("Download now"))
    }

    @Test
    fun `without an image the prompt describes a hero shot instead`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "A budgeting tool.",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = false,
        )
        assertTrue(prompt.contains("hero shot", ignoreCase = true))
    }
}
