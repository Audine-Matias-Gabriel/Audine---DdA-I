package com.audine.dedalo

import android.app.Application
import com.audine.dedalo.core.di.AppContainer

class DedaloApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
