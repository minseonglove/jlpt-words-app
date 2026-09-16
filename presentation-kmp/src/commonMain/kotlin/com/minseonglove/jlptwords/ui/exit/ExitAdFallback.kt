package com.minseonglove.jlptwords.ui.exit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_splash_logo
import org.jetbrains.compose.resources.painterResource

private val LogoSize = 160.dp

@Composable
fun ExitAdFallback(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.size(LogoSize),
            painter = painterResource(Res.drawable.ic_splash_logo),
            contentDescription = null,
        )
    }
}
