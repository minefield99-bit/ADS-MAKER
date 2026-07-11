package com.adsmaker.app.di

import android.content.Context
import com.adsmaker.app.BuildConfig
import com.adsmaker.app.data.remote.NetworkFactory
import com.adsmaker.app.data.repository.AdRepository
import com.adsmaker.app.data.session.LocalSessionProvider
import com.adsmaker.app.data.session.SessionProvider
import com.adsmaker.app.data.settings.ApiKeyResolver
import com.adsmaker.app.data.settings.ApiKeyStore
import com.adsmaker.app.data.settings.SecureApiKeyStore
import com.adsmaker.app.data.usage.InMemoryUsageLogger
import com.adsmaker.app.data.usage.UsageLogger
import com.adsmaker.app.data.video.SeedanceVideoGenerator
import com.adsmaker.app.data.video.VideoDownloader
import com.adsmaker.app.data.video.VideoGenerator

/**
 * Minimal manual dependency container. Week 1 avoids a DI framework to stay
 * lightweight; everything is wired here and reachable from the Application.
 * Singletons are created lazily and shared.
 */
class ServiceLocator(private val appContext: Context) {

    /** On-device store for a user-entered fal.ai key (settable in the app). */
    val apiKeyStore: ApiKeyStore by lazy { SecureApiKeyStore(appContext) }

    /** User-entered key wins; the build-time key is the dev fallback. */
    fun effectiveApiKey(): String =
        ApiKeyResolver.resolve(apiKeyStore.getUserKey(), BuildConfig.FAL_API_KEY)

    /** True when a usable key exists from either source. Evaluated on demand. */
    val hasApiKey: Boolean get() = effectiveApiKey().isNotBlank()

    private val usageLogger: UsageLogger by lazy { InMemoryUsageLogger() }

    private val sessionProvider: SessionProvider by lazy { LocalSessionProvider() }

    private val videoGenerator: VideoGenerator by lazy {
        val api = NetworkFactory.createFalService(
            keyProvider = ::effectiveApiKey,
            enableLogging = BuildConfig.DEBUG,
        )
        SeedanceVideoGenerator(api)
    }

    private val videoDownloader: VideoDownloader by lazy { VideoDownloader() }

    val adRepository: AdRepository by lazy {
        AdRepository(
            appContext = appContext,
            generator = videoGenerator,
            downloader = videoDownloader,
            usageLogger = usageLogger,
            sessionProvider = sessionProvider,
        )
    }
}
