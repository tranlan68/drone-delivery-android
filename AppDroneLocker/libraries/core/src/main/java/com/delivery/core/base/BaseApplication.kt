package com.delivery.core.base

import android.app.Application
import com.delivery.core.BuildConfig
import timber.log.Timber

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}