package com.adsmaker.app.data.video

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Downloads a generated video from its remote URL into the app's private
 * `files/generated/` directory, ready to preview and export.
 */
class VideoDownloader(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun download(context: Context, url: String, fileName: String): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "generated").apply { mkdirs() }
            val target = File(dir, fileName)

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Download failed with HTTP ${response.code}")
                }
                val body = response.body ?: throw IOException("Empty response body")
                target.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            target
        }
}
