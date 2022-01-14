package com.example.tmdbclient.shared

import android.app.Application
import com.example.tmdbclient.BuildConfig

class TmdbClientApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Logs.addLogger(LocalLogger(applicationContext))
        }
    }
}