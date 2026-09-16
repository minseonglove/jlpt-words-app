package com.minseonglove.jlptwords.db.util

/** 예문 토큰 1개(표면형 + 문맥 뜻). reading 은 vocabulary_words 에서 표시 시 조회하므로 여기 담지 않는다. */
data class DecodedToken(
    val surface: String,
    val meaning: String,
)

// 예문 토큰들을 example_sentences.tokens 한 컬럼(TEXT)에 직렬화/역직렬화한다.
// 별도 word_in_example 테이블(토큰당 1행) 대신 예문당 1행으로 묶어 첫 설치 쓰기량을 줄인다.
// 구분자는 일본어/한국어 본문·뜻에 나타나지 않는 ASCII 제어문자(0x1F/0x1E)를 쓴다(JSON 의존성 회피).
private val FIELD_SEP: Char = Char(0x1F) // Unit Separator: surface ↔ meaning
private val RECORD_SEP: Char = Char(0x1E) // Record Separator: 토큰 ↔ 토큰

/**
 * [tokens] 를 순서대로 한 문자열로 직렬화한다. 빈 목록은 빈 문자열.
 * 만약 본문에 구분자(0x1F/0x1E)가 섞여 들어오면 스트림이 깨지므로 방어적으로 제거한다(현재 데이터엔 없음).
 */
fun encodeExampleTokens(tokens: List<DecodedToken>): String = tokens.joinToString(RECORD_SEP.toString()) { "${it.surface.stripSeparators()}$FIELD_SEP${it.meaning.stripSeparators()}" }

private fun String.stripSeparators(): String =
    if (contains(FIELD_SEP) || contains(RECORD_SEP)) {
        replace(FIELD_SEP.toString(), "").replace(RECORD_SEP.toString(), "")
    } else {
        this
    }

/** [encodeExampleTokens] 의 역. 순서를 보존한다. */
fun decodeExampleTokens(raw: String): List<DecodedToken> {
    if (raw.isEmpty()) return emptyList()
    return raw.split(RECORD_SEP).map { record ->
        val sep = record.indexOf(FIELD_SEP)
        if (sep < 0) {
            DecodedToken(surface = record, meaning = "")
        } else {
            DecodedToken(surface = record.substring(0, sep), meaning = record.substring(sep + 1))
        }
    }
}
