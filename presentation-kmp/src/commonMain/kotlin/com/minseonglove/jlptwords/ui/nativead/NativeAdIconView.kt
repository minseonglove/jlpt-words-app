package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeAdIconView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
