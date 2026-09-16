package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.ui.theme.SecondaryGradientEnd
import com.minseonglove.jlptwords.ui.theme.SecondaryGradientStart

@Composable
fun DialogActionButtons(
    modifier: Modifier = Modifier,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BaseButton(
            modifier = Modifier.fillMaxWidth(),
            text = confirmText,
            backgroundBrush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            SecondaryGradientStart,
                            SecondaryGradientEnd,
                        ),
                ),
            onClick = onConfirm,
        )

        Spacer(modifier = Modifier.height(12.dp))

        BaseButton(
            modifier = Modifier.fillMaxWidth(),
            text = cancelText,
            textColor = MolluTheme.colorScheme.sub,
            backgroundBrush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                        ),
                ),
            indication = null,
            interactionSource = null,
            onClick = onCancel,
        )
    }
}
