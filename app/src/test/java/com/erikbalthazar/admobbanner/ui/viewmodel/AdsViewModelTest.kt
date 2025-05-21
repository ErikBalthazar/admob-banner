package com.erikbalthazar.admobbanner.ui.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import com.erikbalthazar.admobbanner.common.exception.NetworkException
import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactory
import com.erikbalthazar.admobbanner.utils.AdEvent
import com.erikbalthazar.admobbanner.utils.Status
import com.erikbalthazar.admobbanner.utils.retryWhenNetworkAvailable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdsViewModelTest {

    private lateinit var viewModel: AdsViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var adRequestFactory: AdRequestFactory

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        adRequestFactory = mockk()
        val application = mockk<Application>(relaxed = true)
        viewModel = AdsViewModel(application, adRequestFactory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBannerAd with valid AdRequestData`() = runTest {
        val data = AdRequestData(keywords = listOf("ads"))
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data) } returns mockAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd with null AdRequestData`() = runTest {
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(null) } returns mockAdRequest

        viewModel.loadBannerAd(null)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd with AdRequestData having null keywords`() = runTest {
        val data = AdRequestData(keywords = null)
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data) } returns mockAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd with AdRequestData having empty keywords list`() = runTest {
        val data = AdRequestData(keywords = emptyList())
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data) } returns mockAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd with AdRequestData containing empty string keyword`() = runTest {
        val data = AdRequestData(keywords = listOf(""))
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data) } returns mockAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd with AdRequestData containing special character keywords`() = runTest {
        val data = AdRequestData(keywords = listOf("!@#\$%", "^&*()"))
        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data) } returns mockAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)
        assertNotNull((result as Status.Success).data)
    }

    @Test
    fun `loadBannerAd when AdRequestFactory throws exception`() = runTest {
        val mockFactory = mockk<AdRequestFactory>()
        val application = mockk<Application>(relaxed = true)
        val exception = RuntimeException("error")
        every { mockFactory.create(any()) } throws exception

        val viewModel = AdsViewModel(application, mockFactory)
        viewModel.loadBannerAd(AdRequestData(listOf("test")))

        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Error)
        assertEquals("error", (result as Status.Error).throwable?.message)
    }

    @Test
    fun `loadBannerAd with valid AdRequestData calls factory`() = runTest {
        val data = AdRequestData(keywords = listOf("game"))
        val fakeAdRequest = mockk<AdRequest>()
        every { adRequestFactory.create(data) } returns fakeAdRequest

        viewModel.loadBannerAd(data)
        advanceUntilIdle()

        verify(exactly = 1) { adRequestFactory.create(data) }
        assertTrue(viewModel.adRequestState.value is Status.Success)
    }

    @Test
    fun `loadBannerAd initial state`() {
        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Loading)
    }

    @Test
    fun `loadBannerAd state transition to Loading`() = runTest {
        viewModel.loadBannerAd(null)

        assertTrue(viewModel.adRequestState.value is Status.Loading)
        advanceUntilIdle()
    }

    @Test
    fun `loadBannerAd multiple calls sequentially`() = runTest {
        val data1 = AdRequestData(keywords = listOf("sports"))
        val data2 = AdRequestData(keywords = listOf("tech"))

        val mockAdRequest = mockk<AdRequest>()

        every { adRequestFactory.create(data1) } returns mockAdRequest
        every { adRequestFactory.create(data2) } returns mockAdRequest

        viewModel.loadBannerAd(data1)
        advanceUntilIdle()
        val result1 = viewModel.adRequestState.value
        assertTrue(result1 is Status.Success)

        viewModel.loadBannerAd(data2)
        advanceUntilIdle()
        val result2 = viewModel.adRequestState.value
        assertTrue(result2 is Status.Success)
        assertNotSame(result1, result2)
    }

    @Test
    fun `loadBannerAd coroutine cancellation`() = runTest {
        backgroundScope.launch {
            viewModel.loadBannerAd(AdRequestData(listOf("")))
            cancel()
        }

        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Loading || result is Status.Success)
    }

    @Test
    fun `onAdEvent emits AdEvent correctly`() = runTest {
        val testEvent = AdEvent.Clicked
        val events = mutableListOf<AdEvent>()

        val job = launch {
            viewModel.adEvents.toList(events)
        }

        viewModel.onAdEvent(testEvent)
        advanceUntilIdle()

        assertTrue(events.contains(testEvent))
        job.cancel()
    }

    @Test
    fun `onAdEvent multiple events`() = runTest {
        val testEvents = listOf(AdEvent.Loaded, AdEvent.Opened, AdEvent.Closed)
        val collectedEvents = mutableListOf<AdEvent>()

        val job = launch {
            viewModel.adEvents.toList(collectedEvents)
        }

        testEvents.forEach { viewModel.onAdEvent(it) }
        advanceUntilIdle()

        assertTrue(collectedEvents.containsAll(testEvents))
        job.cancel()
    }

    @Test
    fun `onAdEvent coroutine cancellation`() = runTest {
        val testEvent = AdEvent.Impression
        val job = launch {
            viewModel.onAdEvent(testEvent)
            cancel()
        }

        job.join()
        assertTrue(job.isCancelled)
    }

    @Test
    fun `handleAdError network error handling`() = runTest {
        val adRequestData = AdRequestData(listOf(""))
        viewModel.loadBannerAd(adRequestData)
        advanceUntilIdle()

        val loadAdError = mockk<LoadAdError>()
        every { loadAdError.code } returns AdRequest.ERROR_CODE_NETWORK_ERROR
        every { loadAdError.message } returns "error"

        viewModel.handleAdError(loadAdError, adRequestData)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Error)
        assertTrue((result as Status.Error).throwable is NetworkException)
    }

    @Test
    fun `handleAdError internal error handling`() = runTest {
        val adRequestData = AdRequestData(listOf(""))
        viewModel.loadBannerAd(adRequestData)
        advanceUntilIdle()

        val loadAdError = mockk<LoadAdError>()
        every { loadAdError.code } returns AdRequest.ERROR_CODE_INTERNAL_ERROR
        every { loadAdError.message } returns "error"

        viewModel.handleAdError(loadAdError, adRequestData)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Error)
        assertTrue((result as Status.Error).throwable is NetworkException)
    }

    @Test
    fun `handleAdError with other error codes`() = runTest {
        val adRequestData = AdRequestData(listOf(""))
        viewModel.loadBannerAd(adRequestData)
        advanceUntilIdle()

        val loadAdError = mockk<LoadAdError>()
        every { loadAdError.code } returns AdRequest.ERROR_CODE_NO_FILL

        viewModel.handleAdError(loadAdError, adRequestData)
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Error)
        assertFalse((result as Status.Error).throwable is NetworkException)
    }

    @Test
    fun `handleAdError network callback registration`() = runTest {
        val data = AdRequestData(listOf(""))

        val loadAdError = mockk<LoadAdError>()
        every { loadAdError.code } returns AdRequest.ERROR_CODE_NETWORK_ERROR

        val mockedCallback = mockk<ConnectivityManager.NetworkCallback>()
        mockkStatic("com.erikbalthazar.admobbanner.utils.NetworkUtilsKt")

        every {
            retryWhenNetworkAvailable(any(), any())
        } returns mockedCallback

        viewModel.handleAdError(loadAdError, data)
        advanceUntilIdle()

        assertEquals(mockedCallback, viewModel.networkCallback)
        unmockkStatic("com.erikbalthazar.admobbanner.utils.NetworkUtilsKt")
    }

    @Test
    fun `handleAdError onReconnect triggers loadBannerAd`() = runTest {
        val data = AdRequestData(listOf(""))

        val loadAdError = mockk<LoadAdError>()
        every { loadAdError.code } returns AdRequest.ERROR_CODE_INTERNAL_ERROR

        val mockAdRequest = mockk<AdRequest>()
        every { adRequestFactory.create(data) } returns mockAdRequest

        val onReconnectSlot = slot<() -> Unit>()

        val mockedCallback = mockk<ConnectivityManager.NetworkCallback>()
        mockkStatic("com.erikbalthazar.admobbanner.utils.NetworkUtilsKt")

        every {
            retryWhenNetworkAvailable(any(), capture(onReconnectSlot))
        } returns mockedCallback

        viewModel.handleAdError(loadAdError, data)
        onReconnectSlot.captured.invoke()
        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Success)

        unmockkStatic("com.erikbalthazar.admobbanner.utils.NetworkUtilsKt")
    }
}