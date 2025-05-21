package com.erikbalthazar.admobbanner.common

import android.util.Log
import com.erikbalthazar.admobbanner.BuildConfig

/**
 * A utility object for logging messages.
 *
 * This object provides methods for logging debug and error messages.
 * The messages are only logged if the application is running in debug mode.
 */
object Logger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    fun e(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }
} 