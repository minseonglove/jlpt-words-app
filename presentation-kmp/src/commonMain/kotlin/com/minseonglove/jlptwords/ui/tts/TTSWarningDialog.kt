package com.minseonglove.jlptwords.ui.tts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.base.DialogActionButtons
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.ui.theme.PrimaryGradientEnd
import com.minseonglove.jlptwords.ui.theme.PrimaryGradientStart
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.tts_warning_cancel
import jlptwords.presentation_kmp.generated.resources.tts_warning_confirm
import jlptwords.presentation_kmp.generated.resources.tts_warning_description
import jlptwords.presentation_kmp.generated.resources.tts_warning_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TTSWarningDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        TTSWarningDialogContent(
            modifier = modifier,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun TTSWarningDialogContent(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MolluTheme.colorScheme.backgroundNormal,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            PrimaryGradientStart,
                                            PrimaryGradientEnd,
                                        ),
                                ),
                        ).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Header Icon
                Icon(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .padding(
                                bottom = 12.dp,
                            ),
                    imageVector = Icons.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )

                Text(
                    text = stringResource(Res.string.tts_warning_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                        ),
                text = stringResource(Res.string.tts_warning_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        horizontal = 24.dp,
                    ),
                color = MolluTheme.colorScheme.lineNormal,
            )

            DialogActionButtons(
                modifier = Modifier.padding(24.dp),
                confirmText = stringResource(Res.string.tts_warning_confirm),
                cancelText = stringResource(Res.string.tts_warning_cancel),
                onConfirm = onConfirm,
                onCancel = onCancel,
            )
        }
    }
}
