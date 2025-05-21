package com.erikbalthazar.admobbanner.common.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * A utility object for logging messages to Firebase Crashlytics.
 */
object AdCrashlytics {
    fun log (message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }
}
