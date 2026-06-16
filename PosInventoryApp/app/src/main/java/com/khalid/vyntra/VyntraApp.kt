package com.khalid.vyntra

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.khalid.vyntra.util.AdManager
import com.khalid.vyntra.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VyntraApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)

        // Initialize AdMob once for the process. No-ops in the pro flavor
        // (BuildConfig.ADS_ENABLED gates inside AdManager). Pre-warm the
        // interstitial so the first show is instant rather than triggering
        // a cold load.
        AdManager.initialize(this)
        AdManager.loadInterstitialAd(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
