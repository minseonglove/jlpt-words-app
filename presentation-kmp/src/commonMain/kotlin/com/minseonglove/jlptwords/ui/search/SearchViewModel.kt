package com.minseonglove.jlptwords.ui.search

import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.AllContentsSyncResult
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.usecase.GetAdZeroRemainingHours
import com.minseonglove.jlptwords.usecase.InitializeAllContents
import com.minseonglove.jlptwords.usecase.SearchWords
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.syntax.Syntax

class SearchViewModel(
    private val searchWords: SearchWords,
    private val getAdZeroRemainingHours: GetAdZeroRemainingHours,
    private val initializeAllContents: InitializeAllContents,
) : BaseViewModel<SearchState, SearchSideEffect>() {
    override val container: Container<SearchState, SearchSideEffect> =
        container(
            initialState = SearchState(),
        )

    /**
     * 검색 실행 요청 스트림. 모든 검색은 [observeSearchRequests] 의 collectLatest 파이프라인
     * 하나로 직렬화되어, 새 요청이 오면 진행 중인 검색이 취소된다(늦게 끝난 검색이 최신 결과를
     * 덮어쓰는 경합 방지). 초기값이 첫 진입 시 전체 목록 로드를 수행한다.
     */
    private val searchRequests = MutableStateFlow(SearchRequest(isDebounced = false, version = 0))

    /**
     * 전 급수 콘텐츠 보충 동기화를 이 ViewModel 생명주기에서 이미 확보했는지 여부.
     * 탭 복귀 때마다 onCreate 가 다시 호출되므로, 1회 확보 후에는 불필요한 원격 동기화 확인을
     * 건너뛴다. 미확보(오프라인 등)면 기록하지 않아 다음 복귀에서 다시 시도된다.
     */
    private var contentsReady = false

    init {
        observeSearchRequests()
    }

    fun onCreate() {
        SearchIntent.Initialize.post()
    }

    /**
     * 검색어 입력 반영. 비동기 intent 를 거치면 빠른 입력·일본어 IME 조합 중 글자가 유실될 수 있어
     * Orbit 가이드대로 [blockingIntent] 로 동기 반영한다.
     */
    fun onQueryChange(query: String) {
        blockingIntent {
            reduce { state.copy(query = query) }
        }
        requestSearch(isDebounced = true)
    }

    fun onWordClick(
        kanji: String,
        pronunciation: String,
    ) {
        SearchIntent.ClickWord(kanji, pronunciation).post()
    }

    fun onBackClick() {
        SearchIntent.ClickBack.post()
    }

    fun onFilterClick() {
        SearchIntent.OpenFilterPanel.post()
    }

    fun onSortClick() {
        SearchIntent.OpenSortPanel.post()
    }

    fun onSortDirectionClick() {
        SearchIntent.ToggleSortDirection.post()
    }

    fun onDraftLevelToggle(level: JLPTLevel) {
        SearchIntent.ToggleDraftLevel(level).post()
    }

    fun onDraftScriptTypeToggle(scriptType: WordScriptType) {
        SearchIntent.ToggleDraftScriptType(scriptType).post()
    }

    fun onAllDraftLevelsSelect() {
        SearchIntent.SelectAllDraftLevels.post()
    }

    fun onAllDraftScriptTypesSelect() {
        SearchIntent.SelectAllDraftScriptTypes.post()
    }

    fun onFilterReset() {
        SearchIntent.ResetDraftFilter.post()
    }

    fun onFilterApply() {
        SearchIntent.ApplyFilter.post()
    }

    fun onDraftSortTypeSelect(sortType: WordSortType) {
        SearchIntent.SelectDraftSortType(sortType).post()
    }

    fun onSortApply() {
        SearchIntent.ApplySort.post()
    }

    fun onAdZeroClick() {
        SearchIntent.ShowAdZeroDialog.post()
    }

    fun onAdZeroDialogDismiss() {
        SearchIntent.DismissAdZeroDialog.post()
    }

    fun onSettingsClick() {
        SearchIntent.ClickSettings.post()
    }

    private fun requestSearch(isDebounced: Boolean) {
        searchRequests.update { SearchRequest(isDebounced = isDebounced, version = it.version + 1) }
    }

    private fun observeSearchRequests() {
        intent {
            searchRequests.collectLatest { request ->
                if (request.isDebounced) {
                    delay(QUERY_DEBOUNCE_MILLIS)
                }
                performSearch()
            }
        }
    }

    private fun SearchIntent.post() {
        intent {
            when (this@post) {
                SearchIntent.Initialize -> initialize()

                is SearchIntent.ClickWord -> {
                    postSideEffect(SearchSideEffect.NavigateToWordDetail(kanji, pronunciation))
                }

                SearchIntent.ClickBack -> {
                    postSideEffect(SearchSideEffect.NavigateToBack)
                }

                SearchIntent.OpenFilterPanel -> {
                    reduce {
                        state.copy(
                            activePanel = SearchPanel.FILTER,
                            draftLevels = state.selectedLevels,
                            draftScriptTypes = state.selectedScriptTypes,
                        )
                    }
                }

                SearchIntent.OpenSortPanel -> {
                    reduce {
                        state.copy(
                            activePanel = SearchPanel.SORT,
                            draftSortType = state.sortType,
                        )
                    }
                }

                SearchIntent.ToggleSortDirection -> {
                    reduce {
                        state.copy(isAscending = state.isAscending.not())
                    }
                    requestSearch(isDebounced = false)
                }

                is SearchIntent.ToggleDraftLevel -> {
                    reduce {
                        state.copy(draftLevels = state.draftLevels.toggle(level))
                    }
                }

                is SearchIntent.ToggleDraftScriptType -> {
                    reduce {
                        state.copy(draftScriptTypes = state.draftScriptTypes.toggle(scriptType))
                    }
                }

                SearchIntent.SelectAllDraftLevels -> {
                    reduce {
                        state.copy(draftLevels = ALL_JLPT_LEVELS)
                    }
                }

                SearchIntent.SelectAllDraftScriptTypes -> {
                    reduce {
                        state.copy(draftScriptTypes = ALL_WORD_SCRIPT_TYPES)
                    }
                }

                SearchIntent.ResetDraftFilter -> {
                    reduce {
                        state.copy(
                            draftLevels = ALL_JLPT_LEVELS,
                            draftScriptTypes = ALL_WORD_SCRIPT_TYPES,
                        )
                    }
                }

                SearchIntent.ApplyFilter -> {
                    reduce {
                        state.copy(
                            selectedLevels = state.draftLevels,
                            selectedScriptTypes = state.draftScriptTypes,
                            activePanel = SearchPanel.NONE,
                        )
                    }
                    requestSearch(isDebounced = false)
                }

                is SearchIntent.SelectDraftSortType -> {
                    reduce {
                        state.copy(draftSortType = sortType)
                    }
                }

                SearchIntent.ApplySort -> {
                    reduce {
                        state.copy(
                            sortType = state.draftSortType,
                            activePanel = SearchPanel.NONE,
                        )
                    }
                    requestSearch(isDebounced = false)
                }

                SearchIntent.ShowAdZeroDialog -> {
                    reduce {
                        state.copy(isAdZeroDialogShown = true)
                    }
                }

                SearchIntent.DismissAdZeroDialog -> {
                    reduce {
                        state.copy(isAdZeroDialogShown = false)
                    }
                    // 리워드 광고 시청 후 광고 제거 시간이 갱신될 수 있어 다이얼로그를 닫을 때 다시 조회한다.
                    val adZeroRemainingHours = getAdZeroRemainingHours()
                    reduce {
                        state.copy(adZeroRemainingHours = adZeroRemainingHours)
                    }
                }

                SearchIntent.ClickSettings -> {
                    postSideEffect(SearchSideEffect.NavigateToSetting)
                }
            }
        }
    }

    private suspend fun Syntax<SearchState, SearchSideEffect>.initialize() {
        // 광고 제거 남은 시간(시간 단위) 로드. 만료/미적용이면 0 -> 상단바 "AD" 표시
        val adZeroRemainingHours = getAdZeroRemainingHours()
        reduce {
            state.copy(adZeroRemainingHours = adZeroRemainingHours)
        }
        // 전 급수 데이터는 스플래시가 보장하지만, 스플래시 적재가 실패한 채 진입한
        // 경우(오프라인 등)의 보충 경로. 탭 복귀마다 반복하면 불필요한 원격 동기화 확인이
        // 생기므로 데이터 확보 전까지만 시도한다. 새로 받은 데이터가 있으면 결과를 갱신한다.
        if (contentsReady.not()) {
            // 진행 상태는 스플래시 전용이므로 여기서는 종결 결과만 사용한다.
            val syncResult =
                initializeAllContents()
                    .filterIsInstance<AllContentsSyncProgress.Finished>()
                    .first()
                    .result
            if (syncResult == AllContentsSyncResult.UPDATED) {
                requestSearch(isDebounced = false)
            }
            if (syncResult.isReady) {
                contentsReady = true
            }
        }
    }

    private suspend fun Syntax<SearchState, SearchSideEffect>.performSearch() {
        val words =
            searchWords(
                query = state.query,
                levels = state.selectedLevels,
                scriptTypes = state.selectedScriptTypes,
                sortType = state.sortType,
                isAscending = state.isAscending,
            )
        reduce {
            state.copy(
                isLoading = false,
                words = words.toPersistentList(),
            )
        }
    }

    companion object {
        private const val QUERY_DEBOUNCE_MILLIS = 300L
    }
}

/** 검색 실행 요청. [version] 은 같은 요청을 연속 발행해도 StateFlow 중복 제거에 걸리지 않게 하는 단조 증가 값. */
private data class SearchRequest(
    val isDebounced: Boolean,
    val version: Int,
)

private fun <T> PersistentSet<T>.toggle(value: T): PersistentSet<T> = if (value in this) remove(value) else add(value)
