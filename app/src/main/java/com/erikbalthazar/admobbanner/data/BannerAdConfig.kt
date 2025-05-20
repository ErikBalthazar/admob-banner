package com.erikbalthazar.admobbanner.data

import com.google.android.gms.ads.AdSize

data class BannerAdConfig(
    val adUnitId: String,
    val adSize: AdSize = AdSize.BANNER,
)
