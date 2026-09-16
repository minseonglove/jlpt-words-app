package com.minseonglove.jlptwords.ui.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.MolluTopBar
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.setting_opensource
import jlptwords.presentation_kmp.generated.resources.word_detail_back_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun OpenSourceLicenseScreen(
    navigateToBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libraries by rememberLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    val hapticFeedback = LocalHapticFeedback.current

    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            topBar = {
                MolluTopBar(
                    title = stringResource(Res.string.setting_opensource),
                    onBackClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        navigateToBack()
                    },
                    titleStyle = MolluTheme.typography.jpHead.copy(fontWeight = FontWeight.Bold),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    backContentDescription = stringResource(Res.string.word_detail_back_content_description),
                )
            },
        ) { innerPadding ->
            LibrariesContainer(
                libraries = libraries,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                // MolluScreenScaffold 는 하단 인셋을 주지 않으므로 목록이 직접 처리한다.
                // 목록 자체는 인셋 아래까지 그려지고 마지막 항목만 가려지지 않는다.
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            )
        }
    }
}
