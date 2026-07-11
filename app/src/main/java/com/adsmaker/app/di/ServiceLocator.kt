package com.adsmaker.app.di

import android.content.Context
import com.adsmaker.app.BuildConfig
import com.adsmaker.app.data.remote.NetworkFactory
import com.adsmaker.app.data.repository.AdRepository
import com.adsmaker.app.data.session.LocalSessionProvider
import com.adsmaker.app.data.session.SessionProvider
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

    /** True when a fal.ai key was supplied at build time. */
    val hasApiKey: Boolean get() = BuildConfig.FAL_API_KEY.isNotBlank()

    private val usageLogger: UsageLogger by lazy { InMemoryUsageLogger() }

    private val sessionProvider: SessionProvider by lazy { LocalSessionProvider() }

    private val videoGenerator: VideoGenerator by lazy {
        val api = NetworkFactory.createFalService(
            apiKey = BuildConfig.FAL_API_KEY,
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
