package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.retry_screen_description
import jlptwords.presentation_kmp.generated.resources.retry_screen_retry_button
import jlptwords.presentation_kmp.generated.resources.retry_screen_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RetryScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Warning,
            contentDescription = null,
            tint = MolluTheme.colorScheme.sub,
        )
        Spacer(Modifier.size(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.retry_screen_title),
            style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
            color = MolluTheme.colorScheme.black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.retry_screen_description),
            style = MolluTheme.typography.body2,
            color = MolluTheme.colorScheme.sub,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(24.dp))
        MolluBottomButton(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(Res.string.retry_screen_retry_button),
            onClick = onRetry,
            height = 56.dp,
            textStyle = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RetryScreenPreview() {
    JLPTWordsTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MolluTheme.colorScheme.white)
                    .molluPaperBackground(),
        ) {
            RetryScreen(
                modifier = Modifier.fillMaxSize(),
                onRetry = {},
            )
        }
    }
}
