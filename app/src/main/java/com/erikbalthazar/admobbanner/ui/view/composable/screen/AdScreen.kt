package com.erikbalthazar.admobbanner.ui.view.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erikbalthazar.admobbanner.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.erikbalthazar.admobbanner.data.BannerAdConfig
import com.erikbalthazar.admobbanner.ui.theme.AdErrorBackground
import com.erikbalthazar.admobbanner.ui.theme.Dimens
import com.erikbalthazar.admobbanner.ui.theme.AdPlaceholderBackground
import com.erikbalthazar.admobbanner.ui.theme.AdPlaceholderBorder
import com.erikbalthazar.admobbanner.ui.view.composable.component.BannerAdView
import com.erikbalthazar.admobbanner.ui.viewmodel.AdsViewModel
import com.erikbalthazar.admobbanner.utils.Status
import com.erikbalthazar.admobbanner.utils.getHeightInDp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize

@Composable
fun AdScreen(
    modifier: Modifier = Modifier,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val adRequestState by viewModel.adRequestState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBannerAd(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.PaddingMedium),
        verticalArrangement = Arrangement.Bottom
    ) {
        Banner(adRequestState)
    }
}

@Composable
fun Banner(adRequestState: Status<AdRequest?>) {
    val bannerAdConfig = BannerAdConfig(
        adUnitId = stringResource(R.string.admob_adunitid)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.PaddingSmall)
    ) {
        when (adRequestState) {
            is Status.Loading -> {
                AdPlaceholder(adSize = bannerAdConfig.adSize)
            }
            is Status.Success -> {
                BannerAdView(
                    adRequest = adRequestState.data,
                    bannerAdConfig = bannerAdConfig
                )
            }
            is Status.Error -> {
                AdError(adSize = bannerAdConfig.adSize)
            }
        }
    }
}

@Composable
fun AdPlaceholder(adSize: AdSize) {
    val heightDp = adSize.getHeightInDp()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .border(Dimens.BorderWidth, AdPlaceholderBorder)
            .background(AdPlaceholderBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.adscreen_banner_loading_message))
    }
}

@Composable
fun AdError(adSize: AdSize) {
    val heightDp = adSize.getHeightInDp()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(AdErrorBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.adscreen_banner_error_message))
    }
}

@Preview(showBackground = true)
@Composable
fun BannerLoadingPreview() {
    Banner(adRequestState = Status.Loading)
}

@Preview(showBackground = true)
@Composable
fun BannerSuccessPreview() {
    val fakeAdRequest = AdRequest.Builder().build()
    Banner(adRequestState = Status.Success(fakeAdRequest))
}

@Preview(showBackground = true)
@Composable
fun BannerErrorPreview() {
    Banner(adRequestState = Status.Error(Exception()))
}

