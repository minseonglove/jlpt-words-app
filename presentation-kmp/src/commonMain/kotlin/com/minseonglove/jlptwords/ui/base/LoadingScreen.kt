package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import com.minseonglove.jlptwords.ui.theme.MolluTheme

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MolluTheme.colorScheme.black,
            strokeWidth = 4.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    JLPTWordsTheme {
        LoadingScreen()
    }
}
