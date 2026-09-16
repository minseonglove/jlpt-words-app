package com.minseonglove.jlptwords.db.util

/** 예문 루비 세그먼트 1개. [reading] 이 비어 있으면 루비 없는 본문 구간. */
data class DecodedRuby(
    val text: String,
    val reading: String,
)

// 예문 루비를 example_sentences.furigana 한 컬럼(TEXT)에 직렬화/역직렬화한다.
// 구분자·방어 규칙은 ExampleTokenCodec 과 동일하다 (0x1F 필드, 0x1E 레코드).
private val FIELD_SEP: Char = Char(0x1F)
private val RECORD_SEP: Char = Char(0x1E)

fun encodeFurigana(segments: List<DecodedRuby>): String = segments.joinToString(RECORD_SEP.toString()) { "${it.text.stripSeparators()}$FIELD_SEP${it.reading.stripSeparators()}" }

private fun String.stripSeparators(): String =
    if (contains(FIELD_SEP) || contains(RECORD_SEP)) {
        replace(FIELD_SEP.toString(), "").replace(RECORD_SEP.toString(), "")
    } else {
        this
    }

fun decodeFurigana(raw: String): List<DecodedRuby> {
    if (raw.isEmpty()) return emptyList()
    return raw.split(RECORD_SEP).map { record ->
        val sep = record.indexOf(FIELD_SEP)
        if (sep < 0) {
            DecodedRuby(text = record, reading = "")
        } else {
            DecodedRuby(text = record.substring(0, sep), reading = record.substring(sep + 1))
        }
    }
}
