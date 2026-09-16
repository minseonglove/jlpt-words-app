package com.minseonglove.jlptwords.ui.setting

import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.GetAppVersionName
import com.minseonglove.jlptwords.usecase.GetInquiryInfo
import com.minseonglove.jlptwords.usecase.GetSettingPreferences
import com.minseonglove.jlptwords.usecase.SetJapaneseLanguageEnabled
import com.minseonglove.jlptwords.usecase.SetStudyNotificationEnabled
import com.minseonglove.jlptwords.usecase.SetStudyNotificationTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax

class SettingViewModel(
    private val getAppVersionName: GetAppVersionName,
    private val getInquiryInfo: GetInquiryInfo,
    private val getSettingPreferences: GetSettingPreferences,
    private val setStudyNotificationEnabled: SetStudyNotificationEnabled,
    private val setStudyNotificationTime: SetStudyNotificationTime,
    private val setJapaneseLanguageEnabled: SetJapaneseLanguageEnabled,
    private val getAdZeroRemainingHours: GetAdZeroRemainingHours,
) : BaseViewModel<SettingState, SettingSideEffect>() {
    override val container: Container<SettingState, SettingSideEffect> =
        container(SettingState())

    fun onCreate() {
        SettingIntent.Initialize.post()
    }

    fun onStudyNotificationToggle() {
        SettingIntent.ToggleStudyNotification.post()
    }

    fun onNotificationTimeClick() {
        SettingIntent.ShowNotificationTimePicker.post()
    }

    fun onNotificationTimeChange(
        hour: Int,
        minute: Int,
    ) {
        SettingIntent
            .ChangeNotificationTime(
                hour = hour,
                minute = minute,
            ).post()
    }

    fun onNotificationTimePickerDismiss() {
        SettingIntent.DismissNotificationTimePicker.post()
    }

    fun onJapaneseLanguageToggle() {
        SettingIntent.ToggleJapaneseLanguage.post()
    }

    fun onInquiryClick() {
        SettingIntent.OpenInquiry.post()
    }

    fun onPrivacyPolicyClick() {
        SettingIntent.OpenPrivacyPolicy.post()
    }

    fun onOpenSourceLicensesClick() {
        SettingIntent.OpenOpenSourceLicenses.post()
    }

    fun onAdZeroClick() {
        SettingIntent.ShowAdZeroDialog.post()
    }

    fun onAdZeroDialogDismiss() {
        SettingIntent.DismissAdZeroDialog.post()
    }

    fun onBackClick() {
        SettingIntent.NavigateBack.post()
    }

    private fun SettingIntent.post() =
        intent {
            when (this@post) {
                SettingIntent.Initialize -> initialize()

                SettingIntent.ToggleStudyNotification -> {
                    val enabled = state.isStudyNotificationEnabled.not()
                    reduce {
                        state.copy(
                            isStudyNotificationEnabled = enabled,
                        )
                    }
                    setStudyNotificationEnabled(enabled)
                    // TODO: 학습 알림 기능 구현 시 토글 상태에 따라 알림 스케줄 등록/해제
                }

                is SettingIntent.ChangeNotificationTime -> {
                    reduce {
                        state.copy(
                            notificationHour = hour,
                            notificationMinute = minute,
                            isNotificationTimePickerShown = false,
                        )
                    }
                    setStudyNotificationTime(
                        hour = hour,
                        minute = minute,
                    )
                    // TODO: 학습 알림 기능 구현 시 변경된 시간으로 알림 스케줄 재등록
                }

                SettingIntent.ShowNotificationTimePicker -> {
                    reduce {
                        state.copy(
                            isNotificationTimePickerShown = true,
                        )
                    }
                }

                SettingIntent.DismissNotificationTimePicker -> {
                    reduce {
                        state.copy(
                            isNotificationTimePickerShown = false,
                        )
                    }
                }

                SettingIntent.ToggleJapaneseLanguage -> {
                    val enabled = state.isJapaneseLanguageEnabled.not()
                    reduce {
                        state.copy(
                            isJapaneseLanguageEnabled = enabled,
                        )
                    }
                    // 저장하면 AppNavHost 가 관찰 중인 Flow 로 전파돼 앱 언어가 전환된다.
                    setJapaneseLanguageEnabled(enabled)
                }

                SettingIntent.OpenInquiry -> {
                    postSideEffect(
                        SettingSideEffect.OpenInquiry(
                            inquiryInfo = getInquiryInfo(),
                        ),
                    )
                }

                SettingIntent.OpenPrivacyPolicy -> {
                    postSideEffect(SettingSideEffect.OpenPrivacyPolicy)
                }

                SettingIntent.OpenOpenSourceLicenses -> {
                    postSideEffect(SettingSideEffect.OpenOpenSourceLicenses)
                }

                SettingIntent.ShowAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = true,
                        )
                    }
                }

                SettingIntent.DismissAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = false,
                        )
                    }
                    // 광고 제거가 새로 적용됐을 수 있으니 상단바 남은 시간을 갱신한다.
                    val adZeroRemainingHours = getAdZeroRemainingHours()
                    reduce {
                        state.copy(
                            adZeroRemainingHours = adZeroRemainingHours,
                        )
                    }
                }

                SettingIntent.NavigateBack -> {
                    postSideEffect(SettingSideEffect.NavigateToBack)
                }
            }
        }

    private suspend fun Syntax<SettingState, SettingSideEffect>.initialize() {
        val initializeData =
            withContext(Dispatchers.IO) {
                val versionDeferred = async { getAppVersionName() ?: "" }
                val preferencesDeferred = async { getSettingPreferences() }
                val adZeroRemainingHoursDeferred = async { getAdZeroRemainingHours() }
                Triple(
                    versionDeferred.await(),
                    preferencesDeferred.await(),
                    adZeroRemainingHoursDeferred.await(),
                )
            }
        val (version, preferences, adZeroRemainingHours) = initializeData
        reduce {
            state.copy(
                version = version,
                adZeroRemainingHours = adZeroRemainingHours,
                isStudyNotificationEnabled = preferences.isStudyNotificationEnabled,
                notificationHour = preferences.notificationHour,
                notificationMinute = preferences.notificationMinute,
                isJapaneseLanguageEnabled = preferences.isJapaneseLanguageEnabled,
            )
        }
    }
}
