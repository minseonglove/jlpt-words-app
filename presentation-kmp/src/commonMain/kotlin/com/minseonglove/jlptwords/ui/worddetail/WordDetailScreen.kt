package com.minseonglove.jlptwords.ui.worddetail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.entity.ExampleToken
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.presentation.icon.Icons
import com.minseonglove.jlptwords.ui.base.DashedDivider
import com.minseonglove.jlptwords.ui.base.MolluTopBar
import com.minseonglove.jlptwords.ui.base.RubyExampleText
import com.minseonglove.jlptwords.ui.base.buildHighlightedExample
import com.minseonglove.jlptwords.ui.base.molluPaperVerticalScroll
import com.minseonglove.jlptwords.ui.base.rememberJpDisplayAutoSize
import com.minseonglove.jlptwords.ui.base.rememberJpHeadAutoSize
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import com.minseonglove.jlptwords.ui.tts.TTSWarningDialog
import com.minseonglove.jlptwords.util.handleInstallLanguagePack
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.tts_volume_muted
import jlptwords.presentation_kmp.generated.resources.word_detail_back_content_description
import jlptwords.presentation_kmp.generated.resources.word_detail_example_number
import jlptwords.presentation_kmp.generated.resources.word_detail_kanji_korean_label
import jlptwords.presentation_kmp.generated.resources.word_detail_kanji_kun_label
import jlptwords.presentation_kmp.generated.resources.word_detail_kanji_on_label
import jlptwords.presentation_kmp.generated.resources.word_detail_title
import jlptwords.presentation_kmp.generated.resources.word_detail_tts_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** 한자·발음이 화면 가장자리에 닿지 않도록 두는 좌우 여백. 글자가 줄어들기 시작하는 폭을 결정한다. */
private val WordSideMargin = 20.dp

@Composable
fun WordDetailScreen(
    kanji: String,
    pronunciation: String,
    modifier: Modifier = Modifier,
    viewModel: WordDetailViewModel = koinViewModel(),
    navigateToWordDetail: (kanji: String, pronunciation: String) -> Unit = { _, _ -> },
    navigateToBack: () -> Unit = {},
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WordDetailSideEffect.NavigateToWordDetail -> {
                navigateToWordDetail(sideEffect.kanji, sideEffect.pronunciation)
            }

            WordDetailSideEffect.ScrollToTop -> {
                scope.launch { scrollState.animateScrollTo(0) }
            }

            WordDetailSideEffect.NavigateToBack -> {
                navigateToBack()
            }

            WordDetailSideEffect.OpenInstallLanguagePack -> {
                handleInstallLanguagePack(
                    showMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
            }

            WordDetailSideEffect.ShowVolumeMutedMessage -> {
                val message = getString(Res.string.tts_volume_muted)
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        }
    }

    LaunchedEffect(kanji, pronunciation) {
        viewModel.onCreate(kanji, pronunciation)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onStopTTS() }
    }

    WordDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        onBackClick = viewModel::onBackClick,
        onTTSClick = viewModel::onTTSClick,
        onExampleTTSClick = viewModel::onExampleTTSClick,
        onExampleWordClick = viewModel::onExampleWordClick,
        onDismissTTSWarningDialog = viewModel::onDismissTTSWarningDialog,
        onOpenInstallLanguagePack = viewModel::onOpenInstallLanguagePack,
        modifier = modifier,
    )
}

@Composable
private fun WordDetailContent(
    state: WordDetailState,
    snackbarHostState: SnackbarHostState,
    scrollState: ScrollState = rememberScrollState(),
    onBackClick: () -> Unit,
    onTTSClick: () -> Unit,
    onExampleTTSClick: (index: Int, text: String) -> Unit,
    onExampleWordClick: (kanji: String, pronunciation: String) -> Unit,
    onDismissTTSWarningDialog: () -> Unit,
    onOpenInstallLanguagePack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MolluTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { WordDetailTopBar(onBackClick = onBackClick) },
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MolluTheme.colorScheme.black)
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .molluPaperVerticalScroll(scrollState)
                            .padding(bottom = 32.dp),
                ) {
                    WordHeader(
                        jlptLevel = state.jlptLevel,
                        kanji = state.kanji,
                        pronunciation = state.pronunciation,
                        partOfSpeech = state.partOfSpeech,
                        meaning = state.meaning,
                        isTTSPlaying = state.isWordTTSPlaying,
                        onTTSClick = onTTSClick,
                    )
                    HorizontalDivider(color = MolluTheme.colorScheme.black)
                    if (state.kanjiInfos.isNotEmpty()) {
                        KanjiBreakdown(kanjiInfos = state.kanjiInfos)
                        HorizontalDivider(color = MolluTheme.colorScheme.black)
                    }
                    state.examples.forEachIndexed { index, example ->
                        ExampleSection(
                            exampleNumber = index + 1,
                            example = example,
                            isTTSPlaying = state.isExampleTTSPlaying(index),
                            onExampleTTSClick = { text -> onExampleTTSClick(index, text) },
                        )
                        if (example.tokens.isNotEmpty()) {
                            DashedDivider()
                            ExampleWords(
                                tokens = example.tokens,
                                onExampleWordClick = onExampleWordClick,
                            )
                        }
                        if (index < state.examples.lastIndex) {
                            HorizontalDivider(color = MolluTheme.colorScheme.black)
                        }
                    }
                }
            }
        }
    }

    if (state.isTTSWarningDialogVisible) {
        TTSWarningDialog(
            onConfirm = onOpenInstallLanguagePack,
            onCancel = onDismissTTSWarningDialog,
            onDismissRequest = onDismissTTSWarningDialog,
        )
    }
}

@Composable
private fun WordDetailTopBar(onBackClick: () -> Unit) {
    MolluTopBar(
        title = stringResource(Res.string.word_detail_title),
        onBackClick = onBackClick,
        titleStyle = MolluTheme.typography.jpHead.copy(fontWeight = FontWeight.Bold),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        backContentDescription = stringResource(Res.string.word_detail_back_content_description),
    )
}

@Composable
private fun WordHeader(
    jlptLevel: JLPTLevel,
    kanji: String,
    pronunciation: String,
    partOfSpeech: String,
    meaning: String,
    isTTSPlaying: Boolean,
    onTTSClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            text = jlptLevel.name,
            style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.sub,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = kanji,
                style = MolluTheme.typography.jpDisplay,
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
                autoSize = rememberJpDisplayAutoSize(),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = WordSideMargin),
            )
            Text(
                text = pronunciation,
                style = MolluTheme.typography.jpHead.copy(fontWeight = FontWeight.W300),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
                autoSize = rememberJpHeadAutoSize(),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = WordSideMargin),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = partOfSpeech,
                    style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                    color = MolluTheme.colorScheme.sub,
                )
                IconButton(
                    onClick = onTTSClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Speaker,
                        contentDescription = stringResource(Res.string.word_detail_tts_content_description),
                        tint =
                            if (isTTSPlaying) {
                                MolluTheme.colorScheme.black
                            } else {
                                MolluTheme.colorScheme.sub
                            },
                    )
                }
            }
            Text(
                modifier =
                    Modifier.fillMaxWidth(),
                text = meaning,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun KanjiBreakdown(kanjiInfos: ImmutableList<KanjiInfo>) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        kanjiInfos.forEach { info ->
            KanjiBreakdownRow(info = info)
        }
    }
}

@Composable
private fun KanjiBreakdownRow(info: KanjiInfo) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MolluTheme.colorScheme.white)
                .padding(horizontal = 20.dp)
                .border(width = 0.5.dp, color = MolluTheme.colorScheme.black),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Text 는 고정 크기 박스 안에서 문단을 위쪽에 그리고 textAlign 은 가로만 담당한다.
        // 세로 중앙은 Box 가, 스크립트별로 다른 폰트의 상하 여백 차이는 LineHeightStyle.Center 가 흡수한다.
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = info.kanji,
                style =
                    MolluTheme.typography.display.copy(
                        fontSize = 48.sp,
                        lineHeight = 60.sp,
                        lineHeightStyle =
                            LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.None,
                            ),
                    ),
                color = MolluTheme.colorScheme.black,
                textAlign = TextAlign.Center,
            )
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 0.5.dp,
            color = MolluTheme.colorScheme.black,
        )
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            info.koreanHanja?.let { korean ->
                KanjiReadingRow(
                    label = stringResource(Res.string.word_detail_kanji_korean_label),
                    value = korean,
                )
            }
            if (info.kunYomi.isNotEmpty()) {
                KanjiReadingRow(
                    label = stringResource(Res.string.word_detail_kanji_kun_label),
                    value = info.kunYomi.joinToString(" / "),
                )
            }
            if (info.onYomi.isNotEmpty()) {
                KanjiReadingRow(
                    label = stringResource(Res.string.word_detail_kanji_on_label),
                    value = info.onYomi.joinToString(" / "),
                )
            }
        }
    }
}

@Composable
private fun KanjiReadingRow(
    label: String,
    value: String,
) {
    Text(
        modifier =
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(end = 20.dp),
        text = "$label : $value",
        style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
        color = MolluTheme.colorScheme.black,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun ExampleSection(
    exampleNumber: Int,
    example: Example,
    isTTSPlaying: Boolean,
    onExampleTTSClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.word_detail_example_number, exampleNumber),
            style = MolluTheme.typography.head1.copy(fontWeight = FontWeight.Bold),
            color = MolluTheme.colorScheme.black,
        )
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
                        if (isTTSPlaying) {
                            MolluTheme.colorScheme.black
                        } else {
                            MolluTheme.colorScheme.sub
                        },
                )
            }
        }
    }
}

@Composable
private fun ExampleWords(
    tokens: ImmutableList<ExampleToken>,
    onExampleWordClick: (kanji: String, pronunciation: String) -> Unit,
) {
    if (tokens.isEmpty()) return
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tokens.forEach { token ->
            ExampleWordRow(
                token = token,
                onExampleWordClick = onExampleWordClick,
            )
        }
    }
}

@Composable
private fun ExampleWordRow(
    token: ExampleToken,
    onExampleWordClick: (kanji: String, pronunciation: String) -> Unit,
) {
    val surfaceColor =
        if (token.isJlptWord) {
            MolluTheme.colorScheme.highlightBlue
        } else {
            MolluTheme.colorScheme.black
        }
    // 테두리·배경은 폭을 고정하고 안쪽 내용만 가로로 흐른다. 내용의 최소 폭을 테두리 안쪽 폭으로
    // 잡아, 짧은 행에서는 남는 자리가 생겨 지금처럼 단어와 뜻이 양끝으로 벌어진다.
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MolluTheme.colorScheme.white)
                .clickable(enabled = token.isJlptWord) { onExampleWordClick(token.surface, token.reading) }
                .border(width = 0.5.dp, color = MolluTheme.colorScheme.black),
    ) {
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = ExampleWordRowPadding, vertical = 8.dp)
                    .widthIn(min = maxWidth - ExampleWordRowPadding * 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${token.surface}（${token.reading}）",
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.W300),
                color = surfaceColor,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                // 내용이 넘쳐 양끝 정렬이 무의미해지면 단어와 뜻이 맞붙으므로 최소 간격을 둔다.
                modifier = Modifier.padding(start = ExampleWordRowPadding),
                text = token.meaning,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.W300),
                color = MolluTheme.colorScheme.black,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private val ExampleWordRowPadding = 16.dp

@Preview
@Composable
private fun WordDetailContentPreview() {
    JLPTWordsTheme {
        WordDetailContent(
            state =
                WordDetailState(
                    isLoading = false,
                    kanji = "弾む",
                    pronunciation = "はずむ",
                    partOfSpeech = "동사",
                    meaning = "1. 튀다\n2. 활기를 띠다",
                    jlptLevel = JLPTLevel.N1,
                    kanjiInfos =
                        persistentListOf(
                            KanjiInfo("弾", "탄알 탄", listOf("ダン"), listOf("はず", "ひ")),
                        ),
                    examples =
                        persistentListOf(
                            Example(
                                japanese = "このテニスコートは表面が硬いため、他よりもボールが大きく*弾む*ので注意が必要だ",
                                korean = "이 테니스 코트는 표면이 단단해서 다른 곳보다 공이 훨씬 높게 튀어 오르니 주의가 필요하다.",
                                tokens =
                                    persistentListOf(
                                        ExampleToken("表面", "ひょうめん", "표면", isJlptWord = true),
                                        ExampleToken("硬い", "かたい", "딱딱하다", isJlptWord = false),
                                        ExampleToken("大きく", "おおきく", "크게", isJlptWord = true),
                                        ExampleToken("注意", "ちゅうい", "주의", isJlptWord = false),
                                    ),
                            ),
                            Example(
                                japanese = "久しぶりに会った友人たちと思い出話に花を咲かせ、時間を忘れるほど会話が*弾んだ*",
                                korean = "오랜만에 만난 친구들과 추억담에 꽃을 피우느라, 시간 가는 줄도 모르고 대화가 활기를 띠었다.",
                                tokens =
                                    persistentListOf(
                                        ExampleToken("会う", "あう", "만나다", isJlptWord = true),
                                        ExampleToken("友人", "ゆうじん", "친구", isJlptWord = true),
                                    ),
                            ),
                        ),
                ),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onTTSClick = {},
            onExampleTTSClick = { _, _ -> },
            onExampleWordClick = { _, _ -> },
            onDismissTTSWarningDialog = {},
            onOpenInstallLanguagePack = {},
        )
    }
}
