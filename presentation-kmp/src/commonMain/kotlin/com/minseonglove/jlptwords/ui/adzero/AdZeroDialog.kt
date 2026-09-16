package com.minseonglove.jlptwords.ui.adzero

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.adzero_action_button
import jlptwords.presentation_kmp.generated.resources.adzero_action_button_loading
import jlptwords.presentation_kmp.generated.resources.adzero_cancel_button
import jlptwords.presentation_kmp.generated.resources.adzero_description
import jlptwords.presentation_kmp.generated.resources.adzero_dialog_subtitle
import jlptwords.presentation_kmp.generated.resources.adzero_dialog_title
import jlptwords.presentation_kmp.generated.resources.adzero_time_format_hours
import jlptwords.presentation_kmp.generated.resources.adzero_time_format_hours_minutes
import jlptwords.presentation_kmp.generated.resources.adzero_time_format_minutes
import jlptwords.presentation_kmp.generated.resources.adzero_timer_label
import jlptwords.presentation_kmp.generated.resources.adzero_timer_max_info
import jlptwords.presentation_kmp.generated.resources.ic_raccoon_adzero
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AdZeroDialog(
    modifier: Modifier = Modifier,
    viewModel: AdZeroViewModel = koinViewModel(),
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AdZeroSideEffect.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(sideEffect.messageRes))
                }
            }

            AdZeroSideEffect.RequestDismiss -> {
                onDismissRequest()
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        viewModel.onCreate()

        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.adEvent.collect { rewardedAd ->
                rewardedAd.show {
                    viewModel.onUserEarnedReward()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onDismissRequest,
                            ),
                )
                AdZeroDialogContent(
                    modifier = Modifier.align(Alignment.Center),
                    state = state,
                    onAdZeroButtonClick = viewModel::onAdZeroButtonClick,
                    onCancelButtonClick = viewModel::onCancelButtonClick,
                )
            }
        }
    }
}

@Composable
private fun AdZeroDialogContent(
    modifier: Modifier = Modifier,
    state: AdZeroState,
    onAdZeroButtonClick: () -> Unit,
    onCancelButtonClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MolluTheme.colorScheme.white)
                .molluPaperBackground()
                .border(
                    width = 0.5.dp,
                    color = MolluTheme.colorScheme.black,
                    shape = RectangleShape,
                ).padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 상단 라쿤 헤더 (이미지 풀블리드 + 텍스트 오버레이)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(204.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_raccoon_adzero),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.adzero_dialog_title),
                    style =
                        MolluTheme.typography.display.copy(
                            fontWeight = FontWeight.Bold,
                            shadow = TextShadow,
                        ),
                    color = MolluTheme.colorScheme.white,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.adzero_dialog_subtitle),
                    style =
                        MolluTheme.typography.body1.copy(
                            fontWeight = FontWeight.Medium,
                            shadow = TextShadow,
                        ),
                    color = MolluTheme.colorScheme.white,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 24 인셋 본문
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 남은 시간 박스
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MolluTheme.colorScheme.black,
                            shape = RectangleShape,
                        ).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.adzero_timer_label),
                        style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.black,
                    )
                    Text(
                        text = formatAdZeroTime(state.adZeroLeftTimeMin),
                        style = MolluTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                        color = MolluTheme.colorScheme.black,
                    )
                }
                Text(
                    text = stringResource(Res.string.adzero_timer_max_info),
                    style = MolluTheme.typography.caption1,
                    color = MolluTheme.colorScheme.black,
                )
            }

            // 설명
            Text(
                text = stringResource(Res.string.adzero_description),
                style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )

            // 버튼
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MolluBottomButton(
                    text =
                        if (state.isAdLoading) {
                            stringResource(Res.string.adzero_action_button_loading)
                        } else {
                            stringResource(Res.string.adzero_action_button)
                        },
                    onClick = onAdZeroButtonClick,
                    enabled = !state.isAdLoading,
                    loading = state.isAdLoading,
                    height = 56.dp,
                    textStyle = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(onClick = onCancelButtonClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.adzero_cancel_button),
                        style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.sub,
                    )
                }
            }
        }
    }
}

/** 헤더 이미지 위 흰 텍스트 가독성용 그림자(Figma: 0 0 10 black 50%). */
private val TextShadow =
    Shadow(
        color = Color(0x80000000),
        offset = Offset.Zero,
        blurRadius = 10f,
    )

@Composable
private fun formatAdZeroTime(minutes: Int): String =
    when {
        minutes < 60 -> stringResource(Res.string.adzero_time_format_minutes, minutes)
        minutes % 60 == 0 -> stringResource(Res.string.adzero_time_format_hours, minutes / 60)
        else -> stringResource(Res.string.adzero_time_format_hours_minutes, minutes / 60, minutes % 60)
    }
