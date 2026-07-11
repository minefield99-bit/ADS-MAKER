package com.adsmaker.app.domain

/**
 * Turns the user's raw materials into a single, well-structured prompt for the
 * video model.
 *
 * This is where the app's "creative intelligence" lives for Week 1. Rather than
 * stitching clips randomly, it encodes:
 *   1. The requested visual style (sharp foreground → fade to blur + overlay/VO).
 *   2. Proven high-performing app-ad patterns (hook in the first seconds, a
 *      single clear benefit, a call-to-action at the end).
 *   3. The target platform's pacing and format.
 *
 * Keeping this pure (no Android deps) makes it unit-testable.
 */
object AdPromptBuilder {

    /** Cheap keyword pass over the notes to guess what the product is "about". */
    private val CTA_HINT = Regex("(download|try|get|install|sign up|buy|shop)", RegexOption.IGNORE_CASE)

    fun build(
        productNotes: String?,
        platform: PlatformFormat,
        style: AdStyle = AdStyle.DEFAULT,
        hasReferenceImage: Boolean,
    ): String {
        val notes = productNotes?.trim().takeUnless { it.isNullOrBlank() }
        val product = notes ?: "the product shown in the reference image"
        val callToAction = deriveCallToAction(notes)

        val subject = if (hasReferenceImage) {
            "Open on the product from the reference image, crisp and sharp in the foreground."
        } else {
            "Open on a crisp, sharp hero shot of the product in the foreground."
        }

        return buildString {
            appendLine("Create a high-converting ${platform.displayName} video ad.")
            appendLine("Pacing: ${platform.pacingNote}.")
            appendLine()
            appendLine("PRODUCT / CONTEXT:")
            appendLine(product)
            appendLine()
            appendLine("STRUCTURE (follow what works for top app ads):")
            appendLine("- First 2 seconds: a strong visual hook that stops the scroll.")
            appendLine("- $subject")
            appendLine("- ${style.transitionDescription}")
            appendLine("- As the background blurs, bold on-screen text and a confident voice-over present the single biggest benefit and build desire.")
            appendLine("- End on a clear call-to-action: \"$callToAction\".")
            appendLine()
            appendLine("STYLE: ${style.moodDescription} Vertical ${platform.aspectRatio} framing. Clean, modern, social-media native.")
            append("AUDIO: energetic voice-over narration matching the pacing, plus subtle background music.")
        }.trim()
    }

    private fun deriveCallToAction(notes: String?): String {
        if (notes == null) return "Download now"
        val match = CTA_HINT.find(notes)?.value?.replaceFirstChar { it.uppercase() }
        return when {
            match != null -> "$match now"
            else -> "Download now"
        }
    }
}
