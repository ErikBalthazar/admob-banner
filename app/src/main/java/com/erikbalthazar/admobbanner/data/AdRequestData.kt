package com.erikbalthazar.admobbanner.data

/**
 * Data class representing the parameters for an ad request.
 *
 * @property keywords A list of keywords to be used for ad targeting. These keywords help AdMob serve
 * more relevant ads.
 */
data class AdRequestData(
    val keywords: List<String>?,
)
