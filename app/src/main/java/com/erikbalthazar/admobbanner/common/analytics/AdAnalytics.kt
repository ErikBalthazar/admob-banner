package com.erikbalthazar.admobbanner.common.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Utility object for logging ad-related analytics events to Firebase.
 */
object AdAnalytics {
    fun logEvent(context: Context, eventTag: String, data: String?) {
        val bundle = Bundle().apply {
            putString(eventTag, data ?: "")
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventTag, bundle)
    }
}
