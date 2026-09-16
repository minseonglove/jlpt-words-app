package com.minseonglove.jlptwords.ui.base

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * 예문 문장에서 `*단어*` 로 감싼 부분만 빨강으로 강조한 AnnotatedString 을 만든다.
 * `*` 마커는 표시에서 제거되며, 마커가 없으면 전체를 기본색으로 둔다.
 */
fun buildHighlightedExample(
    sentence: String,
    highlightColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        append("“")
        sentence.split("*").forEachIndexed { index, segment ->
            if (index % 2 == 1) {
                withStyle(SpanStyle(color = highlightColor)) {
                    append(segment)
                }
            } else {
                append(segment)
            }
        }
        append("”")
    }
