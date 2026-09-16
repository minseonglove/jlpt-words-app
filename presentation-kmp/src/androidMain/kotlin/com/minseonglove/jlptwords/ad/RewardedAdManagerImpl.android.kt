package com.minseonglove.jlptwords.ad

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.minseonglove.jlptwords.usecase.GetRealTimeMillis
import com.minseonglove.jlptwords.util.TimeProvider
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.admob_rewarded_ad_id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class RewardedAdManagerImpl(
    private val context: Context,
    private val getRealTimeMillis: GetRealTimeMillis,
) : RewardedAdManager {
    private var cachedAd: PlatformRewardedAd? = null
    private var lastLoadTime: Long = 0L

    override suspend fun load(): PlatformRewardedAd? =
        withContext(Dispatchers.Main) {
            val realTimeMillis = getRealTimeMillis() ?: TimeProvider.currentTimeMillis()
            val rewardedAdId = getString(Res.string.admob_rewarded_ad_id)
            suspendCoroutine {
                RewardedAd.load(
                    context,
                    rewardedAdId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            lastLoadTime = realTimeMillis
                            val platformAd = PlatformRewardedAd(ad)
                            cachedAd = platformAd
                            it.resume(platformAd)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            it.resume(null)
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
