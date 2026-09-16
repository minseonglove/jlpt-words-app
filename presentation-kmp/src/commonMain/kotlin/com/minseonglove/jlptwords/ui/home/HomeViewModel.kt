package com.minseonglove.jlptwords.ui.home

import com.minseonglove.jlptwords.ad.NativeAdManager
import com.minseonglove.jlptwords.ad.TrackingConsentManager
import com.minseonglove.jlptwords.entity.ContinueSessionResult
import com.minseonglove.jlptwords.entity.ContinueStudySession
import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyOverview
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.ui.exit.ExitAdState
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.GetContinueSession
import com.minseonglove.jlptwords.usecase.GetLastSelectedLevel
import com.minseonglove.jlptwords.usecase.GetStudyOverview
import com.minseonglove.jlptwords.usecase.GetTodayExample
import com.minseonglove.jlptwords.usecase.InitializeWordsIfEmpty
import com.minseonglove.jlptwords.usecase.IsAdZeroEnabled
import com.minseonglove.jlptwords.usecase.SetLastSelectedLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax

class HomeViewModel(
    private val getLastSelectedLevel: GetLastSelectedLevel,
    private val setLastSelectedLevel: SetLastSelectedLevel,
    private val initializeWordsIfEmpty: InitializeWordsIfEmpty,
    private val getTodayExample: GetTodayExample,
    private val getContinueSession: GetContinueSession,
    private val getStudyOverview: GetStudyOverview,
    private val getAdZeroRemainingHours: GetAdZeroRemainingHours,
    private val isAdZeroEnabled: IsAdZeroEnabled,
    private val nativeAdManager: NativeAdManager,
    private val trackingConsentManager: TrackingConsentManager,
) : BaseViewModel<HomeState, HomeSideEffect>() {
    override val container: Container<HomeState, HomeSideEffect> =
        container(HomeState())

    /**
     * 이번 ViewModel 생명주기 안에서 단어 초기화를 마친 급수.
     * 홈은 백스택 복귀 때마다 Initialize 가 다시 돌므로, 급수당 1회만 원격 동기화를 시도한다
     * (실패한 급수는 기록하지 않아 재시도 버튼에서 다시 시도된다).
     */
    private val initializedLevels = mutableSetOf<JLPTLevel>()

    fun onCreate() {
        HomeIntent.Initialize.post()
    }

    fun onTodayExampleClick() {
        HomeIntent.ClickTodayExample.post()
    }

    fun onContinueSessionClick() {
        HomeIntent.ClickContinueSession.post()
    }

    fun onTitleClick() {
        HomeIntent.ClickTitle.post()
    }

    fun onAdZeroClick() {
        HomeIntent.ShowAdZeroDialog.post()
    }

    fun onAdZeroDialogDismiss() {
        HomeIntent.DismissAdZeroDialog.post()
    }

    fun onSettingsClick() {
        HomeIntent.ClickSettings.post()
    }

    fun onBack() {
        HomeIntent.ShowExitAppBottomSheet.post()
    }

    fun onExitAppBottomSheetDismiss() {
        HomeIntent.DismissExitAppBottomSheet.post()
    }

    fun onExitAppButtonClick() {
        HomeIntent.FinishApp.post()
    }

    fun onRetryButtonClick() {
        HomeIntent.Initialize.post()
    }

    private fun HomeIntent.post() =
        intent {
            when (this@post) {
                HomeIntent.Initialize -> initialize()

                HomeIntent.ClickTodayExample -> {
                    val todayExample = state.todayExample
                    if (todayExample != null) {
                        postSideEffect(
                            HomeSideEffect.NavigateToWordDetail(
                                kanji = todayExample.kanji,
                                pronunciation = todayExample.pronunciation,
                            ),
                        )
                    }
                }

                HomeIntent.ClickContinueSession -> {
                    val continueSession = state.continueSession
                    if (continueSession != null) {
                        // 다음 급수의 세션으로 넘어가는 경우 '학습 중인 급수'도 함께 갱신한다.
                        if (continueSession.session.level != state.level) {
                            setLastSelectedLevel(continueSession.session.level)
                        }
                        postSideEffect(
                            HomeSideEffect.NavigateToStudy(
                                sessionId = continueSession.session.id,
                                sessionIndex = continueSession.sessionIndex,
                            ),
                        )
                    }
                }

                HomeIntent.ClickTitle -> {
                    postSideEffect(HomeSideEffect.NavigateToLevelSelection(state.level))
                }

                HomeIntent.ShowAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = true,
                        )
                    }
                }

                HomeIntent.DismissAdZeroDialog -> {
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

                HomeIntent.ClickSettings -> {
                    postSideEffect(HomeSideEffect.NavigateToSetting)
                }

                HomeIntent.ShowExitAppBottomSheet -> showExitAppBottomSheet()

                HomeIntent.DismissExitAppBottomSheet -> {
                    reduce {
                        state.copy(
                            isExitAppBottomSheetShown = false,
                        )
                    }
                }

                HomeIntent.FinishApp -> {
                    postSideEffect(HomeSideEffect.FinishApp)
                }
            }
        }

    private suspend fun Syntax<HomeState, HomeSideEffect>.initialize() {
        // 첫 로드(또는 재시도)에만 전체 로딩을 보여주고, 복귀 시에는 기존 내용을 유지한 채 갱신한다.
        val isSilentRefresh = state.todayExample != null || state.continueSession != null
        if (isSilentRefresh.not()) {
            reduce {
                state.copy(
                    isLoading = true,
                    isRetryScreenVisible = false,
                )
            }
        }

        val level = getLastSelectedLevel() ?: JLPTLevel.N3
        // 현재 급수의 단어·예문이 없으면 받아온다(첫 설치 등).
        initializeWordsIfNeeded(level)

        val homeData = loadHomeData(level)
        if (homeData.todayExample == null && homeData.continueSession == null) {
            reduce {
                state.copy(
                    level = level,
                    adZeroRemainingHours = homeData.adZeroRemainingHours,
                    isLoading = false,
                    isRetryScreenVisible = true,
                )
            }
        } else {
            reduce {
                state.copy(
                    level = level,
                    todayExample = homeData.todayExample,
                    continueSession = homeData.continueSession,
                    studyOverview = homeData.studyOverview,
                    adZeroRemainingHours = homeData.adZeroRemainingHours,
                    isLoading = false,
                    isRetryScreenVisible = false,
                )
            }
        }

        // 홈 내용이 그려진 뒤 요청해야 빈 화면 위에 팝업이 뜨지 않는다.
        trackingConsentManager.requestAuthorizationIfNeeded()
    }

    private suspend fun Syntax<HomeState, HomeSideEffect>.showExitAppBottomSheet() {
        reduce {
            state.copy(
                isExitAppBottomSheetShown = true,
            )
        }

        val exitAdState =
            if (isAdZeroEnabled()) {
                ExitAdState.AdZero
            } else {
                val nativeAd = nativeAdManager.getValidAd()
                if (nativeAd != null) {
                    ExitAdState.AdReady(nativeAd = nativeAd)
                } else {
                    ExitAdState.AdZero
                }
            }

        reduce {
            state.copy(
                exitAdState = exitAdState,
            )
        }
    }

    private suspend fun loadHomeData(
        level: JLPTLevel,
    ): HomeData =
        withContext(Dispatchers.IO) {
            val todayExampleDeferred =
                async {
                    getTodayExample(level)
                }
            val continueSessionDeferred =
                async {
                    resolveContinueSession(level)
                }
            val studyOverviewDeferred =
                async {
                    getStudyOverview(level)
                }
            val adZeroRemainingHoursDeferred =
                async {
                    getAdZeroRemainingHours()
                }
            HomeData(
                todayExample = todayExampleDeferred.await(),
                continueSession = continueSessionDeferred.await(),
                studyOverview = studyOverviewDeferred.await(),
                adZeroRemainingHours = adZeroRemainingHoursDeferred.await(),
            )
        }

    /**
     * 계속 학습할 세션을 결정한다. 대상 급수의 세션이 아직 생성되지 않았다면
     * (다음 급수로 처음 넘어가는 경우 등) 해당 급수를 초기화한 뒤 한 번 더 시도한다.
     */
    private suspend fun resolveContinueSession(
        currentLevel: JLPTLevel,
    ): ContinueStudySession? {
        val result = getContinueSession(currentLevel)
        val requiredLevel =
            when (result) {
                is ContinueSessionResult.Ready -> return result.item
                is ContinueSessionResult.RequiresInitialization -> result.level
            }
        initializeWordsIfNeeded(requiredLevel)
        return (getContinueSession(currentLevel) as? ContinueSessionResult.Ready)?.item
    }

    /** 급수당 1회만 단어 초기화를 시도한다. 실패(ERROR)한 급수는 기록하지 않아 다음에 다시 시도된다. */
    private suspend fun initializeWordsIfNeeded(
        level: JLPTLevel,
    ) {
        if (level in initializedLevels) return
        val progress =
            initializeWordsIfEmpty
                .invoke(level)
                .first { it.isTerminal }
        if (progress != WordsInitializeProgress.ERROR) {
            initializedLevels += level
        }
    }

    private data class HomeData(
        val todayExample: DailyExample?,
        val continueSession: ContinueStudySession?,
        val studyOverview: StudyOverview?,
        val adZeroRemainingHours: Int,
    )
}
