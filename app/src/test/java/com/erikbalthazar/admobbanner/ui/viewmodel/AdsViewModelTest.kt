package com.erikbalthazar.admobbanner.ui.viewmodel

import com.erikbalthazar.admobbanner.data.model.AdRequestData
import com.erikbalthazar.admobbanner.data.source.ads.AdRequestFactory
import com.erikbalthazar.admobbanner.utils.Status
import com.google.android.gms.ads.AdRequest
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
        viewModel = AdsViewModel(adRequestFactory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBannerAd with valid AdRequestData`() = runTest {
        val data = AdRequestData(keywords = listOf("game", "ads"))
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
        val exception = RuntimeException("test error")
        every { mockFactory.create(any()) } throws exception

        val viewModel = AdsViewModel(mockFactory)
        viewModel.loadBannerAd(AdRequestData(listOf("test")))

        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Error)
        assertEquals("test error", (result as Status.Error).throwable?.message)
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
            viewModel.loadBannerAd(AdRequestData(listOf("cancel")))
            cancel()
        }

        advanceUntilIdle()

        val result = viewModel.adRequestState.value
        assertTrue(result is Status.Loading || result is Status.Success)
    }
}