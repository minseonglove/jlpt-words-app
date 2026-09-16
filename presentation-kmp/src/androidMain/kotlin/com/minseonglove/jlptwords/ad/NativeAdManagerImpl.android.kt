package com.minseonglove.jlptwords.ad

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.util.TimeProvider
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.admob_native_ad_id
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class NativeAdManagerImpl(
    private val context: Context,
    private val isAdZeroEnabled: IsAdZeroEnabled,
) : NativeAdManager {
    private var cachedAd: PlatformNativeAd? = null
    private var refreshJob: Job? = null
    private var lastLoadTime: Long = 0L

    // 매니저가 소유하는 단일 스코프. 갱신 요청마다 새 스코프를 만들면 stopRefreshAd 가 취소하지 못하는
    // Job 이 남는다.
    private val refreshScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 종횡비를 특정하면 매칭되는 소재가 줄어 fill 이 떨어진다. 미디어 영역은 고정 높이 박스이고
    // MediaView 가 어떤 비율이든 레터박스로 맞추므로 제약을 걸지 않는다.
    private val nativeAdOptions =
        NativeAdOptions
            .Builder()
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .build()

    override suspend fun load(): PlatformNativeAd? {
        val nativeAdId = getString(Res.string.admob_native_ad_id)
        return withContext(Dispatchers.IO) {
            suspendCoroutine {
                val adLoader =
                    AdLoader
                        .Builder(
                            context,
                            nativeAdId,
                        ).forNativeAd { nativeAd ->
                            val platformAd = PlatformNativeAd(nativeAd)
                            cachedAd = platformAd
                            lastLoadTime = TimeProvider.currentTimeMillis()
                            it.resume(platformAd)
                        }.withAdListener(
                            object : AdListener() {
                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    it.resume(null)
                                }
                            },
                        ).withNativeAdOptions(nativeAdOptions)
                        .build()
                adLoader.loadAd(AdRequest.Builder().build())
            }
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
