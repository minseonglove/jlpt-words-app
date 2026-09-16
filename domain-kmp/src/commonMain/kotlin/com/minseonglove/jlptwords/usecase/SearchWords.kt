package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.WordScriptType
import com.minseonglove.jlptwords.entity.WordSortType
import com.minseonglove.jlptwords.repository.WordRepository

/**
 * 단어 검색 화면용 검색. 검색어(표기·발음·뜻 부분일치, 빈 검색어면 전체)로 조회한 뒤
 * 급수·표기 필터와 정렬(단어 번호/사전/정답률 순, 오름·내림차순)을 적용한다.
 * 정답률 순에서는 학습 이력이 없는 단어(정답률 비교 불가)를 정렬 방향과 무관하게 항상 뒤에 둔다.
 */
class SearchWords(
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(
        query: String,
        levels: Set<JLPTLevel>,
        scriptTypes: Set<WordScriptType>,
        sortType: WordSortType,
        isAscending: Boolean,
    ): List<SearchedWord> {
        // 저장소 계약(searchWords KDoc)상 단어 번호(id) 오름차순으로 도착하며, filter 는 순서를 보존한다.
        val words =
            wordRepository
                .searchWords(query.trim())
                .filter { it.jlptLevel in levels && it.scriptType in scriptTypes }
        return when (sortType) {
            WordSortType.WORD_NUMBER -> {
                if (isAscending) words else words.reversed()
            }

            WordSortType.DICTIONARY -> {
                val comparator = compareBy<SearchedWord>({ it.pronunciation }, { it.id })
                words.sortedWith(if (isAscending) comparator else comparator.reversed())
            }

            WordSortType.ACCURACY -> {
                // 학습 이력이 없는 단어(정답률 비교 불가)는 방향과 무관하게 뒤에 둔다. partition 은 id 순서를 보존한다.
                val (studied, unstudied) = words.partition { it.accuracy != null }
                val comparator = compareBy<SearchedWord>({ it.accuracy }, { it.id })
                studied.sortedWith(if (isAscending) comparator else comparator.reversed()) + unstudied
            }
        }
    }
}
