package com.minseonglove.jlptwords.extension

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.drawShadow(
    cornerRadius: Dp,
    elevation: Dp = 8.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.05f),
): Modifier {
    return this
        .shadow(
            elevation = elevation,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = shadowColor,
            spotColor = shadowColor,
        ).padding(top = 2.dp, bottom = 6.dp)
}
