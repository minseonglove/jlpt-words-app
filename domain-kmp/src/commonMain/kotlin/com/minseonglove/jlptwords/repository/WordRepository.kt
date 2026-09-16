package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.Word

interface WordRepository {
    suspend fun increaseAppearanceCount(
        wordId: Int,
    )

    suspend fun increaseCorrectAndAppearanceCount(
        wordId: Int,
    )

    /**
     * (표기, 발음)으로 단어 1건을 전체 예문(예문 토큰의 JLPT 여부 포함)과 함께 조회한다.
     * 동형이의어(같은 표기, 다른 발음·뜻)를 구분하기 위해 발음까지 받는다.
     * 발음은 `・` 병기 항목 중 하나와의 일치도 허용한다. 해당 단어가 없으면 null.
     */
    suspend fun getWord(
        kanji: String,
        pronunciation: String,
    ): Word?

    /** 단어가 속한 JLPT 레벨 중 가장 높은 급수(N1쪽)를 반환한다. 단어가 있으면 매핑도 항상 존재하므로 non-null. */
    suspend fun getJlptLevel(wordId: Int): JLPTLevel

    /**
     * 검색어(표기·발음·뜻 부분일치)에 매칭되는 모든 단어를 급수·통계와 함께 조회한다.
     * 빈 검색어면 전체 단어를 반환한다. 단어 번호(id) 순으로 정렬된다.
     */
    suspend fun searchWords(query: String): List<SearchedWord>
}
