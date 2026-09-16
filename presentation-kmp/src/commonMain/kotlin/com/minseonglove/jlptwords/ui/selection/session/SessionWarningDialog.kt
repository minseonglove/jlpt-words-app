package com.minseonglove.jlptwords.ui.selection.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.ui.base.DashedDivider
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n1
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n2
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n3
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n4
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n5
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_cancel
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_info
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_progress
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_start
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_subtitle
import jlptwords.presentation_kmp.generated.resources.session_progress_warning_title
import org.jetbrains.compose.resources.stringResource

/**
 * 진행 중인 학습이 있을 때, 다른 세션을 시작하면 기존 진행이 삭제됨을 알리는 다이얼로그(mollu).
 * Figma 733-7047 디자인: 흰 배경 + 검정 외곽선, 안내 문구, 진행 정보 박스, 점선, 시작/돌아가기 버튼.
 */
@Composable
fun SessionWarningDialog(
    level: JLPTLevel,
    chapterNumber: Int,
    sessionNumber: Int,
    progressPercent: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        MolluTheme {
            SessionWarningDialogContent(
                modifier = modifier,
                level = level,
                chapterNumber = chapterNumber,
                sessionNumber = sessionNumber,
                progressPercent = progressPercent,
                onConfirm = onConfirm,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun SessionWarningDialogContent(
    level: JLPTLevel,
    chapterNumber: Int,
    sessionNumber: Int,
    progressPercent: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MolluTheme.colorScheme.white)
                .border(
                    width = 0.5.dp,
                    color = MolluTheme.colorScheme.black,
                    shape = RectangleShape,
                ).padding(
                    top = 48.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 안내 문구
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.session_progress_warning_title),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.session_progress_warning_subtitle),
                style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
        }

        // 진행 정보 박스
        Column(
            modifier =
                Modifier
                    .border(
                        width = 0.5.dp,
                        color = MolluTheme.colorScheme.black,
                        shape = RectangleShape,
                    ).padding(
                        horizontal = 40.dp,
                        vertical = 16.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text =
                    stringResource(
                        Res.string.session_progress_warning_info,
                        level.label(),
                        chapterNumber,
                        sessionNumber,
                    ),
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.session_progress_warning_progress, progressPercent),
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.highlightRed,
                textAlign = TextAlign.Center,
            )
        }

        DashedDivider(modifier = Modifier.fillMaxWidth())

        // 버튼
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MolluBottomButton(
                text = stringResource(Res.string.session_progress_warning_start),
                onClick = onConfirm,
                height = 56.dp,
                textStyle = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.session_progress_warning_cancel),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.sub,
                )
            }
        }
    }
}

@Composable
private fun JLPTLevel.label(): String =
    when (this) {
        JLPTLevel.N1 -> stringResource(Res.string.jlpt_level_n1)
        JLPTLevel.N2 -> stringResource(Res.string.jlpt_level_n2)
        JLPTLevel.N3 -> stringResource(Res.string.jlpt_level_n3)
        JLPTLevel.N4 -> stringResource(Res.string.jlpt_level_n4)
        JLPTLevel.N5 -> stringResource(Res.string.jlpt_level_n5)
    }

@Preview
@Composable
private fun SessionWarningDialogPreview() {
    MolluTheme {
        SessionWarningDialogContent(
            level = JLPTLevel.N1,
            chapterNumber = 1,
            sessionNumber = 3,
            progressPercent = 58,
            onConfirm = {},
            onCancel = {},
        )
    }
}
