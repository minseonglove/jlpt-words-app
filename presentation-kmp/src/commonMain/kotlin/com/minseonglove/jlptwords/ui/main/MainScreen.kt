package com.minseonglove.jlptwords.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.ui.base.MolluBottomTab
import com.minseonglove.jlptwords.ui.base.MolluBottomTabBar
import com.minseonglove.jlptwords.ui.home.HomeScreen
import com.minseonglove.jlptwords.ui.search.SearchScreen
import com.minseonglove.jlptwords.ui.selection.session.SessionSelectionScreen
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * 하단 탭의 호스트 화면. 탭 전환은 네비게이션이 아니라 상태 변경이며,
 * 탭별 UI 상태(스크롤 등)는 SaveableStateHolder 로, 데이터 상태는 Main 엔트리에
 * 스코프된 각 탭 ViewModel 로 보존된다.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    navigateToWordDetail: (String, String) -> Unit,
    navigateToStudy: (Int, Int) -> Unit,
    navigateToSetting: () -> Unit,
    navigateToLevelSelection: (JLPTLevel) -> Unit,
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MainSideEffect.NavigateToLevelSelection -> {
                navigateToLevelSelection(sideEffect.level)
            }
        }
    }

    // 홈 외 탭에서 시스템 back 은 홈 탭으로 전환한다.
    // 홈 탭에서는 이 핸들러가 컴포지션에 없어 Home 콘텐츠의 종료 시트 핸들러가 동작한다.
    if (state.selectedTab != MolluBottomTab.HOME) {
        val backNavigationState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = backNavigationState,
            onBackCompleted = viewModel::onBackToHome,
        )
    }

    Scaffold(
        modifier = modifier,
        // 상/하단 인셋은 MolluTopBar(statusBarsPadding)·MolluBottomTabBar(navigationBarsPadding)가
        // 각자 처리하므로 이 Scaffold 는 시스템바 인셋을 콘텐츠에 적용하지 않는다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            MolluBottomTabBar(
                selectedTab = state.selectedTab,
                onTabClick = viewModel::onTabClick,
            )
        },
    ) { innerPadding ->
        val saveableStateHolder = rememberSaveableStateHolder()
        // 탭 전환은 관례(하단 탭 = 모드 전환)에 따라 애니메이션 없이 즉시 교체한다.
        Box(modifier = Modifier.padding(innerPadding)) {
            saveableStateHolder.SaveableStateProvider(key = state.selectedTab.name) {
                when (state.selectedTab) {
                    MolluBottomTab.HOME ->
                        HomeScreen(
                            navigateToWordDetail = navigateToWordDetail,
                            navigateToStudy = navigateToStudy,
                            navigateToSetting = navigateToSetting,
                            navigateToLevelSelection = navigateToLevelSelection,
                        )

                    MolluBottomTab.STUDY ->
                        SessionSelectionScreen(
                            navigationToStudyScreen = navigateToStudy,
                            navigateToBack = viewModel::onBackToHome,
                            navigateToSetting = navigateToSetting,
                        )

                    MolluBottomTab.SEARCH ->
                        SearchScreen(
                            navigateToWordDetail = navigateToWordDetail,
                            navigateToBack = viewModel::onBackToHome,
                            navigateToSetting = navigateToSetting,
                        )
                }
            }
        }
    }
}
