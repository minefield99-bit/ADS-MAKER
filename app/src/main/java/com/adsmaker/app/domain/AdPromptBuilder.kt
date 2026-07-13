package com.adsmaker.app.domain

/**
 * Turns the user's raw materials into a single, well-structured prompt for the
 * video model.
 *
 * Two kinds of guidance go in, deliberately kept separate:
 *  1. STRUCTURE — proven high-performing ad patterns (hook in the first
 *     seconds, one clear benefit, a call-to-action). This is what makes it an
 *     *ad* rather than a random clip, and it always applies.
 *  2. STYLE — entirely the user's decision. If they described a look/feel (in
 *     their own words, possibly describing an ad they like), it is passed
 *     through verbatim. There is NO built-in house style: the app imposes no
 *     fixed transitions, blur effects, or narration treatment.
 *
 * Keeping this pure (no Android deps) makes it unit-testable.
 */
object AdPromptBuilder {

    /** Cheap keyword pass over the notes to guess the call-to-action verb. */
    private val CTA_HINT = Regex("(download|try|get|install|sign up|buy|shop)", RegexOption.IGNORE_CASE)

    fun build(
        productNotes: String?,
        styleDescription: String?,
        platform: PlatformFormat,
        hasReferenceImage: Boolean,
    ): String {
        val notes = productNotes?.trim().takeUnless { it.isNullOrBlank() }
        val style = styleDescription?.trim().takeUnless { it.isNullOrBlank() }
        val product = notes ?: "the product shown in the reference image"
        val callToAction = deriveCallToAction(notes)

        val subject = if (hasReferenceImage) {
            "Feature the product from the reference image prominently."
        } else {
            "Feature the product prominently."
        }

        return buildString {
            appendLine("Create a high-converting ${platform.displayName} video ad.")
            appendLine("Pacing: ${platform.pacingNote}.")
            appendLine()
            appendLine("PRODUCT / CONTEXT:")
            appendLine(product)
            appendLine()
            appendLine("STRUCTURE (follow what works for top ads):")
            appendLine("- First 2 seconds: a strong hook that stops the scroll.")
            appendLine("- $subject")
            appendLine("- Present the single biggest benefit and build desire.")
            appendLine("- End on a clear call-to-action: \"$callToAction\".")
            appendLine()
            if (style != null) {
                appendLine("STYLE (follow the user's direction exactly):")
                appendLine(style)
            } else {
                appendLine("STYLE: choose a style that fits the product and feels native to ${platform.displayName}.")
            }
            append("FORMAT: ${platform.aspectRatio} framing, with audio.")
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
