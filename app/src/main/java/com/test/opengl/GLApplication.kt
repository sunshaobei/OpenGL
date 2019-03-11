package com.test.opengl

import android.app.Application

class GLApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
    }

}
