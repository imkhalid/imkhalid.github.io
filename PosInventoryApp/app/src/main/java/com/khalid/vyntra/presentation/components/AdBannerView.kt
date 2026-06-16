package com.khalid.vyntra.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.khalid.vyntra.BuildConfig

/**
 * Displays an AdMob banner ad.
 * Only rendered when the build is the "free" flavor with ADS_ENABLED = true.
 * In the "pro" flavor this composable emits nothing.
 */
@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = if (BuildConfig.DEBUG) {
        // Google-provided test banner ad unit ID
        "ca-app-pub-3940256099942544/6300978111"
    } else {
        // Replace with your real ad unit ID for production
        "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    }
) {
    if (!BuildConfig.ADS_ENABLED) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}
