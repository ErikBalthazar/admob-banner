package com.erikbalthazar.admobbanner.data.source.ads

import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.google.android.gms.ads.AdRequest
import javax.inject.Inject

/**
 * Default implementation of [AdRequestFactory] that creates [AdRequest] objects.
 *
 * This class uses the [AdRequest.Builder] to construct AdRequest instances.
 * If [AdRequestData] is provided, its keywords will be added to the request.
 */
class AdRequestFactoryImpl @Inject constructor() : AdRequestFactory {
    override fun create(adRequestData: AdRequestData?): AdRequest {
        val builder = AdRequest.Builder()
        adRequestData?.keywords?.forEach { keyword ->
            builder.addKeyword(keyword)
        }
        return builder.build()
    }
}
