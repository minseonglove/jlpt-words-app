package com.minseonglove.jlptwords.ui.selection.session

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyRecord
import com.minseonglove.jlptwords.entity.StudySession
import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.ui.study.StudySessionCardItem
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.GetStudyRecordsByLevel
import com.minseonglove.jlptwords.usecase.GetStudySessionsByLevel
import com.minseonglove.jlptwords.usecase.GetStudyStatus
import com.minseonglove.jlptwords.usecase.InitializeWordsIfEmpty
import com.minseonglove.jlptwords.usecase.ObserveLastSelectedLevel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax

class SessionSelectionViewModel(
    private val observeLastSelectedLevel: ObserveLastSelectedLevel,
    private val getStudySessionsByLevel: GetStudySessionsByLevel,
    private val getStudyRecordsByLevel: GetStudyRecordsByLevel,
    private val getStudyStatus: GetStudyStatus,
    private val getAdZeroRemainingHours: GetAdZeroRemainingHours,
    private val initializeWordsIfEmpty: InitializeWordsIfEmpty,
) : BaseViewModel<SessionSelectionState, SessionSelectionSideEffect>() {
    override val container: Container<SessionSelectionState, SessionSelectionSideEffect> =
        container(SessionSelectionState())

    init {
        observeLevelChanges()
    }

    /**
     * 급수는 preference 가 단일 소스다. 최초 방출이 첫 로드를 수행하고,
     * 이후 값이 바뀌면(급수 선택 완료 등) 새 급수로 전체 리로드한다.
     */
    private fun observeLevelChanges() {
        intent {
            observeLastSelectedLevel()
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { level ->
                    reduce { state.copy(level = level) }
                    loadSessions(level = level, isSilentRefresh = false)
                }
        }
    }

    fun onCreate() {
        SessionSelectionIntent.Initialize.post()
    }

    fun onAdZeroItemClick() {
        SessionSelectionIntent.ShowAdZeroDialog.post()
    }

    fun onAdZeroDialogDismiss() {
        SessionSelectionIntent.DismissAdZeroDialog.post()
    }

    fun onSessionWarningDialogDismiss() {
        SessionSelectionIntent.DismissSessionWarningDialog.post()
    }

    fun onSessionWarningDialogConfirmClick(
        selectedSessionId: Int,
    ) {
        SessionSelectionIntent
            .NavigationToStudyScreen(
                sessionId = selectedSessionId,
            ).post()
    }

    fun onBack() {
        SessionSelectionIntent.NavigateBack.post()
    }

    fun onRetryButtonClick() {
        SessionSelectionIntent.Retry.post()
    }

    fun onMoreButtonClick() {
        SessionSelectionIntent.ClickSettings.post()
    }

    fun onChapterHeaderClick(
        chapterNumber: Int,
    ) = SessionSelectionIntent.ToggleChapter(chapterNumber).post()

    fun onStackedCardsClick(
        chapterNumber: Int,
    ) = SessionSelectionIntent.ToggleChapter(chapterNumber).post()

    fun onCollapseAnimationCompleted() = SessionSelectionIntent.CollapseAnimationCompleted.post()

    fun onSessionCardClick(
        sessionId: Int,
    ) = SessionSelectionIntent.SelectSession(sessionId).post()

    private fun SessionSelectionIntent.post() =
        intent {
            when (this@post) {
                is SessionSelectionIntent.Initialize -> {
                    val level = state.level ?: return@intent
                    // 첫 로드·급수 변경 로드는 observeLevelChanges 가 소유한다. 복귀 시에는
                    // 이미 로드된 콘텐츠의 조용한 갱신만 수행해 이중 로드를 막는다.
                    if (state.chapters.isEmpty()) return@intent
                    loadSessions(level = level, isSilentRefresh = true)
                }

                is SessionSelectionIntent.Retry -> {
                    val level = state.level ?: return@intent
                    loadSessions(level = level, isSilentRefresh = false)
                }

                is SessionSelectionIntent.SelectSession -> selectSession(sessionId = sessionId)

                is SessionSelectionIntent.NavigationToStudyScreen -> navigateToStudyScreen(sessionId)

                SessionSelectionIntent.ShowAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = true,
                        )
                    }
                }

                SessionSelectionIntent.DismissAdZeroDialog -> {
                    reduce {
                        state.copy(
                            isAdZeroDialogShown = false,
                        )
                    }
                    // 리워드 광고 시청 후 광고 제거 시간이 갱신될 수 있어 다이얼로그를 닫을 때 다시 조회한다.
                    val adZeroRemainingHours = getAdZeroRemainingHours()
                    reduce {
                        state.copy(
                            adZeroRemainingHours = adZeroRemainingHours,
                        )
                    }
                }

                SessionSelectionIntent.NavigateBack -> {
                    postSideEffect(SessionSelectionSideEffect.NavigateToBack)
                }

                SessionSelectionIntent.DismissSessionWarningDialog -> {
                    reduce {
                        state.copy(
                            sessionWarning = null,
                        )
                    }
                }

                SessionSelectionIntent.ClickSettings -> {
                    postSideEffect(SessionSelectionSideEffect.NavigateToSetting)
                }

                is SessionSelectionIntent.ToggleChapter ->
                    reduce {
                        when {
                            chapterNumber in state.expandedChapters ->
                                state.copy(
                                    expandedChapters = state.expandedChapters.removing(chapterNumber),
                                    pendingExpandChapter = null,
                                )

                            state.expandedChapters.isNotEmpty() ->
                                state.copy(
                                    expandedChapters = persistentSetOf(),
                                    pendingExpandChapter = chapterNumber,
                                )

                            else ->
                                state.copy(
                                    expandedChapters = persistentSetOf(chapterNumber),
                                    pendingExpandChapter = null,
                                )
                        }
                    }

                SessionSelectionIntent.CollapseAnimationCompleted -> {
                    val pending = state.pendingExpandChapter ?: return@intent
                    reduce {
                        state.copy(
                            expandedChapters = persistentSetOf(pending),
                            pendingExpandChapter = null,
                        )
                    }
                }
            }
        }

    private suspend fun Syntax<SessionSelectionState, SessionSelectionSideEffect>.selectSession(
        sessionId: Int,
    ) {
        val studyStatus = state.lastStudyStatus
        if (studyStatus == null || studyStatus.sessionId == sessionId) {
            navigateToStudyScreen(sessionId)
            return
        }
        // 경고 문구에 필요한 값은 여기서 확정한다 (진행 중 세션의 위치·진행률).
        val position = state.chapters.findSessionPosition(studyStatus.sessionId)
        reduce {
            state.copy(
                sessionWarning =
                    SessionWarning(
                        selectedSessionId = sessionId,
                        level = studyStatus.level,
                        chapterNumber = position?.chapterNumber ?: 1,
                        sessionNumber =
                            (position?.indexInChapter ?: studyStatus.sessionPosition) + 1,
                        progressPercent = studyStatus.progressPercent(),
                    ),
            )
        }
    }

    private suspend fun Syntax<SessionSelectionState, SessionSelectionSideEffect>.navigateToStudyScreen(
        sessionId: Int,
    ) {
        reduce {
            state.copy(
                sessionWarning = null,
            )
        }
        val sessionIndex = state.chapters.findSessionPosition(sessionId)?.globalIndex ?: -1
        postSideEffect(
            SessionSelectionSideEffect.NavigationToStudyScreen(
                sessionId = sessionId,
                sessionIndex = sessionIndex,
            ),
        )
    }

    private fun StudySession.toStudySessionCardItem(
        completedSessionRecord: StudyRecord?,
        allRecords: List<StudyRecord>,
        lastStudyStatus: StudyStatus?,
    ): StudySessionCardItem {
        val isInProgress = lastStudyStatus?.sessionId == id
        val speed = hasSpeedStamp(allRecords, wordsSize)
        val accurate = hasAccurateStamp(allRecords)
        val main = mainStampOf(isInProgress = isInProgress, hasCompletedRecord = completedSessionRecord != null)

        return when {
            isInProgress -> {
                val accuracy =
                    (lastStudyStatus.totalKnownWordIds.size * 100) /
                        lastStudyStatus.totalAppearanceCount.coerceAtLeast(1)

                StudySessionCardItem(
                    id = id,
                    startNumber = startNumber,
                    endNumber = endNumber,
                    studyProgressState = StudyProgressState.IN_PROGRESS,
                    progress = lastStudyStatus.progressPercent(),
                    accuracy = accuracy,
                    elapsedTimeSeconds = lastStudyStatus.totalElapsedTimeSeconds,
                    completionCount = allRecords.size + 1, // 현재 진행 중인 학습도 카운트 침
                    hasSpeedStamp = speed,
                    hasAccurateStamp = accurate,
                    mainStamp = main,
                )
            }

            completedSessionRecord != null -> {
                StudySessionCardItem(
                    id = id,
                    startNumber = startNumber,
                    endNumber = endNumber,
                    studyProgressState = StudyProgressState.COMPLETED,
                    progress = 100,
                    accuracy = completedSessionRecord.accuracy,
                    elapsedTimeSeconds = completedSessionRecord.completionTimeSeconds,
                    completionCount = allRecords.size,
                    hasSpeedStamp = speed,
                    hasAccurateStamp = accurate,
                    mainStamp = main,
                )
            }

            else -> {
                StudySessionCardItem(
                    id = id,
                    startNumber = startNumber,
                    endNumber = endNumber,
                    studyProgressState = StudyProgressState.NOT_STARTED,
                    progress = 0,
                    accuracy = 0,
                    elapsedTimeSeconds = 0,
                    completionCount = 0,
                    hasSpeedStamp = false,
                    hasAccurateStamp = false,
                    mainStamp = MainStamp.NONE,
                )
            }
        }
    }

    private suspend fun Syntax<SessionSelectionState, SessionSelectionSideEffect>.loadSessions(
        level: JLPTLevel,
        isSilentRefresh: Boolean,
    ) {
        if (isSilentRefresh.not()) {
            reduce {
                state.copy(
                    isRetryScreenVisible = false,
                    isLoading = true,
                )
            }

            // 원격 콘텐츠 확인(날짜 비교)은 첫 로드·급수 변경·재시도에서만 수행한다.
            // 복귀 시의 조용한 갱신은 로컬 DB 재조회만 한다 (탭 왕복마다 원격 왕복 방지).
            initializeWordsIfEmpty
                .invoke(level)
                .first { it.isTerminal }
        }
        val initializeData = getInitializeData(level)

        // 세션 별 가장 최근에 완료한 기록들
        val lastCompletedSessions =
            initializeData.allCompletedSessions
                .mapNotNull { (_, records) -> records.maxByOrNull { it.createAt } }
                .associateBy { it.sessionId }

        val items =
            initializeData.studySessions.map { studySession ->
                studySession.toStudySessionCardItem(
                    completedSessionRecord = lastCompletedSessions[studySession.id],
                    allRecords = initializeData.allCompletedSessions[studySession.id].orEmpty(),
                    lastStudyStatus = initializeData.studyStatus,
                )
            }

        if (items.isEmpty()) {
            // 조용한 갱신 실패 시에는 보고 있던 콘텐츠를 유지한다.
            if (isSilentRefresh.not()) {
                reduce {
                    state.copy(
                        isRetryScreenVisible = true,
                        isLoading = false,
                    )
                }
            }
        } else {
            val currentSessionIndex =
                currentSessionIndexOf(
                    sessions = initializeData.studySessions,
                    latestRecords = lastCompletedSessions.values,
                    studyStatus = initializeData.studyStatus,
                )
            val chapters =
                items
                    .chunked(SESSIONS_PER_CHAPTER)
                    .mapIndexed { index, sessions ->
                        SessionChapter(
                            chapterNumber = index + 1,
                            sessions = sessions.toPersistentList(),
                            currentSessionIndex =
                                chapterCurrentSessionIndex(
                                    currentSessionIndex = currentSessionIndex,
                                    chapterStartIndex = index * SESSIONS_PER_CHAPTER,
                                    chapterSize = sessions.size,
                                ),
                        )
                    }.toPersistentList()

            reduce {
                state.copy(
                    chapters = chapters,
                    adZeroRemainingHours = initializeData.adZeroRemainingHours,
                    lastStudyStatus = initializeData.studyStatus,
                    isRetryScreenVisible = false,
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun getInitializeData(
        level: JLPTLevel,
    ) = withContext(Dispatchers.IO) {
        val adZeroRemainingHoursDeferred =
            async {
                getAdZeroRemainingHours()
            }
        val lastStudyStatusDeferred =
            async {
                getStudyStatus()
            }
        val allCompletedSessionsDeferred =
            async {
                getStudyRecordsByLevel
                    .invoke(level)
                    .groupBy { it.sessionId }
            }
        // 세션 목록도 같은 병렬 블록에서 함께 로드(기존엔 이후 직렬 호출이었음).
        val studySessionsDeferred =
            async {
                getStudySessionsByLevel(level)
            }
        InitializeData(
            studyStatus = lastStudyStatusDeferred.await(),
            allCompletedSessions = allCompletedSessionsDeferred.await(),
            studySessions = studySessionsDeferred.await(),
            adZeroRemainingHours = adZeroRemainingHoursDeferred.await(),
        )
    }

    private data class InitializeData(
        val studyStatus: StudyStatus?,
        val allCompletedSessions: Map<Int, List<StudyRecord>>,
        val studySessions: List<StudySession>,
        val adZeroRemainingHours: Int,
    )

    companion object {
        private const val SESSIONS_PER_CHAPTER = 24
    }
}

/** 학습한 단어 비율(%). */
private fun StudyStatus.progressPercent(): Int = totalKnownWordIds.size * 100 / totalWordSize.coerceAtLeast(1)

private data class SessionPosition(
    val chapterNumber: Int,
    val indexInChapter: Int,
    val globalIndex: Int,
)

private fun List<SessionChapter>.findSessionPosition(sessionId: Int): SessionPosition? {
    var offset = 0
    forEach { chapter ->
        val index = chapter.sessions.indexOfFirst { it.id == sessionId }
        if (index >= 0) return SessionPosition(chapter.chapterNumber, index, offset + index)
        offset += chapter.sessions.size
    }
    return null
}
