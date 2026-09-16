package com.minseonglove.jlptwords.ui.nativead

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.HighLightGradientEnd
import com.minseonglove.jlptwords.ui.theme.HighLightGradientStart
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ad_attribution_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun NativeAdBadge(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier =
            modifier
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    HighLightGradientStart,
                                    HighLightGradientEnd,
                                ),
                        ),
                ).padding(4.dp),
        text = stringResource(Res.string.ad_attribution_label),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onPrimary,
    )
}
