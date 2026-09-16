package com.minseonglove.jlptwords.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.setting_time_picker_cancel
import jlptwords.presentation_kmp.generated.resources.setting_time_picker_confirm
import org.jetbrains.compose.resources.stringResource

/** 학습 알림 시간 선택 다이얼로그. 확인 시 선택한 (시, 분)을 [onConfirm] 으로 전달한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false,
        )

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier =
                modifier
                    .background(MolluTheme.colorScheme.white)
                    .border(width = 0.5.dp, color = MolluTheme.colorScheme.black)
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimePicker(
                state = timePickerState,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.setting_time_picker_cancel),
                    style = MolluTheme.typography.body1,
                    color = MolluTheme.colorScheme.black,
                    modifier =
                        Modifier
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                onDismissRequest()
                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Text(
                    text = stringResource(Res.string.setting_time_picker_confirm),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    color = MolluTheme.colorScheme.black,
                    modifier =
                        Modifier
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                onConfirm(timePickerState.hour, timePickerState.minute)
                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
