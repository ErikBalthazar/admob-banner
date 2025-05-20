package com.erikbalthazar.admobbanner.ui.view.composable.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.erikbalthazar.admobbanner.data.BannerAdConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

/**
 * A composable function that displays a banner ad using AdMob.
 *
 * @param adRequest The [AdRequest] to load the ad with. If null, the ad will not be displayed.
 * @param bannerAdConfig The [BannerAdConfig] containing the configuration for the banner ad,
 *                       such as the ad unit ID and ad size.
 */
@Composable
fun BannerAdView(
    adRequest: AdRequest?,
    bannerAdConfig: BannerAdConfig
) {
    if (adRequest == null) return

    AndroidView(
        modifier = Modifier
            .fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                adUnitId = bannerAdConfig.adUnitId
                setAdSize(bannerAdConfig.adSize)
                loadAd(adRequest)
            }
        },
        update = { adView ->
            adView.loadAd(adRequest)
        }
    )
}
