package com.minseonglove.jlptwords.ui.splash

import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.GetLastSelectedLevel
import com.minseonglove.jlptwords.usecase.InitializeAllContents
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.Container

class SplashViewModel(
    private val getLastSelectedLevel: GetLastSelectedLevel,
    private val initializeAllContents: InitializeAllContents,
) : BaseViewModel<SplashState, SplashSideEffect>() {
    override val container: Container<SplashState, SplashSideEffect> =
        container(
            SplashState(),
        )

    /**
     * 초기화가 진행 중인지. 재시도 버튼을 연타하면 초기화가 겹쳐 실행되고, 각각이 화면 전환을
     * 요청해 같은 화면이 백스택에 여러 번 쌓인다.
     */
    private var isInitializing = false

    fun onCreate() {
        SplashIntent.Initialize.post()
    }

    fun onRetry() {
        SplashIntent.Initialize.post()
    }

    private fun SplashIntent.post() =
        intent {
            when (this@post) {
                SplashIntent.Initialize -> {
                    if (isInitializing) return@intent
                    isInitializing = true
                    try {
                        reduce {
                            state.copy(
                                progress = WordsInitializeProgress.UPDATING,
                                syncProgress = null,
                            )
                        }
                        coroutineScope {
                            val lastSelectedLevelDeferred =
                                async {
                                    getLastSelectedLevel.invoke()
                                }
                            val waitDeferred =
                                async {
                                    delay(750)
                                }
                            // 전 급수 단어·예문·사전을 보장한다(검색·단어 상세의 급수 무관 조회용).
                            // 이미 최신이면 날짜 확인만으로 즉시 통과한다.
                            // 단계 진행 상태는 로딩 문구로 노출하고, 종결 원소에서 결과를 꺼낸다.
                            val syncResultDeferred =
                                async {
                                    initializeAllContents
                                        .invoke()
                                        .onEach { syncProgress ->
                                            if (syncProgress !is AllContentsSyncProgress.Finished) {
                                                reduce {
                                                    state.copy(
                                                        syncProgress =
                                                            selectDisplayProgress(
                                                                current = state.syncProgress,
                                                                incoming = syncProgress,
                                                            ),
                                                    )
                                                }
                                            }
                                        }.filterIsInstance<AllContentsSyncProgress.Finished>()
                                        .first()
                                        .result
                                }
                            val lastSelectedLevel = lastSelectedLevelDeferred.await()
                            val syncResult = syncResultDeferred.await()
                            waitDeferred.await()

                            if (!syncResult.isReady) {
                                // 핵심 데이터 미확보(첫 실행 + 오프라인 등) — 재시도 안내로 전환한다.
                                reduce {
                                    state.copy(
                                        progress = WordsInitializeProgress.ERROR,
                                    )
                                }
                            } else if (lastSelectedLevel != null) {
                                postSideEffect(SplashSideEffect.NavigateToHome)
                            } else {
                                // 완전 첫 실행은 급수 선택부터 시작한다. 초기 선택 값은 N3.
                                postSideEffect(
                                    SplashSideEffect.NavigateToLevelSelection(
                                        level = JLPTLevel.N3,
                                    ),
                                )
                            }
                        }
                    } finally {
                        isInitializing = false
                    }
                }
            }
        }

    /**
     * 동기화 브랜치들이 병렬이라 방출 순서가 뒤섞인다. 진행률을 보여주는 긴 작업(단어·예문)이
     * 표시 중일 때 짧은 작업(요미·한자) 문구가 그것을 덮으면 긴 대기의 진행 상황이 가려지므로,
     * 그 경우에만 기존 표시를 유지한다.
     */
    private fun selectDisplayProgress(
        current: AllContentsSyncProgress?,
        incoming: AllContentsSyncProgress,
    ): AllContentsSyncProgress =
        when {
            incoming is AllContentsSyncProgress.Words || incoming is AllContentsSyncProgress.Examples -> incoming
            current is AllContentsSyncProgress.Words || current is AllContentsSyncProgress.Examples -> current
            else -> incoming
        }
}
