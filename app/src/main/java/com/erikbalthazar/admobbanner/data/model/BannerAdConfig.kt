package com.erikbalthazar.admobbanner.data.model

import com.google.android.gms.ads.AdSize

/**
 * Represents the configuration for a banner ad.
 *
 * @property adUnitId The ad unit ID for the banner ad.
 * @property adSize The size of the banner ad. Defaults to [AdSize.BANNER].
 */
data class BannerAdConfig(
    val adUnitId: String,
    val adSize: AdSize = AdSize.BANNER,
)
