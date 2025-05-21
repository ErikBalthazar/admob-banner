package com.erikbalthazar.admobbanner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Retries a given action when a network connection becomes available.
 *
 * This function registers a network callback that listens for network availability.
 * When a network with internet capability becomes available, the `onReconnect` lambda
 * is executed, and the network callback is then unregistered.
 *
 * @param context The application context, used to access system services.
 * @param onReconnect A lambda function to be executed when a network connection is re-established.
 * @return A [ConnectivityManager.NetworkCallback] instance that was registered, or `null` if
 *         the [ConnectivityManager] could not be obtained. This can be used to manually
 *         unregister the callback if needed, though it's typically handled internally.
 */
fun retryWhenNetworkAvailable(
    context: Context,
    onReconnect: () -> Unit
): ConnectivityManager.NetworkCallback? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectivityManager.unregisterNetworkCallback(this)
            onReconnect()
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(request, networkCallback)
    return networkCallback
}