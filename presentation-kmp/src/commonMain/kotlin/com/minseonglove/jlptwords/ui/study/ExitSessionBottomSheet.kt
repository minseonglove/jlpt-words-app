package com.minseonglove.jlptwords.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.ui.base.BaseBottomSheet
import com.minseonglove.jlptwords.ui.base.DashedDivider
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.exit_session_chapter_format
import jlptwords.presentation_kmp.generated.resources.exit_session_confirm
import jlptwords.presentation_kmp.generated.resources.exit_session_continue
import jlptwords.presentation_kmp.generated.resources.exit_session_description
import jlptwords.presentation_kmp.generated.resources.exit_session_progress_format
import jlptwords.presentation_kmp.generated.resources.exit_session_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitSessionBottomSheet(
    level: JLPTLevel,
    chapter: Int,
    progressPercent: Int,
    onDismissRequest: () -> Unit,
    onExitSessionButtonClick: () -> Unit,
    onContinueButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
) {
    BaseBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        properties = properties,
        shape = RectangleShape,
    ) {
        ExitSessionBottomSheetContent(
            level = level,
            chapter = chapter,
            progressPercent = progressPercent,
            onExitSessionButtonClick = onExitSessionButtonClick,
            onContinueButtonClick = onContinueButtonClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun ExitSessionBottomSheetContent(
    level: JLPTLevel,
    chapter: Int,
    progressPercent: Int,
    onExitSessionButtonClick: () -> Unit,
    onContinueButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // 헤더 (제목 + 설명)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.exit_session_title),
                style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.exit_session_description),
                style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
        }

        // 본문 (정보 박스 + 점선 구분선 + 버튼)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 정보 박스: JLPT 레벨/챕터 + 학습 진행률
            Column(
                modifier =
                    Modifier
                        .border(width = 0.5.dp, color = MolluTheme.colorScheme.black)
                        .padding(horizontal = 47.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.exit_session_chapter_format, level.name, chapter),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.exit_session_progress_format, "$progressPercent%"),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.highlightRed,
                    textAlign = TextAlign.Center,
                )
            }

            // 점선 구분선
            DashedDivider()

            // 학습 종료하기 (검정 패턴 버튼)
            MolluBottomButton(
                text = stringResource(Res.string.exit_session_confirm),
                onClick = onExitSessionButtonClick,
                height = 56.dp,
                textStyle = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            )

            // 학습 계속하기 (회색 텍스트 버튼)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(onClick = onContinueButtonClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.exit_session_continue),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.sub,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExitSessionBottomSheetContentPreview() {
    MolluTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MolluTheme.colorScheme.white),
        ) {
            ExitSessionBottomSheetContent(
                level = JLPTLevel.N1,
                chapter = 3,
                progressPercent = 58,
                onExitSessionButtonClick = {},
                onContinueButtonClick = {},
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            )
        }
    }
}
