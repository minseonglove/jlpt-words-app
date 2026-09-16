package com.minseonglove.jlptwords.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.minseonglove.jlptwords.ui.theme.MolluTheme

/**
 * mollu 화면 공용 Scaffold. 시스템바 인셋을 콘텐츠에 적용하지 않는다(contentWindowInsets=0):
 * 상단은 [MolluTopBar]/[MolluDefaultTopBar]의 statusBarsPadding, 하단은 AppNavHost의 고정 탭바
 * (navigationBarsPadding)가 각각 단독으로 처리한다. 화면마다 이 인셋 규약을 잊지 않도록 래핑한다.
 * 탭바가 없는 화면은 콘텐츠가 시스템 내비게이션바 아래까지 그려질 수 있으므로, 자체 하단 인셋이
 * 필요하면 [bottomBar] 또는 [content]에서 직접 처리한다.
 */
@Composable
fun MolluScreenScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = MolluTheme.colorScheme.backgroundNormal,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = {
            // Scaffold 가 스낵바를 띄우는 높이는 contentWindowInsets 의 하단값인데 그것을 0 으로 두므로,
            // 여기서 인셋을 주지 않으면 스낵바가 시스템 내비게이션바 아래에 깔린다.
            Box(modifier = Modifier.navigationBarsPadding()) {
                snackbarHost()
            }
        },
        content = content,
    )
}
