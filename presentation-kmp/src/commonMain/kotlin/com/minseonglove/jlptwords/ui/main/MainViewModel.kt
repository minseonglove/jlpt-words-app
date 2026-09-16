package com.minseonglove.jlptwords.ui.main

import androidx.lifecycle.SavedStateHandle
import com.minseonglove.jlptwords.entity.StudyEntry
import com.minseonglove.jlptwords.ui.base.BaseViewModel
import com.minseonglove.jlptwords.ui.base.MolluBottomTab
import com.minseonglove.jlptwords.usecase.ResolveStudyEntry
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Syntax

/**
 * 하단 탭 선택 상태 전용 ViewModel. 탭 콘텐츠(홈/단어학습/단어 검색)의 ViewModel 은
 * 각자 유지되며, 여기서는 탭 전환과 STUDY 탭 진입 판정만 담당한다.
 */
class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val resolveStudyEntry: ResolveStudyEntry,
) : BaseViewModel<MainState, MainSideEffect>() {
    override val container: Container<MainState, MainSideEffect> =
        container(
            MainState(
                selectedTab =
                    savedStateHandle
                        .get<String>(SELECTED_TAB_KEY)
                        // 복원값은 앱 업데이트 전 버전일 수 있으므로 미지의 이름은 홈으로 폴백한다.
                        ?.let { name -> MolluBottomTab.entries.firstOrNull { it.name == name } }
                        ?: MolluBottomTab.HOME,
            ),
        )

    // 급수는 한 번 선택되면 해제되지 않으므로, 최초 확인 후에는 조회 없이 즉시 전환한다.
    private var hasStudyLevel = false

    fun onTabClick(
        tab: MolluBottomTab,
    ) = intent {
        when (tab) {
            MolluBottomTab.HOME, MolluBottomTab.SEARCH -> selectTab(tab)
            // 단어학습 탭은 선택된 급수가 있어야 진입할 수 있다(없으면 급수 선택으로 유도).
            MolluBottomTab.STUDY ->
                if (hasStudyLevel) {
                    selectTab(MolluBottomTab.STUDY)
                } else {
                    when (val entry = resolveStudyEntry()) {
                        is StudyEntry.Sessions -> {
                            hasStudyLevel = true
                            selectTab(MolluBottomTab.STUDY)
                        }

                        is StudyEntry.NeedsLevelSelection ->
                            postSideEffect(MainSideEffect.NavigateToLevelSelection(entry.defaultLevel))
                    }
                }
        }
    }

    fun onBackToHome() =
        intent {
            selectTab(MolluBottomTab.HOME)
        }

    private suspend fun Syntax<MainState, MainSideEffect>.selectTab(
        tab: MolluBottomTab,
    ) {
        // 프로세스 재생성 후에도 마지막 탭을 복원하기 위해 SavedStateHandle 에 보관한다.
        savedStateHandle[SELECTED_TAB_KEY] = tab.name
        reduce { state.copy(selectedTab = tab) }
    }

    companion object {
        private const val SELECTED_TAB_KEY = "selected_tab"
    }
}
