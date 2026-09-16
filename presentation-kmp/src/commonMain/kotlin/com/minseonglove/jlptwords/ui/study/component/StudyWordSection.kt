package com.minseonglove.jlptwords.ui.study.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.base.RubyExampleText
import com.minseonglove.jlptwords.ui.base.buildHighlightedExample
import com.minseonglove.jlptwords.ui.base.rememberJpDisplayAutoSize
import com.minseonglove.jlptwords.ui.base.rememberJpHeadAutoSize
import com.minseonglove.jlptwords.ui.study.WordPageCardItem
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.study_detail
import jlptwords.presentation_kmp.generated.resources.study_example_show
import jlptwords.presentation_kmp.generated.resources.study_help
import jlptwords.presentation_kmp.generated.resources.study_word_accuracy
import jlptwords.presentation_kmp.generated.resources.study_word_accuracy_none
import jlptwords.presentation_kmp.generated.resources.word_detail_tts_content_description
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * 단어 영역 위쪽 여백. "도움말"·"상세풀이" 줄이 이만큼 내려온다.
 * 도움말 오버레이의 "도움말 닫기"가 "도움말"과 같은 높이에 놓이도록 [StudyHelpOverlay] 가 같은 값을 쓴다.
 */
val StudyWordSectionTopPadding = 16.dp

/** 한자·발음이 화면 가장자리에 닿지 않도록 두는 좌우 여백. 글자가 줄어들기 시작하는 폭을 결정한다. */
private val WordSideMargin = 20.dp

/**
 * 학습 단어 + 예문 영역(mollu). 한자는 항상 보이고, 뜻 보기([isRevealed]) 시 발음·뜻이 노출된다.
 * 예문은 [isExampleVisible] 로 따로 제어되어 뜻을 가린 채로도 볼 수 있다.
 * 예문 내 대상 단어 빨강 강조는 후속(Phase 2)에서 정밀화한다.
 */
@Composable
fun StudyWordSection(
    item: WordPageCardItem,
    isRevealed: Boolean,
    isExampleVisible: Boolean,
    isWordTTSPlaying: Boolean,
    isExampleTTSPlaying: Boolean,
    onHelpClick: () -> Unit,
    onDetailClick: () -> Unit,
    onCopyClick: () -> Unit,
    onWordTTSClick: () -> Unit,
    onExampleTTSClick: (String) -> Unit,
    onExampleShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = StudyWordSectionTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                UnderlineLabel(text = stringResource(Res.string.study_help), onClick = onHelpClick)
                UnderlineLabel(text = stringResource(Res.string.study_detail), onClick = onDetailClick)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text =
                        item.accuracy?.let { stringResource(Res.string.study_word_accuracy, it) }
                            ?: stringResource(Res.string.study_word_accuracy_none),
                    style = MolluTheme.typography.caption1,
                    color = MolluTheme.colorScheme.sub,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = item.kanji,
                        // 디자인상 카드의 한자는 64sp 글자에 행간을 40sp 로 압축한다.
                        // 행간을 고정으로 두면 글자가 줄어도 이 줄의 높이가 변하지 않아,
                        // 단어 길이에 따라 아래 발음·뜻·예문이 위아래로 밀리지 않는다.
                        style = MolluTheme.typography.jpDisplay.copy(lineHeight = 40.sp),
                        color = MolluTheme.colorScheme.black,
                        textAlign = TextAlign.Center,
                        autoSize = rememberJpDisplayAutoSize(),
                        maxLines = 1,
                        modifier =
                            Modifier
                                .padding(horizontal = WordSideMargin)
                                .clickable(onClick = onCopyClick),
                    )
                    Text(
                        text = item.pronunciation,
                        style = MolluTheme.typography.jpHead.copy(fontWeight = FontWeight.W300),
                        color = MolluTheme.colorScheme.black,
                        textAlign = TextAlign.Center,
                        autoSize = rememberJpHeadAutoSize(),
                        maxLines = 1,
                        modifier =
                            Modifier
                                .padding(horizontal = WordSideMargin)
                                .invisibleUnless(isRevealed),
                    )
                    Text(
                        text = item.meaning,
                        style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Bold),
                        color = MolluTheme.colorScheme.black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.invisibleUnless(isRevealed),
                    )
                }

                IconButton(
                    onClick = onWordTTSClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Speaker,
                        contentDescription = stringResource(Res.string.word_detail_tts_content_description),
                        tint =
                            if (isWordTTSPlaying) {
                                MolluTheme.colorScheme.black
                            } else {
                                MolluTheme.colorScheme.sub
                            },
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MolluTheme.colorScheme.sub,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            val example = item.examples.firstOrNull()
            if (isExampleVisible && example != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (example.furigana.isNotEmpty()) {
                        RubyExampleText(
                            furigana = example.furigana,
                            style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.W300),
                            color = MolluTheme.colorScheme.black,
                            highlightColor = MolluTheme.colorScheme.highlightRed,
                        )
                    } else {
                        Text(
                            text =
                                buildHighlightedExample(
                                    sentence = example.japanese,
                                    highlightColor = MolluTheme.colorScheme.highlightRed,
                                ),
                            style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.W300),
                            color = MolluTheme.colorScheme.black,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(
                        text = example.korean,
                        style = MolluTheme.typography.body2,
                        color = MolluTheme.colorScheme.black,
                        textAlign = TextAlign.Center,
                    )
                    IconButton(
                        onClick = { onExampleTTSClick(example.japanese.replace("*", "")) },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Speaker,
                            contentDescription = stringResource(Res.string.word_detail_tts_content_description),
                            tint =
                                if (isExampleTTSPlaying) {
                                    MolluTheme.colorScheme.black
                                } else {
                                    MolluTheme.colorScheme.sub
                                },
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(Res.string.study_example_show),
                    style = MolluTheme.typography.body1.copy(textDecoration = TextDecoration.Underline),
                    color = MolluTheme.colorScheme.sub,
                    modifier = Modifier.clickable(onClick = onExampleShowClick),
                )
            }
        }
    }
}

/**
 * 자리는 유지한 채 내용만 감춘다. 뜻 보기 여부에 따라 아래 구분선·예문 영역이 위아래로 움직이지 않게 하기 위한 것으로,
 * 가려진 동안에는 접근성 트리에서도 읽히지 않도록 시맨틱을 비운다.
 */
private fun Modifier.invisibleUnless(isVisible: Boolean): Modifier = if (isVisible) this else this.alpha(0f).clearAndSetSemantics {}

@Composable
private fun UnderlineLabel(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = MolluTheme.typography.caption1.copy(textDecoration = TextDecoration.Underline),
        color = MolluTheme.colorScheme.sub,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Preview
@Composable
private fun StudyWordSectionPreview() {
    MolluTheme {
        StudyWordSection(
            item =
                WordPageCardItem(
                    id = 0,
                    kanji = "間隔",
                    pronunciation = "かんかく",
                    meaning = "간격",
                    appearanceCount = 10,
                    correctCount = 5,
                    examples =
                        persistentListOf(
                            Example(japanese = "一定の*間隔*をおけ", korean = "일정한 간격을 둬라"),
                        ),
                ),
            isRevealed = true,
            isExampleVisible = true,
            isWordTTSPlaying = false,
            isExampleTTSPlaying = false,
            onHelpClick = {},
            onDetailClick = {},
            onCopyClick = {},
            onWordTTSClick = {},
            onExampleTTSClick = {},
            onExampleShowClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** 단어장에서 가장 긴 10자 단어. 한 줄이 유지되는지 확인한다. */
@Preview
@Composable
private fun StudyWordSectionLongWordPreview() {
    MolluTheme {
        StudyWordSection(
            item =
                WordPageCardItem(
                    id = 0,
                    kanji = "ありがとうございます",
                    pronunciation = "ありがとうございます",
                    meaning = "감사합니다",
                    appearanceCount = 10,
                    correctCount = 5,
                    examples =
                        persistentListOf(
                            Example(
                                japanese = "「*ありがとうございます*」「どういたしまして」",
                                korean = "감사합니다 \"천만에요\"",
                            ),
                        ),
                ),
            isRevealed = true,
            isExampleVisible = true,
            isWordTTSPlaying = false,
            isExampleTTSPlaying = false,
            onHelpClick = {},
            onDetailClick = {},
            onCopyClick = {},
            onWordTTSClick = {},
            onExampleTTSClick = {},
            onExampleShowClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
