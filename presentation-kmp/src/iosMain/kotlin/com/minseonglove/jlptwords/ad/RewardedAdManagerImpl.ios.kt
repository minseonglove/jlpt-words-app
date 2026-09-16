package com.minseonglove.jlptwords.ad

import com.minseonglove.jlptwords.usecase.GetRealTimeMillis
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.admob_ios_rewarded_ad_id
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import platform.posix.time
import swiftPMImport.jlpt.words.presentation.kmp.GADRequest
import swiftPMImport.jlpt.words.presentation.kmp.GADRewardedAd
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class RewardedAdManagerImpl(
    private val getRealTimeMillis: GetRealTimeMillis,
) : RewardedAdManager {
    private var cachedAd: PlatformRewardedAd? = null
    private var lastLoadTime: Long = 0L

    override suspend fun load(): PlatformRewardedAd? =
        withContext(Dispatchers.Main) {
            val rewardedAdId = getString(Res.string.admob_ios_rewarded_ad_id)
            val realTimeMillis = getRealTimeMillis() ?: (time(null) * 1000L)
            suspendCancellableCoroutine { continuation ->
                GADRewardedAd.loadWithAdUnitID(
                    adUnitID = rewardedAdId,
                    request = GADRequest(),
                    completionHandler = { ad, error ->
                        if (!continuation.isActive) return@loadWithAdUnitID
                        if (ad != null) {
                            lastLoadTime = realTimeMillis
                            val platformAd = PlatformRewardedAd(ad)
                            cachedAd = platformAd
                            continuation.resume(platformAd)
                        } else {
                            continuation.resume(null)
                        }
                    },
                )
            }
        }

    private suspend fun isAdValid(): Boolean {
        val currentTime = getRealTimeMillis() ?: return false
        val timeSinceLoad = currentTime - lastLoadTime

        return timeSinceLoad < AD_VALIDITY_DURATION
    }

    override suspend fun getValidAd(): PlatformRewardedAd? {
        return if (cachedAd != null && isAdValid()) {
            cachedAd
        } else {
            load()
        }
    }

    override fun clearAd() {
        cachedAd = null
    }

    companion object {
        private const val AD_VALIDITY_DURATION = 55 * 60 * 1000L
    }
}
