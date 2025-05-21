package com.erikbalthazar.admobbanner.ui.view.composable.screen

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erikbalthazar.admobbanner.R
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.erikbalthazar.admobbanner.common.exception.NetworkException
import com.erikbalthazar.admobbanner.data.model.BannerAdConfig
import com.erikbalthazar.admobbanner.data.source.ads.CustomAdListener
import com.erikbalthazar.admobbanner.ui.theme.AdErrorBackground
import com.erikbalthazar.admobbanner.ui.theme.Dimens
import com.erikbalthazar.admobbanner.ui.theme.AdPlaceholderBackground
import com.erikbalthazar.admobbanner.ui.theme.AdPlaceholderBorder
import com.erikbalthazar.admobbanner.ui.theme.BoxBorder
import com.erikbalthazar.admobbanner.ui.view.composable.component.BannerAdView
import com.erikbalthazar.admobbanner.ui.viewmodel.AdsViewModel
import com.erikbalthazar.admobbanner.utils.AdEvent
import com.erikbalthazar.admobbanner.utils.Status
import com.erikbalthazar.admobbanner.utils.getHeightInDp
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdScreen(
    modifier: Modifier = Modifier,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val adRequestState by viewModel.adRequestState.collectAsState()

    val loadedMsg = stringResource(R.string.adscreen_banner_adevent_loaded_message)
    val failedMsg = stringResource(R.string.adscreen_banner_adevent_failed_message)
    val openedMsg = stringResource(R.string.adscreen_banner_adevent_opened_message)
    val clickedMsg = stringResource(R.string.adscreen_banner_adevent_clicked_message)
    val closedMsg = stringResource(R.string.adscreen_banner_adevent_closed_message)
    val impressionMsg = stringResource(R.string.adscreen_banner_adevent_impression_message)
    val swipeMsg = stringResource(R.string.adscreen_banner_adevent_swipe_clicked_message)

    val bannerAdConfig = BannerAdConfig(
        adUnitId = stringResource(R.string.admob_adunitid),
        adSize = AdSize.BANNER
    )

    LaunchedEffect(Unit) {
        viewModel.loadBannerAd(null)
    }

    LaunchedEffect(Unit) {
        viewModel.adEvents.collectLatest { adEvent ->
            when (adEvent) {
                AdEvent.Loaded -> Log.d(
                    "AdEvent", loadedMsg
                )

                is AdEvent.FailedToLoad -> {
                    viewModel.handleAdError(
                        error = adEvent.loadAdError,
                        adRequestData = null
                    )
                    Log.e(
                        "AdEvent",
                        "$failedMsg ${adEvent.loadAdError.message} - ${adEvent.loadAdError.code}"
                    )
                }

                AdEvent.Opened -> Log.d(
                    "AdEvent", openedMsg
                )

                AdEvent.Clicked -> Log.d(
                    "AdEvent", clickedMsg
                )

                AdEvent.Closed -> Log.d(
                    "AdEvent", closedMsg
                )

                AdEvent.Impression -> Log.d(
                    "AdEvent", impressionMsg
                )

                AdEvent.SwipeGestureClicked -> Log.d(
                    "AdEvent", swipeMsg
                )
            }
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            viewModel.cancelNetworkCallback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.PaddingMedium),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    width = Dimens.BorderWidth,
                    color = BoxBorder
                )
        ) {
            InstructionsText()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerAdConfig.adSize.getHeightInDp())
        ) {
            Banner(
                bannerAdConfig = bannerAdConfig,
                adRequestState = adRequestState,
                adListener = CustomAdListener(viewModel)
            )
        }
    }
}

@Composable
fun InstructionsText() {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.adscreen_instructions_message),
            modifier = Modifier.padding(Dimens.PaddingMedium)
        )
    }
}

@Composable
fun Banner(
    bannerAdConfig: BannerAdConfig,
    adRequestState: Status<AdRequest?>,
    adListener: AdListener
) {
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
                    bannerAdConfig = bannerAdConfig,
                    adListener = adListener
                )
            }
            is Status.Error -> {
                var errorMessage = stringResource(R.string.adscreen_banner_unknown_error_message)
                if (adRequestState.throwable is NetworkException) {
                    errorMessage = stringResource(R.string.adscreen_banner_network_error_message)
                }
                AdError(
                    adSize = bannerAdConfig.adSize,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

@Composable
fun AdPlaceholder(
    adSize: AdSize,
) {
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
fun AdError(
    adSize: AdSize,
    errorMessage: String
) {
    val heightDp = adSize.getHeightInDp()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .background(AdErrorBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = errorMessage)
    }
}
