package com.erikbalthazar.admobbanner.data.source.ads

import com.erikbalthazar.admobbanner.ui.viewmodel.AdsViewModel
import com.erikbalthazar.admobbanner.data.model.AdEvent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

/**
 * A custom [AdListener] that forwards ad events to the provided [AdsViewModel].
 *
 * This class extends [AdListener] and overrides its methods to capture various ad lifecycle events.
 * Upon receiving an event, it notifies the [AdsViewModel] by calling `onAdEvent` with a
 * corresponding [AdEvent].
 *
 * @property viewModel The [AdsViewModel] instance that will receive ad event notifications.
 */
class CustomAdListener(
    private val viewModel: AdsViewModel
) : AdListener() {

    override fun onAdLoaded() {
        super.onAdLoaded()
        viewModel.onAdEvent(AdEvent.Loaded)
    }

    override fun onAdFailedToLoad(adError: LoadAdError) {
        super.onAdFailedToLoad(adError)
        viewModel.onAdEvent(AdEvent.FailedToLoad(adError))
    }

    override fun onAdOpened() {
        super.onAdOpened()
        viewModel.onAdEvent(AdEvent.Opened)
    }

    override fun onAdClicked() {
        super.onAdClicked()
        viewModel.onAdEvent(AdEvent.Clicked)
    }

    override fun onAdClosed() {
        super.onAdClosed()
        viewModel.onAdEvent(AdEvent.Closed)
    }

    override fun onAdImpression() {
        super.onAdImpression()
        viewModel.onAdEvent(AdEvent.Impression)
    }

    override fun onAdSwipeGestureClicked() {
        super.onAdSwipeGestureClicked()
        viewModel.onAdEvent(AdEvent.SwipeGestureClicked)
    }
}