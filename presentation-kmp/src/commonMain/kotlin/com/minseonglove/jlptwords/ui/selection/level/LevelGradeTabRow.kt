package com.minseonglove.jlptwords.ui.selection.level

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.bg_pattern_black
import org.jetbrains.compose.resources.painterResource

/**
 * 급수카드 내부 탭(N5~N1). 활성 탭은 검정 텍스처+흰 글자, 비활성은 라인+검정 글자.
 */
@Composable
internal fun LevelGradeTabRow(
    selectedLevel: JLPTLevel,
    onLevelSelected: (JLPTLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
    ) {
        JLPTLevel.entries.forEach { level ->
            val selected = level == selectedLevel
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.5.dp, MolluTheme.colorScheme.black)
                        .clickable { onLevelSelected(level) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Image(
                        painter = painterResource(Res.drawable.bg_pattern_black),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                }
                Text(
                    text = level.name,
                    style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
                    color =
                        if (selected) {
                            MolluTheme.colorScheme.white
                        } else {
                            MolluTheme.colorScheme.black
                        },
                )
            }
        }
    }
}
