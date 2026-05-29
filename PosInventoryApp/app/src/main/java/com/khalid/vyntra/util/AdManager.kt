package com.khalid.vyntra.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.khalid.vyntra.BuildConfig

/**
 * Centralized manager for AdMob banner and interstitial ads.
 * Only loads ads when the "free" flavor is active ([BuildConfig.ADS_ENABLED]).
 */
object AdManager {

    private const val TAG = "AdManager"

    // Google-provided test ad unit IDs
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // TODO: Replace with real ad unit IDs for production release builds
    private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    private val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    /**
     * Initializes the Mobile Ads SDK. Call once in [android.app.Application.onCreate].
     */
    fun initialize(context: Context) {
        if (!BuildConfig.ADS_ENABLED) return

        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapter, status) in statusMap) {
                Log.d(TAG, "Adapter: $adapter, Status: ${status.initializationState}")
            }
            Log.d(TAG, "AdMob SDK initialized")
        }
    }

    /**
     * Creates and loads a banner [AdView]. The caller is responsible for
     * adding the returned view to their layout hierarchy.
     *
     * @param context Activity or application context.
     * @return A loaded [AdView], or `null` if ads are disabled.
     */
    fun loadBannerAd(context: Context): AdView? {
        if (!BuildConfig.ADS_ENABLED) return null

        return AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = bannerAdUnitId
            loadAd(AdRequest.Builder().build())
        }
    }

    /**
     * Pre-loads an interstitial ad so it is ready when [showInterstitialAd] is called.
     * Does nothing if ads are disabled, an ad is already loaded, or a load is in progress.
     */
    fun loadInterstitialAd(context: Context) {
        if (!BuildConfig.ADS_ENABLED) return
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows a previously loaded interstitial ad.
     *
     * @param activity  The activity to show the ad in.
     * @param onDismissed Optional callback invoked after the ad is dismissed or fails to show.
     */
    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "No interstitial ad available, skipping")
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                onDismissed()
                // Pre-load the next one
                loadInterstitialAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
                onDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad shown")
            }
        }

        ad.show(activity)
    }
}
