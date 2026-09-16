package com.minseonglove.jlptwords.ad

import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.util.TimeProvider
import com.minseonglove.jlptwords.util.runCatchingCancellable
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.admob_ios_interstitial_ad_id
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
import swiftPMImport.jlpt.words.presentation.kmp.GADInterstitialAd
import swiftPMImport.jlpt.words.presentation.kmp.GADRequest
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class InterstitialAdManagerImpl(
    private val isAdZeroEnabled: IsAdZeroEnabled,
) : InterstitialAdManager {
    private var cachedAd: PlatformInterstitialAd? = null
    private var refreshJob: Job? = null
    private var lastLoadTime: Long = 0L

    // 매니저가 소유하는 단일 스코프. 갱신 요청마다 새 스코프를 만들면 stopRefreshAd 가 취소하지 못하는
    // Job 이 남는다. GADInterstitialAd 로드는 메인 스레드에서 시작해야 한다.
    private val refreshScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private suspend fun requestInterstitialAd(): GADInterstitialAd? =
        withContext(Dispatchers.Main) {
            val interstitialAdId = getString(Res.string.admob_ios_interstitial_ad_id)
            suspendCancellableCoroutine { continuation ->
                GADInterstitialAd.loadWithAdUnitID(
                    adUnitID = interstitialAdId,
                    request = GADRequest(),
                    completionHandler = { ad, error ->
                        if (continuation.isActive) {
                            continuation.resume(ad)
                        }
                    },
                )
            }
        }

    override suspend fun load(): PlatformInterstitialAd? {
        val ad = requestInterstitialAd() ?: return null
        lastLoadTime = TimeProvider.currentTimeMillis()
        // 캐시본과 반환본은 같은 인스턴스여야 한다. 따로 감싸면 호출부가 설정한 콜백(과 그 델리게이트
        // 강한 참조)이 캐시된 쪽에 남지 않는다.
        return PlatformInterstitialAd(ad).also { cachedAd = it }
    }

    private fun isAdValid(): Boolean {
        if (lastLoadTime == 0L) return false

        val currentTime = TimeProvider.currentTimeMillis()
        val timeSinceLoad = currentTime - lastLoadTime

        return timeSinceLoad < AD_VALIDITY_DURATION
    }

    override suspend fun getValidAd(): PlatformInterstitialAd? {
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

    private suspend fun isAdZeroEnabled(): Boolean {
        return runCatchingCancellable {
            isAdZeroEnabled.invoke()
        }.getOrDefault(false)
    }

    companion object {
        private const val AD_VALIDITY_DURATION = 55 * 60 * 1000L
        private const val REFRESH_INTERVAL = 5 * 60 * 1000L
    }
}
