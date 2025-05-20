package com.erikbalthazar.admobbanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactory
import com.erikbalthazar.admobbanner.utils.Status
import com.google.android.gms.ads.AdRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val adRequestFactory: AdRequestFactory
) : ViewModel() {

    private val _adRequestState = MutableStateFlow<Status<AdRequest?>>(Status.Loading)
    val adRequestState: StateFlow<Status<AdRequest?>> = _adRequestState

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
}
