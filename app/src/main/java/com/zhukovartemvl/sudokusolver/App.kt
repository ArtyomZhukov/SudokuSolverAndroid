package com.zhukovartemvl.sudokusolver

import android.app.Application
import com.zhukovartemvl.sudokusolver.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.opencv.android.OpenCVLoader

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initDebug()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(appModule)
        }
    }
}
