package com.minseonglove.jlptwords.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.toRoute
import com.minseonglove.jlptwords.navigation.dto.LevelSelectionDTO
import com.minseonglove.jlptwords.navigation.dto.StudyDTO
import com.minseonglove.jlptwords.navigation.type.LevelSelectionNavType
import com.minseonglove.jlptwords.navigation.type.StudyNavType
import com.minseonglove.jlptwords.ui.base.MolluBottomTab
import com.minseonglove.jlptwords.ui.main.MainScreen
import com.minseonglove.jlptwords.ui.main.MainViewModel
import com.minseonglove.jlptwords.ui.selection.level.LevelSelectionScreen
import com.minseonglove.jlptwords.ui.setting.OpenSourceLicenseScreen
import com.minseonglove.jlptwords.ui.setting.SettingScreen
import com.minseonglove.jlptwords.ui.splash.SplashScreen
import com.minseonglove.jlptwords.ui.study.StudyScreen
import com.minseonglove.jlptwords.ui.worddetail.WordDetailScreen
import com.minseonglove.jlptwords.usecase.ObserveJapaneseLanguageEnabled
import com.minseonglove.jlptwords.util.AppLanguage
import com.minseonglove.jlptwords.util.applyAppLanguage
import com.minseonglove.jlptwords.util.finishApp
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.reflect.typeOf

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    // navController 는 key(appLanguage) 재구성 밖에서 생성해 언어 전환 시에도 백스택을 보존한다.
    navController: NavHostController = rememberNavController(),
    observeJapaneseLanguageEnabled: ObserveJapaneseLanguageEnabled = koinInject(),
) {
    // 첫 방출 전(null)에는 기기 로케일을 그대로 쓰고, 값이 정해지면 그 언어를 적용한다.
    val isJapaneseEnabled by remember { observeJapaneseLanguageEnabled() }.collectAsState(initial = null)
    val appLanguage = isJapaneseEnabled?.let { if (it) AppLanguage.JAPANESE else AppLanguage.KOREAN }
    // key(appLanguage) 하위가 재구성되기 전에 플랫폼 로케일을 먼저 바꿔, 재구성 시 새 언어 리소스를 읽게 한다.
    remember(appLanguage) { appLanguage?.let(::applyAppLanguage) }

    // NavHost 는 넘겨받은 graph 를 navController 에 대입하고, 이전과 다른 인스턴스가 들어오면
    // NavController 가 백스택을 모두 비우고 startDestination 부터 다시 시작한다.
    // 커스텀 NavType 은 equals 를 구현하지 않아 graph 를 다시 만들면 매번 다른 인스턴스로 판정되므로,
    // graph 는 key(appLanguage) 재구성 밖에서 한 번만 만든다.
    val graph =
        remember(navController) {
            navController.createGraph(startDestination = SplashRoute) {
                mainGraph(navController)
                levelSelectionGraph(navController)
                splashGraph(navController)
                studyGraph(navController)
                settingGraph(navController)
                openSourceLicensesGraph(navController)
                wordDetailGraph(navController)
            }
        }

    // 언어(appLanguage)가 바뀌면 이 블록만 재구성돼 화면의 문자열 리소스가 새 언어로 다시 로드된다.
    // navController 와 graph 는 블록 밖에 있으므로 재구성돼도 현재 화면·백스택은 유지된다.
    key(appLanguage) {
        NavHost(
            navController = navController,
            graph = graph,
            modifier = modifier,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 350),
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 350),
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 350),
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 350),
                )
            },
        )
    }
}

// 급수 선택 완료(LevelSelection pop) 시 Main 에 단어학습 탭 전환을 요청하는 결과 키.
private const val SELECT_STUDY_TAB_KEY = "select_study_tab"

// Main 은 스플래시 이후의 루트 화면으로, 하단 탭(홈/단어학습/단어 검색)을 호스팅한다.
// 상세 화면(단어 상세·학습·설정·급수 선택)은 Main 위에 push 되어 탭바를 가린다.
private fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
) {
    composable<MainRoute> { backStackEntry ->
        val mainViewModel: MainViewModel = koinViewModel()
        val selectStudyTab by backStackEntry.savedStateHandle
            .getStateFlow(SELECT_STUDY_TAB_KEY, false)
            .collectAsState()
        LaunchedEffect(selectStudyTab) {
            if (selectStudyTab) {
                backStackEntry.savedStateHandle[SELECT_STUDY_TAB_KEY] = false
                mainViewModel.onTabClick(MolluBottomTab.STUDY)
            }
        }
        MainScreen(
            viewModel = mainViewModel,
            navigateToWordDetail = { kanji, pronunciation ->
                navController.navigateOnce(WordDetailRoute(kanji, pronunciation))
            },
            navigateToStudy = { sessionId, sessionIndex ->
                val studyDTO =
                    StudyDTO(
                        sessionId = sessionId,
                        sessionIndex = sessionIndex,
                    )
                navController.navigateOnce(StudyRoute(studyDTO))
            },
            navigateToSetting = {
                navController.navigateOnce(SettingRoute)
            },
            navigateToLevelSelection = { level ->
                val levelSelectionDTO = LevelSelectionDTO(currentLevel = level)
                navController.navigateOnce(LevelSelectionRoute(levelSelectionDTO))
            },
        )
    }
}

private fun NavGraphBuilder.levelSelectionGraph(
    navController: NavHostController,
) {
    composable<LevelSelectionRoute>(
        typeMap =
            mapOf(
                typeOf<LevelSelectionDTO>() to LevelSelectionNavType(),
            ),
    ) {
        LevelSelectionScreen(
            onLevelConfirmed = {
                // 급수는 preference 에 이미 저장됐다. Main 으로 돌아가 단어학습 탭 전환을 요청한다.
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(SELECT_STUDY_TAB_KEY, true)
                navController.popBackStackOnce()
            },
            navigateToBack = {
                val isNavigate = navController.navigateUpOnce()
                if (isNavigate.not()) {
                    finishApp()
                }
            },
        )
    }
}

private fun NavGraphBuilder.splashGraph(
    navController: NavHostController,
) {
    composable<SplashRoute> {
        SplashScreen(
            navigateToHome = {
                navController.navigate(MainRoute) {
                    popUpTo<SplashRoute> {
                        inclusive = true
                    }
                }
            },
            navigateToLevelSelection = { level ->
                // 첫 실행은 급수 선택부터 시작하되, 루트인 Main 을 백스택 아래에 깔아 둔다.
                navController.navigate(MainRoute) {
                    popUpTo<SplashRoute> {
                        inclusive = true
                    }
                }
                val levelSelectionDTO =
                    LevelSelectionDTO(
                        currentLevel = level,
                    )
                navController.navigate(LevelSelectionRoute(levelSelectionDTO))
            },
        )
    }
}

private fun NavGraphBuilder.studyGraph(
    navController: NavHostController,
) {
    composable<StudyRoute>(
        typeMap =
            mapOf(
                typeOf<StudyDTO>() to StudyNavType(),
            ),
    ) {
        StudyScreen(
            navigateToWordDetail = { kanji, pronunciation ->
                navController.navigateOnce(WordDetailRoute(kanji, pronunciation))
            },
            navigateToBack = {
                navController.popBackStackOnce()
            },
            navigateToSetting = {
                navController.navigateOnce(SettingRoute)
            },
        )
    }
}

private fun NavGraphBuilder.settingGraph(
    navController: NavHostController,
) {
    composable<SettingRoute> {
        SettingScreen(
            navigateToOpenSourceLicenses = {
                navController.navigateOnce(OpenSourceLicensesRoute)
            },
            navigateToBack = {
                navController.popBackStackOnce()
            },
        )
    }
}

private fun NavGraphBuilder.openSourceLicensesGraph(
    navController: NavHostController,
) {
    composable<OpenSourceLicensesRoute> {
        OpenSourceLicenseScreen(
            navigateToBack = {
                navController.popBackStackOnce()
            },
        )
    }
}

// 네비게이션 경로만 등록한다. study 등 외부 화면의 진입점(UI)은 아직 연결하지 않는다.
// 예문 속 JLPT 단어 클릭 시 같은 화면을 새 표제어로 push 하는 self-navigation 만 연결한다.
private fun NavGraphBuilder.wordDetailGraph(
    navController: NavHostController,
) {
    composable<WordDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<WordDetailRoute>()
        WordDetailScreen(
            kanji = route.kanji,
            pronunciation = route.pronunciation,
            navigateToWordDetail = { kanji, pronunciation ->
                navController.navigateOnce(WordDetailRoute(kanji, pronunciation))
            },
            navigateToBack = {
                navController.popBackStackOnce()
            },
        )
    }
}

@Serializable
object MainRoute

@Serializable
data class LevelSelectionRoute(
    val levelSelectionDTO: LevelSelectionDTO,
)

@Serializable
object SplashRoute

@Serializable
data class StudyRoute(
    val studyDTO: StudyDTO,
)

@Serializable
object SettingRoute

@Serializable
object OpenSourceLicensesRoute

@Serializable
data class WordDetailRoute(
    val kanji: String,
    val pronunciation: String,
)
