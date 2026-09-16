package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    prefix: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
) {
    TopAppBar(
        modifier = modifier,
        title = {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
        navigationIcon = prefix,
        actions = actions,
        colors = colors,
    )
}

@Preview(showBackground = true)
@Composable
private fun BaseTopBarPreview() {
    JLPTWordsTheme {
        BaseTopBar(
            title = "N1",
            prefix = {
                IconButton(
                    onClick = { },
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Back,
                        contentDescription = null,
                    )
                }
            },
        )
    }
}
