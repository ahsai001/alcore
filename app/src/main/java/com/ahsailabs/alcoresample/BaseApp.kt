package com.ahsailabs.alcoresample

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.ahsailabs.sqlitewrapper.Lookup

/**
 * Created by ahmad s on 12/10/20.
 */
class BaseApp: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        Lookup.init(this, true)

    }
}