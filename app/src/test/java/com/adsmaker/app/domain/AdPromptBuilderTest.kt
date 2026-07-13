package com.adsmaker.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPromptBuilderTest {

    @Test
    fun `prompt includes platform pacing and product notes`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "A meditation app that helps you sleep.",
            styleDescription = null,
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = true,
        )
        assertTrue(prompt.contains("TikTok"))
        assertTrue(prompt.contains("9:16"))
        assertTrue(prompt.contains("meditation app", ignoreCase = true))
    }

    @Test
    fun `user style direction is passed through verbatim`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "A budgeting tool.",
            styleDescription = "Retro VHS look, grainy, neon captions, lo-fi music",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = false,
        )
        assertTrue(prompt.contains("Retro VHS look, grainy, neon captions, lo-fi music"))
        assertTrue(prompt.contains("follow the user's direction exactly", ignoreCase = true))
    }

    @Test
    fun `no built-in house style is imposed`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "A budgeting tool.",
            styleDescription = null,
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = true,
        )
        // The old fixed style (sharp foreground -> blur + narration takeover)
        // must be gone entirely.
        assertFalse(prompt.contains("blur", ignoreCase = true))
        assertFalse(prompt.contains("defocus", ignoreCase = true))
        assertFalse(prompt.contains("rack-focus", ignoreCase = true))
        // Without user direction, style is left open for the model.
        assertTrue(prompt.contains("choose a style", ignoreCase = true))
    }

    @Test
    fun `ad structure patterns stay - hook and call-to-action`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "Users can download it free on the Play Store.",
            styleDescription = "minimalist",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = false,
        )
        assertTrue(prompt.contains("hook", ignoreCase = true))
        assertTrue(prompt.contains("Download now"))
    }

    @Test
    fun `blank style is treated as absent`() {
        val prompt = AdPromptBuilder.build(
            productNotes = "x",
            styleDescription = "   ",
            platform = PlatformFormat.TIKTOK,
            hasReferenceImage = false,
        )
        assertTrue(prompt.contains("choose a style", ignoreCase = true))
    }
}
