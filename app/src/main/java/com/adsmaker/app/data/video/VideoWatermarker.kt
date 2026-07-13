package com.adsmaker.app.data.video

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Burns the free-trial watermark INTO the video on-device, after the clean
 * file has been downloaded from the provider. Paying users / owner mode skip
 * this step entirely — the provider output is already clean, so no money is
 * wasted generating twice.
 *
 * Uses Media3 Transformer with a static text overlay (centered, translucent —
 * deliberately hard to crop out). Audio passes through untouched.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class VideoWatermarker {

    /**
     * Re-encodes [input] with the watermark and returns the new file.
     * Throws on failure; the caller decides how to handle it.
     */
    suspend fun watermark(context: Context, input: File, output: File): File {
        // Transformer must be created and started on a Looper thread.
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val overlay = TextOverlay.createStaticTextOverlay(watermarkText())
                val effects = Effects(
                    /* audioProcessors = */ ImmutableList.of(),
                    /* videoEffects = */ ImmutableList.of(OverlayEffect(ImmutableList.of(overlay))),
                )
                val item = EditedMediaItem.Builder(MediaItem.fromUri(input.toUri()))
                    .setEffects(effects)
                    .build()

                val transformer = Transformer.Builder(context)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult,
                        ) {
                            continuation.resume(output)
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException,
                        ) {
                            continuation.resumeWithException(exportException)
                        }
                    })
                    .build()

                transformer.start(item, output.absolutePath)
                continuation.invokeOnCancellation { transformer.cancel() }
            }
        }
    }

    private fun watermarkText(): SpannableString {
        val text = SpannableString(WATERMARK_TEXT)
        val all = 0..text.length
        text.setSpan(
            ForegroundColorSpan(Color.argb(0xB3, 0xFF, 0xFF, 0xFF)),
            all.first, all.last, Spanned.SPAN_INCLUSIVE_INCLUSIVE,
        )
        text.setSpan(
            AbsoluteSizeSpan(WATERMARK_TEXT_SIZE_PX),
            all.first, all.last, Spanned.SPAN_INCLUSIVE_INCLUSIVE,
        )
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            all.first, all.last, Spanned.SPAN_INCLUSIVE_INCLUSIVE,
        )
        return text
    }

    private companion object {
        const val WATERMARK_TEXT = "ADS MAKER · FREE TRIAL"
        const val WATERMARK_TEXT_SIZE_PX = 56
    }
}
