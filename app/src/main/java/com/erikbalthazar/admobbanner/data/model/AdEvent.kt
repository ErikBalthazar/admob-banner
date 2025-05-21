package com.erikbalthazar.admobbanner.data.model

import com.google.android.gms.ads.LoadAdError

/**
 * Represents events that can occur during the lifecycle of an ad.
 * This sealed class is used to convey different states and interactions with an ad.
 */
enum class AdEventType { Loaded, Failed, Opened, Clicked, Closed, Impression, Swipe }

sealed class AdEvent(val type: AdEventType) {
    data object Loaded : AdEvent(AdEventType.Loaded)
    data class FailedToLoad(val loadAdError: LoadAdError) : AdEvent(AdEventType.Failed)
    data object Opened : AdEvent(AdEventType.Opened)
    data object Closed : AdEvent(AdEventType.Closed)
    data object Clicked : AdEvent(AdEventType.Clicked)
    data object Impression : AdEvent(AdEventType.Impression)
    data object SwipeGestureClicked : AdEvent(AdEventType.Swipe)
}
