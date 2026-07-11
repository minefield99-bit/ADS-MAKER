package com.adsmaker.app

import android.app.Application
import com.adsmaker.app.di.ServiceLocator

/** App entry point; owns the [ServiceLocator] for the process lifetime. */
class AdsMakerApplication : Application() {

    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(this)
    }
}
