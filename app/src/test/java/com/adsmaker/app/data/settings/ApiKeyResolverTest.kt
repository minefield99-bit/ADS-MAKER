package com.adsmaker.app.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyResolverTest {

    @Test
    fun `user key overrides build config key`() {
        assertEquals("user-key", ApiKeyResolver.resolve("user-key", "build-key"))
    }

    @Test
    fun `blank user key falls back to build config`() {
        assertEquals("build-key", ApiKeyResolver.resolve("   ", "build-key"))
        assertEquals("build-key", ApiKeyResolver.resolve(null, "build-key"))
    }

    @Test
    fun `user key is trimmed`() {
        assertEquals("abc123", ApiKeyResolver.resolve("  abc123 ", "build-key"))
    }

    @Test
    fun `both empty resolves to empty`() {
        assertEquals("", ApiKeyResolver.resolve(null, ""))
    }

    @Test
    fun `mask reveals only the last four characters`() {
        val masked = ApiKeyResolver.mask("supersecretkey1234")
        assertTrue(masked.endsWith("1234"))
        assertTrue(masked.startsWith("••••"))
        assertTrue(!masked.contains("supersecret"))
    }

    @Test
    fun `mask of blank is empty`() {
        assertEquals("", ApiKeyResolver.mask("  "))
    }
}
