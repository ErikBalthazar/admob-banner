package com.erikbalthazar.admobbanner.ui.view.composable.screen

import android.content.Context
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
import androidx.compose.runtime.MutableState
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
import com.erikbalthazar.admobbanner.data.model.AdEvent
import com.erikbalthazar.admobbanner.common.Status
import com.erikbalthazar.admobbanner.ui.view.extension.getHeightInDp
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.flow.collectLatest
import com.erikbalthazar.admobbanner.BuildConfig
import com.erikbalthazar.admobbanner.common.crashlytics.AdCrashlytics
import com.erikbalthazar.admobbanner.data.model.AdEventType
import com.erikbalthazar.admobbanner.common.Logger
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun AdScreen(
    modifier: Modifier = Modifier,
    viewModel: AdsViewModel = hiltViewModel()
) {
    val adRequestState by viewModel.adRequestState.collectAsState()
    val context = LocalContext.current
    val adUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
    val bannerAdConfig = BannerAdConfig(
        adUnitId = adUnitId,
        adSize = AdSize.BANNER
    )
    val eventMessages = rememberEventMessages()

    val currentAdEventType = remember { mutableStateOf<AdEventType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBannerAd(null)
    }

    CollectAdEvents(
        viewModel = viewModel,
        context = context,
        eventMessages = eventMessages,
        currentAdEventType = currentAdEventType
    )

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            handleAdViewRelease(viewModel)
        }
    }

    AdScreenContent(
        modifier = modifier,
        bannerAdConfig = bannerAdConfig,
        adRequestState = adRequestState,
        adListener = CustomAdListener(viewModel),
        currentAdEventType = currentAdEventType.value
    )
}

private fun handleAdViewRelease(viewModel: AdsViewModel) {
    viewModel.cancelNetworkCallback()
}

@Composable
private fun rememberEventMessages(): Map<AdEventType, String> {
    return mapOf(
        AdEventType.Loaded to stringResource(R.string.adscreen_banner_adevent_loaded_message),
        AdEventType.Failed to stringResource(R.string.adscreen_banner_adevent_failed_message),
        AdEventType.Opened to stringResource(R.string.adscreen_banner_adevent_opened_message),
        AdEventType.Clicked to stringResource(R.string.adscreen_banner_adevent_clicked_message),
        AdEventType.Closed to stringResource(R.string.adscreen_banner_adevent_closed_message),
        AdEventType.Impression to stringResource(R.string.adscreen_banner_adevent_impression_message),
        AdEventType.Swipe to stringResource(R.string.adscreen_banner_adevent_swipe_clicked_message)
    )
}

@Composable
private fun CollectAdEvents(
    viewModel: AdsViewModel,
    context: Context,
    eventMessages: Map<AdEventType, String>,
    currentAdEventType: MutableState<AdEventType?>
) {
    LaunchedEffect(Unit) {
        viewModel.adEvents.collectLatest { adEvent ->
            currentAdEventType.value = adEvent.type
            when (adEvent) {
                AdEvent.Loaded -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
                is AdEvent.FailedToLoad -> {
                    viewModel.handleAdError(adEvent.loadAdError, null)
                    logAdEventCrash(
                        eventTag = AnalyticsTags.AD_EVENT,
                        logMessage = "${eventMessages[adEvent.type]} ${adEvent.loadAdError.message}",
                        crashlyticsMessage = adEvent.loadAdError.message
                    )
                }
                AdEvent.Opened -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
                AdEvent.Clicked -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
                AdEvent.Closed -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
                AdEvent.Impression -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
                AdEvent.SwipeGestureClicked -> {
                    logAdEvent(context, AnalyticsTags.AD_EVENT, eventMessages[adEvent.type].orEmpty())
                }
            }
        }
    }
}

@Composable
private fun AdScreenContent(
    modifier: Modifier = Modifier,
    bannerAdConfig: BannerAdConfig,
    adRequestState: Status<AdRequest?>,
    adListener: AdListener,
    currentAdEventType: AdEventType?
) {
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
                adListener = adListener,
                currentAdEventType = currentAdEventType
            )
        }
    }
}

@Composable
private fun InstructionsText() {
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
private fun Banner(
    bannerAdConfig: BannerAdConfig,
    adRequestState: Status<AdRequest?>,
    adListener: AdListener,
    currentAdEventType: AdEventType?
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
                if (currentAdEventType == null || currentAdEventType == AdEventType.Failed) {
                    AdPlaceholder(adSize = bannerAdConfig.adSize)
                }
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
private fun AdPlaceholder(
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
private fun AdError(
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

private fun logAdEvent(
    context: Context,
    eventTag: String,
    message: String
) {
    Logger.d(eventTag, message)
    AdAnalytics.logEvent(context = context, eventTag = eventTag, data = message)
}

private fun logAdEventCrash(
    eventTag: String,
    logMessage: String,
    crashlyticsMessage: String
) {
    Logger.e(eventTag, logMessage)
    AdCrashlytics.log(message = crashlyticsMessage)
}
