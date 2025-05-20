package com.erikbalthazar.admobbanner.data.source.ads

import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.google.android.gms.ads.AdRequest

/**
 * Factory interface for creating AdMob ad requests.
 */
interface AdRequestFactory {
    fun create(adRequestData: AdRequestData?): AdRequest
}