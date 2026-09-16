package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.BorderLight

@Composable
fun BaseDragHandle(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = BorderLight,
                    shape = RoundedCornerShape(2.dp),
                ),
    )
}
