package com.minseonglove.jlptwords.ad

import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.util.TimeProvider
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.admob_ios_native_ad_id
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSError
import platform.darwin.NSObject
import swiftPMImport.jlpt.words.presentation.kmp.GADAdLoader
import swiftPMImport.jlpt.words.presentation.kmp.GADAdLoaderAdTypeNative
import swiftPMImport.jlpt.words.presentation.kmp.GADAdLoaderDelegateProtocol
import swiftPMImport.jlpt.words.presentation.kmp.GADNativeAd
import swiftPMImport.jlpt.words.presentation.kmp.GADNativeAdLoaderDelegateProtocol
import swiftPMImport.jlpt.words.presentation.kmp.GADRequest
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class NativeAdManagerImpl(
    private val isAdZeroEnabled: IsAdZeroEnabled,
) : NativeAdManager {
    private var cachedAd: PlatformNativeAd? = null
    private var refreshJob: Job? = null
    private var lastLoadTime: Long = 0L

    // 매니저가 소유하는 단일 스코프. 갱신 요청마다 새 스코프를 만들면 stopRefreshAd 가 취소하지 못하는
    // Job 이 남는다. GADAdLoader 는 메인 스레드에서 시작해야 한다.
    private val refreshScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // GADAdLoader.delegate 는 weak 프로퍼티이고 로더 자신도 지역 변수로는 요청 도중 해제될 수 있다.
    // 둘 다 여기서 붙잡지 않으면 콜백이 오지 않아 아래 코루틴이 영원히 재개되지 않는다.
    private var adLoader: GADAdLoader? = null
    private var loaderDelegate: NativeAdLoaderDelegate? = null

    override suspend fun load(): PlatformNativeAd? {
        return withContext(Dispatchers.Main) {
            val nativeAdId = getString(Res.string.admob_ios_native_ad_id)
            suspendCancellableCoroutine { continuation ->
                // 델리게이트 콜백은 모두 메인 스레드로 오지만, 성공/실패가 함께 통지되는
                // 경우에도 재개는 한 번만 해야 한다.
                var isResumed = false

                fun resumeOnce(ad: PlatformNativeAd?) {
                    if (isResumed) return
                    isResumed = true
                    continuation.resume(ad)
                }

                val delegate =
                    NativeAdLoaderDelegate(
                        onAdReceived = { ad ->
                            val platformAd = PlatformNativeAd(ad)
                            cachedAd = platformAd
                            lastLoadTime = TimeProvider.currentTimeMillis()
                            resumeOnce(platformAd)
                        },
                        onAdFailed = {
                            resumeOnce(null)
                        },
                    )

                val loader =
                    GADAdLoader(
                        adUnitID = nativeAdId,
                        rootViewController = null,
                        adTypes = listOf(GADAdLoaderAdTypeNative),
                        options = null,
                    )
                loader.delegate = delegate
                loaderDelegate = delegate
                adLoader = loader

                continuation.invokeOnCancellation {
                    loaderDelegate = null
                    adLoader = null
                }
                loader.loadRequest(GADRequest())
            }
        }
    }

    private class NativeAdLoaderDelegate(
        private val onAdReceived: (GADNativeAd) -> Unit,
        private val onAdFailed: () -> Unit,
    ) : NSObject(),
        GADAdLoaderDelegateProtocol,
        GADNativeAdLoaderDelegateProtocol {
        override fun adLoader(
            adLoader: GADAdLoader,
            didReceiveNativeAd: GADNativeAd,
        ) {
            onAdReceived(didReceiveNativeAd)
        }

        override fun adLoader(
            adLoader: GADAdLoader,
            didFailToReceiveAdWithError: NSError,
        ) {
            onAdFailed()
        }

        override fun adLoaderDidFinishLoading(adLoader: GADAdLoader) {
            // Called when adLoader has finished loading
        }
    }

    override suspend fun getValidAd(): PlatformNativeAd? {
        return if (cachedAd != null && isAdValid()) {
            cachedAd
        } else {
            load()
        }
    }

    override fun clearAd() {
        cachedAd = null
    }

    override fun startRefreshAd() {
        stopRefreshAd()

        refreshJob =
            refreshScope.launch {
                checkAndRefreshAd()

                while (true) {
                    delay(REFRESH_INTERVAL)
                    checkAndRefreshAd()
                }
            }
    }

    override fun stopRefreshAd() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private suspend fun checkAndRefreshAd() {
        val shouldRefresh =
            when {
                isAdZeroEnabled() -> false
                cachedAd == null -> true
                isAdValid().not() -> true
                else -> false
            }

        if (shouldRefresh) {
            load()
        }
    }

    private fun isAdValid(): Boolean {
        if (lastLoadTime == 0L) return false

        val currentTime = TimeProvider.currentTimeMillis()
        val timeSinceLoad = currentTime - lastLoadTime

        return timeSinceLoad < AD_VALIDITY_DURATION
    }

    companion object {
        private const val AD_VALIDITY_DURATION = 55 * 60 * 1000L
        private const val REFRESH_INTERVAL = 5 * 60 * 1000L
    }
}
