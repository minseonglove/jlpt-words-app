package com.minseonglove.jlptwords.ui.exit

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.base.BaseBottomSheet
import com.minseonglove.jlptwords.ui.base.MolluBottomButton
import com.minseonglove.jlptwords.ui.nativead.NativeAdSession
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.exit_app_bottom_sheet_confirm
import jlptwords.presentation_kmp.generated.resources.exit_app_bottom_sheet_subtitle
import jlptwords.presentation_kmp.generated.resources.exit_app_bottom_sheet_title
import org.jetbrains.compose.resources.stringResource

/**
 * 광고 영역 고정 높이. 상태 전환 시 다이얼로그 높이가 흔들리지 않게 하고,
 * Android NativeAdView(AndroidView, MATCH_PARENT)가 시트 전체로 확장되는 것을 막는다(높이 제약 필수).
 */
private val AdAreaHeight = 300.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitAppBottomSheet(
    modifier: Modifier = Modifier,
    exitAdState: ExitAdState,
    onDismissRequest: () -> Unit,
    onExitAppButtonClick: () -> Unit,
    sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
) {
    BaseBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        properties = properties,
        shape = RectangleShape,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 24.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 안내 문구
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.exit_app_bottom_sheet_title),
                    style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
                    color = MolluTheme.colorScheme.black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.exit_app_bottom_sheet_subtitle),
                    style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.black,
                    textAlign = TextAlign.Center,
                )
            }

            // 광고 영역
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AdAreaHeight)
                        .border(
                            width = 0.25.dp,
                            color = MolluTheme.colorScheme.black,
                            shape = RectangleShape,
                        ).padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (exitAdState) {
                    ExitAdState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MolluTheme.colorScheme.black,
                            strokeWidth = 4.dp,
                        )
                    }

                    ExitAdState.AdZero -> {
                        ExitAdFallback(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    is ExitAdState.AdReady -> {
                        NativeAdSession(
                            modifier = Modifier.fillMaxSize(),
                            nativeAd = exitAdState.nativeAd,
                        )
                    }
                }
            }

            // 앱 종료하기
            MolluBottomButton(
                text = stringResource(Res.string.exit_app_bottom_sheet_confirm),
                onClick = onExitAppButtonClick,
                height = 56.dp,
                textStyle = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}
