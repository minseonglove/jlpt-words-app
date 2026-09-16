package com.minseonglove.jlptwords.ui

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.minseonglove.jlptwords.entity.RubySegment
import com.minseonglove.jlptwords.ui.base.RubyExampleText
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 루비 칸의 높이가 내용(가나 유무)에 따라 달라지면 본문 글자가 그만큼 밀려 한 줄 안에서 세로로 어긋난다.
 * 실제 폰트 메트릭이 필요해 에뮬레이터/기기에서만 검증할 수 있다.
 */
@RunWith(AndroidJUnit4::class)
class RubyExampleTextAlignmentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun 루비가_있는_글자와_없는_글자의_본문이_같은_높이에_놓인다() {
        composeRule.setContent {
            MolluTheme {
                RubyExampleText(
                    furigana =
                        persistentListOf(
                            RubySegment(text = "学校", reading = "がっこう", isHighlight = false),
                            RubySegment(text = "へ", reading = "", isHighlight = false),
                        ),
                    style = MolluTheme.typography.body1,
                    color = MolluTheme.colorScheme.black,
                    highlightColor = MolluTheme.colorScheme.highlightRed,
                )
            }
        }
        composeRule.waitForIdle()

        val withRuby = composeRule.onNodeWithText("学校").getUnclippedBoundsInRoot()
        val withoutRuby = composeRule.onNodeWithText("へ").getUnclippedBoundsInRoot()

        assertEquals(withRuby.top.value, withoutRuby.top.value, 0.5f)
        assertEquals(withRuby.bottom.value, withoutRuby.bottom.value, 0.5f)
    }
}
