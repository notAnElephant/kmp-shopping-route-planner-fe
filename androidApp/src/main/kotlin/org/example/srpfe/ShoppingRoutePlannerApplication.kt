package org.example.srpfe

import android.app.Application
import org.example.srpfe.di.initKoin
import org.koin.android.ext.koin.androidContext

class ShoppingRoutePlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ShoppingRoutePlannerApplication)
        }
    }
}
