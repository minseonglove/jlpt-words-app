package com.minseonglove.jlptwords.util

/**
 * CJK 통합 한자(기본 블록) 유니코드 범위.
 * 현재 JLPT N1~N5 단어의 고유 한자 2135자를 전수 확인한 결과 전부 이 범위에 속한다
 * (확장 블록·이체자·서로게이트 페어 없음). 데이터에 확장 한자가 추가되면 코드포인트 순회 방식으로 바꿔야 한다.
 */
private val CJK_UNIFIED_IDEOGRAPHS = 0x4E00..0x9FFF

/**
 * [char] 가 한자(CJK 통합 한자)인지 여부.
 * 한자 판별이 필요한 곳은 이 함수를 사용해 판별 기준을 한 곳으로 유지한다
 * (확장 한자 유입 시 여기만 코드포인트 순회 방식으로 바꾼다).
 */
internal fun isKanji(char: Char): Boolean = char.code in CJK_UNIFIED_IDEOGRAPHS

/**
 * 단어 표기에서 한자(CJK 통합 한자)만 출현 순서대로 추출한다.
 * 히라가나·가타카나 등은 제외하며, 중복 한자는 첫 등장만 남긴다.
 *
 * 예: "肝要" -> ["肝", "要"], "諦める" -> ["諦"], "あきらめる" -> []
 */
fun extractKanji(text: String): List<String> =
    text
        .filter { isKanji(it) }
        .map { it.toString() }
        .distinct()
