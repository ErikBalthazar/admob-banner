package com.erikbalthazar.admobbanner.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erikbalthazar.admobbanner.common.exception.NetworkException
import com.erikbalthazar.admobbanner.common.exception.UnknownException
import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactory
import com.erikbalthazar.admobbanner.utils.AdEvent
import com.erikbalthazar.admobbanner.utils.Status
import com.erikbalthazar.admobbanner.utils.retryWhenNetworkAvailable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing ad-related data and logic.
 *
 * This ViewModel is responsible for creating and providing AdRequests
 * that can be used to load ads in the UI.
 */
@HiltViewModel
class AdsViewModel @Inject constructor(
    application: Application,
    private val adRequestFactory: AdRequestFactory
) : AndroidViewModel(application) {

    private val _adRequestState = MutableStateFlow<Status<AdRequest?>>(Status.Loading)
    val adRequestState: StateFlow<Status<AdRequest?>> = _adRequestState

    private val _adEvents = MutableSharedFlow<AdEvent>(replay = 0)
    val adEvents: SharedFlow<AdEvent> = _adEvents

    internal var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     *
     * It cancels any ongoing network callback registration to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        cancelNetworkCallback()
    }

    /**
     * Loads a banner ad request.
     *
     * This function initiates the process of creating an [AdRequest].
     * It updates the [_adRequestState] to [Status.Loading] initially.
     * Then, it launches a coroutine in the [viewModelScope] to create the ad request
     * using the [adRequestFactory].
     * - If the creation is successful, [_adRequestState] is updated to [Status.Success]
     *   with the created [AdRequest].
     * - If an [Exception] occurs during the creation, [_adRequestState] is updated to
     *   [Status.Error] with the caught exception.
     *
     * @param adRequestData Optional data to customize the ad request.
     */
    fun loadBannerAd(adRequestData: AdRequestData?) {
        _adRequestState.value = Status.Loading
        viewModelScope.launch {
            try {
                val request = adRequestFactory.create(adRequestData)
                _adRequestState.value = Status.Success(request)
            } catch (e: Exception) {
                _adRequestState.value = Status.Error(e)
            }
        }
    }

    /**
     * Emits an [AdEvent] to the [_adEvents] shared flow.
     *
     * This function is used to signal various ad-related events (e.g., ad loaded, ad failed to load)
     * to observers of the [adEvents] flow.
     *
     * @param event The [AdEvent] to emit.
     */
    fun onAdEvent(event: AdEvent) {
        viewModelScope.launch {
            _adEvents.emit(event)
        }
    }

    /**
     * Handles errors that occur during ad loading.
     *
     * If the error is a network error or an internal error, it updates the
     * ad request state to reflect the error and attempts to retry loading the ad
     * when the network becomes available.
     *
     * @param error The [LoadAdError] that occurred.
     * @param adRequestData The [AdRequestData] used for the failed ad request,
     *                      which will be used for the retry attempt.
     */
    fun handleAdError(
        error: LoadAdError,
        adRequestData: AdRequestData?
    ) {
        if (error.code == AdRequest.ERROR_CODE_NETWORK_ERROR ||
            error.code == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
            _adRequestState.value = Status.Error(NetworkException())
            networkCallback = retryWhenNetworkAvailable(
                context = getApplication<Application>().applicationContext,
                onReconnect = {
                    loadBannerAd(adRequestData)
                }
            )
        } else {
            _adRequestState.value = Status.Error(UnknownException())
        }
    }

    /**
     * Unregisters the network callback if it has been previously registered.
     * This is typically called when the ViewModel is cleared to prevent memory leaks.
     */
    fun cancelNetworkCallback() {
        val connectivityManager =
            getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
}
