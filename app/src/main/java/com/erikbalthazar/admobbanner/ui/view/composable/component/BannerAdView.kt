package com.erikbalthazar.admobbanner.ui.view.composable.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.erikbalthazar.admobbanner.data.model.BannerAdConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

/**
 * A composable function that displays a banner ad using AdMob.
 *
 * This function handles the lifecycle of the AdView, including pausing, resuming, and destroying the ad
 * in sync with the Composable lifecycle. Permite também callbacks opcionais para eventos de lifecycle.
 *
 * @param adRequest The [AdRequest] to load the ad with. If null, the ad will not be displayed.
 * @param bannerAdConfig The [BannerAdConfig] containing the configuration for the banner ad,
 *                       such as the ad unit ID and ad size.
 * @param adListener The [AdListener] to handle ad events (e.g., ad loaded, ad failed to load).
 * @param onResume Optional callback invoked when the AdView is resumed (Lifecycle.Event.ON_RESUME).
 * @param onPause Optional callback invoked when the AdView is paused (Lifecycle.Event.ON_PAUSE).
 * @param onDestroy Optional callback invoked when the AdView is destroyed (Lifecycle.Event.ON_DESTROY).
 */
@Composable
fun BannerAdView(
    adRequest: AdRequest?,
    bannerAdConfig: BannerAdConfig,
    adListener: AdListener,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null
) {
    if (adRequest == null) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val adView = remember {
        AdView(context).apply {
            adUnitId = bannerAdConfig.adUnitId
            setAdSize(bannerAdConfig.adSize)
            setAdListener(adListener)
            loadAd(adRequest)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    adView.resume()
                    onResume?.invoke()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    adView.pause()
                    onPause?.invoke()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    onDestroy?.invoke()
                    adView.destroy()
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { adView }
    )
}

