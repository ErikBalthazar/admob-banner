package com.erikbalthazar.admobbanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AdsViewModel @Inject constructor() : ViewModel() {

    private val _adRequest = MutableStateFlow<AdRequest?>(null)
    val adRequest: StateFlow<AdRequest?> = _adRequest

    fun loadBannerAd() {
        viewModelScope.launch {
            val request = createAdRequest()
            _adRequest.value = request
        }
    }

    private fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}