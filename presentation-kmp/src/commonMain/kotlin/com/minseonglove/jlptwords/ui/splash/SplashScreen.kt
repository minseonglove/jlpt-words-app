package com.minseonglove.jlptwords.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.AllContentsSyncProgress
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SyncMode
import com.minseonglove.jlptwords.entity.WordsInitializeProgress
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.RetryScreen
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.util.DarkStatusBarIcons
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_splash_logo
import jlptwords.presentation_kmp.generated.resources.ic_splash_logo_ruby
import jlptwords.presentation_kmp.generated.resources.splash_stage_checking
import jlptwords.presentation_kmp.generated.resources.splash_stage_examples
import jlptwords.presentation_kmp.generated.resources.splash_stage_kanji
import jlptwords.presentation_kmp.generated.resources.splash_stage_readings
import jlptwords.presentation_kmp.generated.resources.splash_stage_words
import jlptwords.presentation_kmp.generated.resources.splash_sync_checking
import jlptwords.presentation_kmp.generated.resources.splash_sync_examples_initialize
import jlptwords.presentation_kmp.generated.resources.splash_sync_examples_update
import jlptwords.presentation_kmp.generated.resources.splash_sync_kanji_initialize
import jlptwords.presentation_kmp.generated.resources.splash_sync_kanji_update
import jlptwords.presentation_kmp.generated.resources.splash_sync_readings_initialize
import jlptwords.presentation_kmp.generated.resources.splash_sync_readings_update
import jlptwords.presentation_kmp.generated.resources.splash_sync_words_initialize
import jlptwords.presentation_kmp.generated.resources.splash_sync_words_update
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * 로고 묶음(旅 108 + たび 25)의 중심은 시안에서 화면 중심보다 13.5dp 위에 있다.
 * 화면 높이와 무관하게 같은 인상을 주도록 절대 좌표 대신 중앙 기준 오프셋으로 잡는다.
 */
private val LogoCenterOffset = (-14).dp

/** 하단 진행 블록의 캡션 baseline 이 시안(852 기준)에서 화면 하단으로부터 74dp 지점이다. */
private val ProgressBottomPadding = 74.dp

private const val SPINNER_ROTATION_MILLIS = 1000

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
    navigateToHome: () -> Unit,
    navigateToLevelSelection: (JLPTLevel) -> Unit,
) {
    val state by viewModel.collectAsState()

    // 상단바 없이 흰 배경이 상태바까지 닿는 유일한 화면이라, 앱 기본값인 밝은 아이콘으로는 보이지 않는다.
    DarkStatusBarIcons()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            SplashSideEffect.NavigateToHome -> {
                navigateToHome()
            }

            is SplashSideEffect.NavigateToLevelSelection -> {
                navigateToLevelSelection(sideEffect.level)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    MolluScreenScaffold {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(it)
                    .molluPaperBackground(),
        ) {
            if (state.progress == WordsInitializeProgress.ERROR) {
                // 핵심 데이터(전 급수 단어·예문)를 확보하지 못한 상태 — 재시도 안내
                RetryScreen(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = viewModel::onRetry,
                )
            } else {
                LogoSection(modifier = Modifier.align(Alignment.Center))

                ProgressSection(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = ProgressBottomPadding),
                    progress = state.progress,
                    syncProgress = state.syncProgress,
                )
            }
        }
    }
}

@Composable
private fun LogoSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.offset(y = LogoCenterOffset),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.size(108.dp),
            painter = painterResource(Res.drawable.ic_splash_logo),
            contentDescription = null,
        )
        Image(
            modifier = Modifier.size(width = 72.dp, height = 25.dp),
            painter = painterResource(Res.drawable.ic_splash_logo_ruby),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
private fun ProgressSection(
    modifier: Modifier = Modifier,
    progress: WordsInitializeProgress,
    syncProgress: AllContentsSyncProgress?,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (progress) {
            WordsInitializeProgress.INITIALIZING,
            WordsInitializeProgress.UPDATING,
            -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(stageLabel(syncProgress)),
                        style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.black,
                    )
                    LoadingSpinner()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = syncProgressMessage(syncProgress),
                    style = MolluTheme.typography.caption1,
                    color = MolluTheme.colorScheme.sub,
                )
            }
            WordsInitializeProgress.IDLE,
            WordsInitializeProgress.ERROR,
            WordsInitializeProgress.COMPLETE,
            WordsInitializeProgress.ALREADY_UP_TO_DATE,
            -> Unit
        }
    }
}

@Composable
private fun LoadingSpinner(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "splashSpinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SPINNER_ROTATION_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "splashSpinnerAngle",
    )
    Box(
        modifier = modifier.size(16.dp).alpha(0.6f),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.rotate(angle),
            imageVector = Icons.Loading,
            contentDescription = null,
        )
    }
}

/** 하단 첫 줄 라벨. Checking 만 시안대로 영문이고 나머지는 로케일 문구를 쓴다. */
private fun stageLabel(syncProgress: AllContentsSyncProgress?): StringResource =
    when (syncProgress) {
        null,
        AllContentsSyncProgress.Checking,
        is AllContentsSyncProgress.Finished,
        -> Res.string.splash_stage_checking

        is AllContentsSyncProgress.Readings -> Res.string.splash_stage_readings
        is AllContentsSyncProgress.Words -> Res.string.splash_stage_words
        is AllContentsSyncProgress.Examples -> Res.string.splash_stage_examples
        is AllContentsSyncProgress.Kanji -> Res.string.splash_stage_kanji
    }

@Composable
private fun syncProgressMessage(syncProgress: AllContentsSyncProgress?): String =
    when (syncProgress) {
        null,
        AllContentsSyncProgress.Checking,
        is AllContentsSyncProgress.Finished,
        -> stringResource(Res.string.splash_sync_checking)

        is AllContentsSyncProgress.Readings ->
            stringResource(
                when (syncProgress.mode) {
                    SyncMode.INITIALIZE -> Res.string.splash_sync_readings_initialize
                    SyncMode.UPDATE -> Res.string.splash_sync_readings_update
                },
            )

        is AllContentsSyncProgress.Words ->
            stringResource(
                when (syncProgress.mode) {
                    SyncMode.INITIALIZE -> Res.string.splash_sync_words_initialize
                    SyncMode.UPDATE -> Res.string.splash_sync_words_update
                },
                syncProgress.done,
                syncProgress.total,
            )

        is AllContentsSyncProgress.Examples ->
            stringResource(
                when (syncProgress.mode) {
                    SyncMode.INITIALIZE -> Res.string.splash_sync_examples_initialize
                    SyncMode.UPDATE -> Res.string.splash_sync_examples_update
                },
                syncProgress.done,
                syncProgress.total,
            )

        is AllContentsSyncProgress.Kanji ->
            stringResource(
                when (syncProgress.mode) {
                    SyncMode.INITIALIZE -> Res.string.splash_sync_kanji_initialize
                    SyncMode.UPDATE -> Res.string.splash_sync_kanji_update
                },
            )
    }
