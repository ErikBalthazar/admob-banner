package com.erikbalthazar.admobbanner.ui.view.composable.screen

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import com.erikbalthazar.admobbanner.R
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.erikbalthazar.admobbanner.common.analytics.AdAnalytics
import com.erikbalthazar.admobbanner.common.analytics.AnalyticsTags
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
import com.erikbalthazar.admobbanner.BuildConfig
import com.erikbalthazar.admobbanner.common.crashlytics.AdCrashlytics

@Composable
fun AdScreen(
    modifier: Modifier = Modifier,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val adRequestState by viewModel.adRequestState.collectAsState()

    val context = LocalContext.current
    val adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    val loadedMsg = stringResource(R.string.adscreen_banner_adevent_loaded_message)
    val failedMsg = stringResource(R.string.adscreen_banner_adevent_failed_message)
    val openedMsg = stringResource(R.string.adscreen_banner_adevent_opened_message)
    val clickedMsg = stringResource(R.string.adscreen_banner_adevent_clicked_message)
    val closedMsg = stringResource(R.string.adscreen_banner_adevent_closed_message)
    val impressionMsg = stringResource(R.string.adscreen_banner_adevent_impression_message)
    val swipeMsg = stringResource(R.string.adscreen_banner_adevent_swipe_clicked_message)

    val bannerAdConfig = BannerAdConfig(
        adUnitId = adUnitId,
        adSize = AdSize.BANNER
    )

    LaunchedEffect(Unit) {
        viewModel.loadBannerAd(null)
    }

    LaunchedEffect(Unit) {
        viewModel.adEvents.collectLatest { adEvent ->
            when (adEvent) {
                AdEvent.Loaded -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = loadedMsg
                    )
                }

                is AdEvent.FailedToLoad -> {
                    viewModel.handleAdError(
                        error = adEvent.loadAdError,
                        adRequestData = null
                    )
                    logAdEventCrash(
                        eventTag = AnalyticsTags.AD_EVENT,
                        logMessage = "$failedMsg ${adEvent.loadAdError.message}",
                        crashlyticsMessage = adEvent.loadAdError.message
                    )
                }

                AdEvent.Opened -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = openedMsg
                    )
                }

                AdEvent.Clicked -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = clickedMsg
                    )
                }

                AdEvent.Closed -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = closedMsg
                    )
                }

                AdEvent.Impression -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = impressionMsg
                    )
                }

                AdEvent.SwipeGestureClicked -> {
                    logAdEvent(
                        context = context,
                        eventTag = AnalyticsTags.AD_EVENT,
                        message = swipeMsg
                    )
                }
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
                adListener = CustomAdListener(viewModel),
                onAdViewRelease = {
                    viewModel.cancelNetworkCallback()
                }
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
    adListener: AdListener,
    onAdViewRelease: () -> Unit = {},
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
                    adListener = adListener,
                    onRelease = onAdViewRelease
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

fun logAdEvent(
    context: Context,
    eventTag: String,
    message: String
) {
    Log.d(eventTag, message)
    AdAnalytics.logEvent(context = context, eventTag = eventTag, data = message)
}

fun logAdEventCrash(
    eventTag: String,
    logMessage: String,
    crashlyticsMessage: String
) {
    Log.e(eventTag, logMessage)
    AdCrashlytics.log(message = crashlyticsMessage)
}
