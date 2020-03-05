package com.agileburo.anytype.app

import android.app.Application
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.core_utils.tools.CrashlyticsTree
import com.agileburo.anytype.di.common.ComponentManager
import com.agileburo.anytype.di.main.*
import com.facebook.stetho.Stetho
import timber.log.Timber

class AndroidApplication : Application() {

    private val main: MainComponent by lazy {
        DaggerMainComponent
            .builder()
            .contextModule(ContextModule(this))
            .dataModule(DataModule())
            .configModule(ConfigModule())
            .utilModule(UtilModule())
            .deviceModule(DeviceModule())
            .build()
    }

    val componentManager by lazy {
        ComponentManager(main)
    }

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        setupStetho()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(CrashlyticsTree())
    }

    private fun setupStetho() {
        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this)
    }
}