package com.entaingroup.nexttogo

import android.app.Application
import com.entaingroup.nexttogo.di.networkModule
import com.entaingroup.nexttogo.di.repositoryModule
import com.entaingroup.nexttogo.di.viewModelModule
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.core.context.startKoin
import timber.log.Timber

class NextToGoApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Used to converse epoch time to instance pre API 26
        AndroidThreeTen.init(this)

        if(BuildConfig.DEBUG) { Timber.plant(Timber.DebugTree()) }

        startKoin {
            modules(networkModule, repositoryModule, viewModelModule)
        }
    }
}