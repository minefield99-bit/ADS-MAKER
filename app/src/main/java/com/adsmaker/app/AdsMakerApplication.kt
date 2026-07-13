package com.adsmaker.app

import android.app.Application
import com.adsmaker.app.di.ServiceLocator
import java.io.File

/** App entry point; owns the [ServiceLocator] for the process lifetime. */
class AdsMakerApplication : Application() {

    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(this)
        sweepPendingCleanFiles()
    }

    /**
     * Free-tier generations download a clean "_pending" intermediate that is
     * deleted the moment the watermarked copy exists. If the process dies in
     * that window the clean file would survive on disk — sweep on every start
     * so a free user can never recover an unwatermarked video.
     */
    private fun sweepPendingCleanFiles() {
        Thread {
            File(filesDir, "generated")
                .listFiles { f -> f.name.endsWith("_pending.mp4") }
                ?.forEach { it.delete() }
        }.start()
    }
}
