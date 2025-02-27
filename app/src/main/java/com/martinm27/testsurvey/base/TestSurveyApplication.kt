package com.martinm27.testsurvey.base

import android.app.Application
import com.martinm27.testsurvey.di.apiModule
import com.martinm27.testsurvey.di.featuresModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestSurveyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@TestSurveyApplication)

            val modules = listOf(
                apiModule,
                featuresModule
            )
            modules(modules)
        }
    }
}