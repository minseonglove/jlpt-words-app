package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.MolluTheme

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = MolluTheme.colorScheme.white,
    backgroundBrush: Brush,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues =
        PaddingValues(
            horizontal = 32.dp,
            vertical = 16.dp,
        ),
    indication: Indication? = LocalIndication.current,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundBrush)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            enabled = enabled,
                            indication = indication,
                            interactionSource = interactionSource,
                        ) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onClick()
                        }
                    } else {
                        Modifier
                    },
                ).padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.invoke()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}
