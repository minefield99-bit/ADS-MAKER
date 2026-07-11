package com.adsmaker.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdInputsTest {

    @Test
    fun `notes alone are generatable`() {
        val inputs = AdInputs(imageUri = null, videoUri = null, productNotes = "Cool app")
        assertTrue(inputs.isGeneratable)
    }

    @Test
    fun `empty inputs are not generatable`() {
        val inputs = AdInputs(imageUri = null, videoUri = null, productNotes = "   ")
        assertFalse(inputs.isGeneratable)
    }

    @Test
    fun `tiktok is the week1 default platform`() {
        val inputs = AdInputs(imageUri = null, videoUri = null, productNotes = "x")
        assertTrue(inputs.platform == PlatformFormat.TIKTOK)
    }
}
