package com.erikbalthazar.admobbanner.data.source.ads

import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.google.android.gms.ads.AdRequest
import javax.inject.Inject

class AdRequestFactoryImpl @Inject constructor() : AdRequestFactory {
    override fun create(adRequestData: AdRequestData?): AdRequest {
        val builder = AdRequest.Builder()
        adRequestData?.keywords?.forEach { keyword ->
            builder.addKeyword(keyword)
        }
        return builder.build()
    }
}
