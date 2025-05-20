package com.erikbalthazar.admobbanner.data.model

import com.google.android.gms.ads.AdSize

data class BannerAdConfig(
    val adUnitId: String,
    val adSize: AdSize = AdSize.BANNER,
)
