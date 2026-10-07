package com.hussain.learningdashboard

import android.app.Application
import com.hussain.learningdashboard.di.initKoinAndroid

class LearningDashboardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
