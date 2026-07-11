package com.adsmaker.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Local media helpers: reading the attached text file, turning the picked image
 * into a data URI for the API, and exporting the finished video to the gallery.
 * All IO runs off the main thread.
 */
object MediaUtils {

    private const val MAX_IMAGE_DIMEN = 1280
    private const val JPEG_QUALITY = 85

    /** Reads a user-picked text file as plain UTF-8 text (capped for safety). */
    suspend fun readTextFile(context: Context, uri: Uri, maxChars: Int = 20_000): String =
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText().take(maxChars)
            } ?: ""
        }

    /**
     * Loads the picked image, downscales it, and returns a `data:image/jpeg;base64,...`
     * URI. fal.ai accepts data URIs for image inputs, so we avoid a separate
     * upload round-trip in Week 1.
     */
    suspend fun imageToDataUri(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            val bitmap = decodeScaledBitmap(context, uri) ?: return@withContext null
            val bytes = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.toByteArray()
            }
            bitmap.recycle()
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        // First pass: bounds only, to compute a sample size.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val largest = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        var sample = 1
        while (largest / sample > MAX_IMAGE_DIMEN) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }

    /**
     * Exports a locally-saved video file into the device's shared gallery
     * (Movies/AdsMaker). Returns the MediaStore [Uri] on success.
     */
    suspend fun exportToGallery(context: Context, sourceFile: File, displayName: String): Uri? =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/AdsMaker")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = resolver.insert(collection, values) ?: return@withContext null
            resolver.openOutputStream(itemUri)?.use { out ->
                sourceFile.inputStream().use { it.copyTo(out) }
            } ?: return@withContext null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
            itemUri
        }

    /** Content URI for sharing a generated file out to other apps. */
    fun shareableUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
