package com.minseonglove.jlptwords.entity

/**
 * 단어 상세 화면이 사용하는 단어 상세 정보.
 *
 * @property word 전체 예문(예문 토큰의 JLPT 여부 포함)까지 채워진 단어
 * @property kanjiInfos 단어 표기에 포함된 한자들의 분해 정보 (출현 순서)
 * @property jlptLevel 표제어가 속한 JLPT 레벨 중 가장 높은 급수(N1쪽)
 */
data class WordDetail(
    val word: Word,
    val kanjiInfos: List<KanjiInfo>,
    val jlptLevel: JLPTLevel,
)
