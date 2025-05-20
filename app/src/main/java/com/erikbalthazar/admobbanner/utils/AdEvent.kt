package com.erikbalthazar.admobbanner.utils

import com.google.android.gms.ads.LoadAdError

/**
 * Represents events that can occur during the lifecycle of an ad.
 * This sealed class is used to convey different states and interactions with an ad.
 */
sealed class AdEvent {
    data object Loaded : AdEvent()
    data class FailedToLoad(val loadAdError: LoadAdError) : AdEvent()
    data object Opened : AdEvent()
    data object Closed : AdEvent()
    data object Clicked : AdEvent()
    data object Impression : AdEvent()
    data object SwipeGestureClicked : AdEvent()
}
