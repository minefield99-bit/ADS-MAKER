package com.adsmaker.app.domain

import android.net.Uri

/**
 * Everything the user supplied on the input screen. [imageUri] is the primary
 * driver (image-to-video); [videoUri] and [productNotes] are optional context.
 */
data class AdInputs(
    val imageUri: Uri?,
    val videoUri: Uri?,
    /** Plain-text contents already read from the user's attached text file. */
    val productNotes: String?,
    val platform: PlatformFormat = PlatformFormat.WEEK1_DEFAULT,
    val mode: GenerationMode = GenerationMode.DEFAULT,
) {
    val hasImage: Boolean get() = imageUri != null
    val hasVideo: Boolean get() = videoUri != null
    val hasNotes: Boolean get() = !productNotes.isNullOrBlank()

    /** Enough to attempt a generation? Need at least an image or some notes. */
    val isGeneratable: Boolean get() = hasImage || hasNotes
}
