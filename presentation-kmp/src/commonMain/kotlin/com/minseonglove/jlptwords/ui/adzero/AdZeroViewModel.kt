package com.minseonglove.jlptwords.ui.adzero

import com.minseonglove.jlptwords.ad.PlatformFullScreenContentCallback
import com.minseonglove.jlptwords.ad.PlatformRewardedAd
import com.minseonglove.jlptwords.ad.RewardedAdManager
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.AddAdZeroTime
import com.minseonglove.jlptwords.usecase.GetAdZeroTime
import com.minseonglove.jlptwords.usecase.GetRealTimeMillis
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.adzero_load_failed
import jlptwords.presentation_kmp.generated.resources.adzero_reward_success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.orbitmvi.orbit.Container
import kotlin.math.ceil
import kotlin.math.roundToInt

class AdZeroViewModel(
    val rewardedAdManager: RewardedAdManager,
    val addAdZeroTime: AddAdZeroTime,
    val getAdZeroTime: GetAdZeroTime,
    val getRealTimeMillis: GetRealTimeMillis,
) : BaseViewModel<AdZeroState, AdZeroSideEffect>() {
    override val container: Container<AdZeroState, AdZeroSideEffect> =
        container(
            initialState = AdZeroState(),
        )

    // 버퍼 1 + tryEmit: 다이얼로그가 먼저 닫혀 수집자가 없을 때 emit 이 영구 정지(코루틴 누수)하는 것을 막는다.
    private val _adEvent = MutableSharedFlow<PlatformRewardedAd>(extraBufferCapacity = 1)
    val adEvent = _adEvent.asSharedFlow()

    private val fullScreenContentCallback =
        object : PlatformFullScreenContentCallback {
            override fun onAdShowedFullScreenContent() {
                onAdShowed()
            }

            override fun onAdFailedToShowFullScreenContent() {
                onAdLoadFailed()
            }

            override fun onAdDismissedFullScreenContent() {
                // Do Nothing
            }
        }

    fun onCreate() {
        AdZeroIntent.Initialize.post()
    }

    fun onAdZeroButtonClick() {
        AdZeroIntent.ShowRewardedAd.post()
    }

    fun onCancelButtonClick() {
        AdZeroIntent.RequestDismiss.post()
    }

    fun onUserEarnedReward() {
        AdZeroIntent.AddAdZeroTime.post()
    }

    fun onAdLoadFailed() {
        AdZeroIntent.ShowLoadFailedMessage.post()
    }

    fun onAdShowed() {
        AdZeroIntent.EnableAdZeroButton.post()
    }

    private fun AdZeroIntent.post() =
        intent {
            when (this@post) {
                AdZeroIntent.Initialize -> {
                    val currentAdZeroTime = getAdZeroTime()
                    val leftTimeMin = getAdZeroLeftTimeMin(currentAdZeroTime)
                    reduce {
                        state.copy(
                            adZeroLeftTimeMin = leftTimeMin,
                        )
                    }
                }

                AdZeroIntent.ShowRewardedAd -> {
                    reduce {
                        state.copy(
                            isAdLoading = true,
                        )
                    }

                    val rewardedAd = rewardedAdManager.getValidAd()

                    if (rewardedAd != null) {
                        rewardedAd.setFullScreenContentCallback(fullScreenContentCallback)
                        _adEvent.tryEmit(rewardedAd)
                        // 리워드 광고는 1회용이라 노출 즉시 캐시에서 비운다.
                        // (보상 없이 닫거나 노출에 실패한 광고가 재사용돼 재노출이 계속 실패하는 문제 방지)
                        rewardedAdManager.clearAd()
                    } else {
                        postSideEffect(
                            AdZeroSideEffect.ShowMessage(Res.string.adzero_load_failed),
                        )
                    }
                    reduce {
                        state.copy(
                            isAdLoading = false,
                        )
                    }
                }

                AdZeroIntent.RequestDismiss -> {
                    postSideEffect(AdZeroSideEffect.RequestDismiss)
                }

                is AdZeroIntent.ShowLoadFailedMessage -> {
                    postSideEffect(
                        AdZeroSideEffect.ShowMessage(Res.string.adzero_load_failed),
                    )
                    reduce {
                        state.copy(
                            isAdLoading = false,
                        )
                    }
                }

                AdZeroIntent.AddAdZeroTime -> {
                    rewardedAdManager.clearAd()
                    val currentAdZeroTime =
                        addAdZeroTime(
                            time = AD_ZERO_TIME_FOR_REWARD,
                        )
                    val leftTimeMin = getAdZeroLeftTimeMin(currentAdZeroTime)
                    reduce {
                        state.copy(
                            adZeroLeftTimeMin = leftTimeMin,
                        )
                    }
                    postSideEffect(
                        AdZeroSideEffect.ShowMessage(
                            messageRes = Res.string.adzero_reward_success,
                        ),
                    )
                }

                AdZeroIntent.EnableAdZeroButton -> {
                    reduce {
                        state.copy(
                            isAdLoading = false,
                        )
                    }
                }
            }
        }

    private suspend fun getAdZeroLeftTimeMin(adZeroTime: Long): Int {
        // 시간을 가져오는데 실패 했으면 남은 시간을 안보여준다.
        val realTimeMillis = getRealTimeMillis() ?: return 0
        return ceil((adZeroTime - realTimeMillis) / 60000f).roundToInt().coerceAtLeast(0)
    }

    companion object {
        private const val AD_ZERO_TIME_FOR_REWARD = 24 * 60 * 60 * 1000L
    }
}
