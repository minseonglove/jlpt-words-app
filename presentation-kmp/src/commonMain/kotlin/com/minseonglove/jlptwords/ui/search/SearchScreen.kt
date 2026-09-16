package com.minseonglove.jlptwords.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType
import com.minseonglove.jlptwords.ui.adzero.AdZeroDialog
import com.minseonglove.jlptwords.ui.base.DashedDivider
import com.minseonglove.jlptwords.ui.base.MolluDefaultTopBar
import com.minseonglove.jlptwords.ui.base.MolluScreenScaffold
import com.minseonglove.jlptwords.ui.base.molluPaperBackground
import com.minseonglove.jlptwords.ui.theme.JLPTWordsTheme
import com.minseonglove.jlptwords.ui.theme.MolluTheme
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.search_ascending
import jlptwords.presentation_kmp.generated.resources.search_descending
import jlptwords.presentation_kmp.generated.resources.search_empty_result
import jlptwords.presentation_kmp.generated.resources.search_filter
import jlptwords.presentation_kmp.generated.resources.search_filter_apply
import jlptwords.presentation_kmp.generated.resources.search_level_label
import jlptwords.presentation_kmp.generated.resources.search_placeholder
import jlptwords.presentation_kmp.generated.resources.search_reset
import jlptwords.presentation_kmp.generated.resources.search_script_hiragana
import jlptwords.presentation_kmp.generated.resources.search_script_kanji
import jlptwords.presentation_kmp.generated.resources.search_script_katakana
import jlptwords.presentation_kmp.generated.resources.search_script_label
import jlptwords.presentation_kmp.generated.resources.search_select_all
import jlptwords.presentation_kmp.generated.resources.search_sort
import jlptwords.presentation_kmp.generated.resources.search_sort_accuracy
import jlptwords.presentation_kmp.generated.resources.search_sort_apply
import jlptwords.presentation_kmp.generated.resources.search_sort_dictionary
import jlptwords.presentation_kmp.generated.resources.search_sort_word_number
import jlptwords.presentation_kmp.generated.resources.search_title
import jlptwords.presentation_kmp.generated.resources.search_total_count
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
    navigateToWordDetail: (kanji: String, pronunciation: String) -> Unit = { _, _ -> },
    navigateToBack: () -> Unit = {},
    navigateToSetting: () -> Unit = {},
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToWordDetail -> {
                navigateToWordDetail(sideEffect.kanji, sideEffect.pronunciation)
            }

            SearchSideEffect.NavigateToBack -> {
                navigateToBack()
            }

            SearchSideEffect.NavigateToSetting -> {
                navigateToSetting()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    SearchContent(
        state = state,
        onBackClick = viewModel::onBackClick,
        onAdZeroClick = viewModel::onAdZeroClick,
        onSettingsClick = viewModel::onSettingsClick,
        onQueryChange = viewModel::onQueryChange,
        onWordClick = viewModel::onWordClick,
        onFilterClick = viewModel::onFilterClick,
        onSortClick = viewModel::onSortClick,
        onSortDirectionClick = viewModel::onSortDirectionClick,
        onDraftLevelToggle = viewModel::onDraftLevelToggle,
        onDraftScriptTypeToggle = viewModel::onDraftScriptTypeToggle,
        onAllDraftLevelsSelect = viewModel::onAllDraftLevelsSelect,
        onAllDraftScriptTypesSelect = viewModel::onAllDraftScriptTypesSelect,
        onFilterReset = viewModel::onFilterReset,
        onFilterApply = viewModel::onFilterApply,
        onDraftSortTypeSelect = viewModel::onDraftSortTypeSelect,
        onSortApply = viewModel::onSortApply,
        modifier = modifier,
    )

    if (state.isAdZeroDialogShown) {
        AdZeroDialog(
            onDismissRequest = viewModel::onAdZeroDialogDismiss,
        )
    }
}

@Composable
private fun SearchContent(
    state: SearchState,
    onBackClick: () -> Unit,
    onAdZeroClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onWordClick: (kanji: String, pronunciation: String) -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onSortDirectionClick: () -> Unit,
    onDraftLevelToggle: (JLPTLevel) -> Unit,
    onDraftScriptTypeToggle: (WordScriptType) -> Unit,
    onAllDraftLevelsSelect: () -> Unit,
    onAllDraftScriptTypesSelect: () -> Unit,
    onFilterReset: () -> Unit,
    onFilterApply: () -> Unit,
    onDraftSortTypeSelect: (WordSortType) -> Unit,
    onSortApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MolluTheme {
        MolluScreenScaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MolluTheme.colorScheme.backgroundNormal,
            topBar = {
                MolluDefaultTopBar(
                    title = stringResource(Res.string.search_title),
                    adZeroRemainingHours = state.adZeroRemainingHours,
                    onBackClick = onBackClick,
                    onAdZeroClick = onAdZeroClick,
                    onSettingsClick = onSettingsClick,
                )
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .molluPaperBackground(),
            ) {
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                )
                HorizontalDivider(color = MolluTheme.colorScheme.black)
                when (state.activePanel) {
                    SearchPanel.NONE -> {
                        ResultCountBar(
                            totalCount = state.words.size,
                            isAscending = state.isAscending,
                            onFilterClick = onFilterClick,
                            onSortClick = onSortClick,
                            onSortDirectionClick = onSortDirectionClick,
                        )
                    }

                    SearchPanel.FILTER -> {
                        FilterPanel(
                            draftLevels = state.draftLevels,
                            draftScriptTypes = state.draftScriptTypes,
                            onLevelToggle = onDraftLevelToggle,
                            onScriptTypeToggle = onDraftScriptTypeToggle,
                            onAllLevelsSelect = onAllDraftLevelsSelect,
                            onAllScriptTypesSelect = onAllDraftScriptTypesSelect,
                            onResetClick = onFilterReset,
                            onApplyClick = onFilterApply,
                        )
                    }

                    SearchPanel.SORT -> {
                        SortPanel(
                            draftSortType = state.draftSortType,
                            onSortTypeSelect = onDraftSortTypeSelect,
                            onApplyClick = onSortApply,
                        )
                    }
                }
                HorizontalDivider(color = MolluTheme.colorScheme.black)
                WordListArea(
                    isLoading = state.isLoading,
                    words = state.words,
                    isDimmed = state.activePanel != SearchPanel.NONE,
                    onWordClick = onWordClick,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        textStyle =
            MolluTheme.typography.head2.copy(
                fontWeight = FontWeight.Bold,
                color = MolluTheme.colorScheme.black,
            ),
        singleLine = true,
        cursorBrush = SolidColor(MolluTheme.colorScheme.black),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Box {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.search_placeholder),
                        style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.sub,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun ResultCountBar(
    totalCount: Int,
    isAscending: Boolean,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onSortDirectionClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.search_total_count, totalCount),
            style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.black,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UnderlinedTextButton(
                text = stringResource(Res.string.search_filter),
                onClick = onFilterClick,
            )
            UnderlinedTextButton(
                text = stringResource(Res.string.search_sort),
                onClick = onSortClick,
            )
            // 정렬 방향 링크는 '누르면 전환될 방향' 을 표시한다. 기본(오름차순) 상태에서 "내림차순" 노출.
            UnderlinedTextButton(
                text =
                    stringResource(
                        if (isAscending) {
                            Res.string.search_descending
                        } else {
                            Res.string.search_ascending
                        },
                    ),
                onClick = onSortDirectionClick,
            )
        }
    }
}

@Composable
private fun FilterPanel(
    draftLevels: PersistentSet<JLPTLevel>,
    draftScriptTypes: PersistentSet<WordScriptType>,
    onLevelToggle: (JLPTLevel) -> Unit,
    onScriptTypeToggle: (WordScriptType) -> Unit,
    onAllLevelsSelect: () -> Unit,
    onAllScriptTypesSelect: () -> Unit,
    onResetClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UnderlinedTextButton(
                text = stringResource(Res.string.search_reset),
                onClick = onResetClick,
            )
            Spacer(modifier = Modifier.weight(1f))
            UnderlinedTextButton(
                text = stringResource(Res.string.search_filter_apply),
                onClick = onApplyClick,
            )
        }
        HorizontalDivider(color = MolluTheme.colorScheme.black)
        FilterSection(
            title = stringResource(Res.string.search_level_label),
            options = JLPT_LEVEL_DISPLAY_ORDER,
            optionLabel = { it.name },
            isSelected = { it in draftLevels },
            onOptionClick = onLevelToggle,
            onSelectAllClick = onAllLevelsSelect,
        )
        DashedDivider()
        FilterSection(
            title = stringResource(Res.string.search_script_label),
            options = WORD_SCRIPT_TYPE_OPTIONS,
            optionLabel = { it.label() },
            isSelected = { it in draftScriptTypes },
            onOptionClick = onScriptTypeToggle,
            onSelectAllClick = onAllScriptTypesSelect,
        )
    }
}

@Composable
private fun <T> FilterSection(
    title: String,
    options: ImmutableList<T>,
    optionLabel: @Composable (T) -> String,
    isSelected: (T) -> Boolean,
    onOptionClick: (T) -> Unit,
    onSelectAllClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = MolluTheme.colorScheme.black,
            )
            Text(
                text = stringResource(Res.string.search_select_all),
                style = MolluTheme.typography.caption1.copy(textDecoration = TextDecoration.Underline),
                color = MolluTheme.colorScheme.black,
                modifier = Modifier.clickable(onClick = onSelectAllClick),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            options.forEach { option ->
                Text(
                    text = optionLabel(option),
                    style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                    color =
                        if (isSelected(option)) {
                            MolluTheme.colorScheme.black
                        } else {
                            MolluTheme.colorScheme.sub
                        },
                    modifier = Modifier.clickable { onOptionClick(option) },
                )
            }
        }
    }
}

@Composable
private fun SortPanel(
    draftSortType: WordSortType,
    onSortTypeSelect: (WordSortType) -> Unit,
    onApplyClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            UnderlinedTextButton(
                text = stringResource(Res.string.search_sort_apply),
                onClick = onApplyClick,
            )
        }
        HorizontalDivider(color = MolluTheme.colorScheme.black)
        WordSortType.entries.forEach { sortType ->
            val isSelected = sortType == draftSortType
            Text(
                text = sortType.label(),
                style =
                    MolluTheme.typography.body1.copy(
                        fontWeight =
                            if (isSelected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                    ),
                color =
                    if (isSelected) {
                        MolluTheme.colorScheme.black
                    } else {
                        MolluTheme.colorScheme.sub
                    },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSortTypeSelect(sortType) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

/** 검색어 입력마다 목록까지 재구성되지 않도록 State 전체가 아니라 필요한 값만 받는다. */
@Composable
private fun ColumnScope.WordListArea(
    isLoading: Boolean,
    words: ImmutableList<SearchedWord>,
    isDimmed: Boolean,
    onWordClick: (kanji: String, pronunciation: String) -> Unit,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    // iOS 는 시스템 뒤로가기가 없어 키보드를 내릴 방법이 검색 키뿐이다. 결과를 훑기 시작하면
    // 입력이 끝난 것으로 보고 내려 준다.
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .alpha(if (isDimmed) DIMMED_LIST_ALPHA else 1f),
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MolluTheme.colorScheme.black)
                }
            }

            words.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.search_empty_result),
                        style = MolluTheme.typography.head2.copy(fontWeight = FontWeight.Medium),
                        color = MolluTheme.colorScheme.sub,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    userScrollEnabled = isDimmed.not(),
                ) {
                    itemsIndexed(
                        items = words,
                        key = { _, word -> word.id },
                    ) { index, word ->
                        SearchedWordRow(
                            word = word,
                            isClickEnabled = isDimmed.not(),
                            onWordClick = onWordClick,
                        )
                        if (index < words.lastIndex) {
                            DashedDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedWordRow(
    word: SearchedWord,
    isClickEnabled: Boolean,
    onWordClick: (kanji: String, pronunciation: String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = isClickEnabled) { onWordClick(word.kanji, word.pronunciation) }
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = word.jlptLevel.name,
            style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.highlightBlue,
        )
        Text(
            text = word.kanji,
            style = MolluTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.black,
        )
        if (word.pronunciation != word.kanji) {
            Text(
                text = word.pronunciation,
                style = MolluTheme.typography.body2,
                color = MolluTheme.colorScheme.sub,
            )
        }
        Text(
            text = formatMeaningForRow(word.meaning),
            style = MolluTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
            color = MolluTheme.colorScheme.black,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun UnderlinedTextButton(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style =
            MolluTheme.typography.body2.copy(
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
            ),
        color = MolluTheme.colorScheme.black,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun WordScriptType.label(): String =
    stringResource(
        when (this) {
            WordScriptType.HIRAGANA -> Res.string.search_script_hiragana
            WordScriptType.KATAKANA -> Res.string.search_script_katakana
            WordScriptType.KANJI -> Res.string.search_script_kanji
        },
    )

@Composable
private fun WordSortType.label(): String =
    stringResource(
        when (this) {
            WordSortType.WORD_NUMBER -> Res.string.search_sort_word_number
            WordSortType.DICTIONARY -> Res.string.search_sort_dictionary
            WordSortType.ACCURACY -> Res.string.search_sort_accuracy
        },
    )

/**
 * 목록 한 줄 표시용 뜻 문자열. 줄바꿈으로 구분된 번호 매김 뜻
 * ("1. 튀다\n2. 활기를 띠다")을 쉼표 구분 한 줄("튀다, 활기를 띠다")로 바꾼다.
 */
private fun formatMeaningForRow(meaning: String): String = meaning.lines().joinToString(", ") { it.replace(MEANING_NUMBERING_REGEX, "") }

private val MEANING_NUMBERING_REGEX = Regex("""^\d+\.\s*""")

/** 필터 패널의 급수 표기 순서(디자인 기준 N1 -> N5). */
private val JLPT_LEVEL_DISPLAY_ORDER = JLPTLevel.entries.reversed().toImmutableList()

private val WORD_SCRIPT_TYPE_OPTIONS = WordScriptType.entries.toImmutableList()

private const val DIMMED_LIST_ALPHA = 0.3f

@Preview
@Composable
private fun SearchContentPreview() {
    JLPTWordsTheme {
        SearchContent(
            state =
                SearchState(
                    isLoading = false,
                    words =
                        persistentListOf(
                            SearchedWord(
                                id = 1,
                                kanji = "ござる",
                                pronunciation = "ござる",
                                meaning = "계시다(있다의 존경어)",
                                jlptLevel = JLPTLevel.N1,
                            ),
                            SearchedWord(
                                id = 2,
                                kanji = "代用",
                                pronunciation = "だいよう",
                                meaning = "대용",
                                jlptLevel = JLPTLevel.N1,
                            ),
                            SearchedWord(
                                id = 3,
                                kanji = "デリケート",
                                pronunciation = "でりけーと",
                                meaning = "섬세함",
                                jlptLevel = JLPTLevel.N1,
                            ),
                            SearchedWord(
                                id = 4,
                                kanji = "スラックス",
                                pronunciation = "すらっくす",
                                meaning = "슬랙스, 좁은 바지",
                                jlptLevel = JLPTLevel.N1,
                            ),
                        ),
                    adZeroRemainingHours = 12,
                ),
            onBackClick = {},
            onAdZeroClick = {},
            onSettingsClick = {},
            onQueryChange = {},
            onWordClick = { _, _ -> },
            onFilterClick = {},
            onSortClick = {},
            onSortDirectionClick = {},
            onDraftLevelToggle = {},
            onDraftScriptTypeToggle = {},
            onAllDraftLevelsSelect = {},
            onAllDraftScriptTypesSelect = {},
            onFilterReset = {},
            onFilterApply = {},
            onDraftSortTypeSelect = {},
            onSortApply = {},
        )
    }
}
