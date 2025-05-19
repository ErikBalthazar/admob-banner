package com.erikbalthazar.admobbanner.ui.view.composable.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erikbalthazar.admobbanner.ui.view.composable.component.BannerAdView
import com.erikbalthazar.admobbanner.ui.viewmodel.AdsViewModel

@Composable
fun AdScreen(viewModel: AdsViewModel = hiltViewModel()) {
    val adRequest by viewModel.adRequest.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.loadBannerAd()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        BannerAdView(adRequest = adRequest)
    }
}
