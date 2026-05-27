package com.audine.dedalo

import android.app.Application
import com.audine.dedalo.core.di.AppContainer
import com.google.firebase.FirebaseApp

class DedaloApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        container = AppContainer(this)
    }
}
