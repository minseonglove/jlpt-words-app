package com.minseonglove.jlptwords.db.snapshot

import com.minseonglove.jlptwords.service.dto.ExampleRubyDto
import com.minseonglove.jlptwords.service.dto.ExampleSentenceDto
import com.minseonglove.jlptwords.service.dto.ExampleTokenDto
import java.io.File

/**
 * 단어 데이터 TSV 파서 — scripts/firestore_upload/parsing.py 의 Kotlin 포팅.
 * 스냅샷 생성 전용이므로 파싱 규칙을 바꿀 때는 parsing.py 와 함께 수정한다.
 */
object TsvCatalog {
    /** 단어 1행 (번호 컬럼은 파서가 무시한다) */
    data class ParsedWord(
        val kanji: String,
        val pronunciation: String,
        val meaning: String,
        val partOfSpeech: String,
    )

    data class ParsedKanji(
        val kanji: String,
        val koreanHanja: String?,
        val onYomi: String?,
        val kunYomi: String?,
    )

    /**
     * n{level}words.tsv → 단어 목록. 헤더(첫 컬럼이 숫자가 아님)는 스킵.
     * 뜻의 개행 표기(`\\n`/`\n` 혼재)는 업로드 파서·앱 파서가 나눠 변환하므로 여기서 둘 다 변환한다.
     * 같은 표기(kanji)의 중복 행은 첫 행만 남긴다 — 앱 다운로드 경로(getValidWords)와 동일 규칙.
     */
    fun parseWords(file: File): List<ParsedWord> {
        val seen = mutableSetOf<String>()
        return file.readLines().mapNotNull { line ->
            val cols = line.split("\t")
            if (cols.size < 5) return@mapNotNull null
            if (cols[0].trim().toIntOrNull() == null) return@mapNotNull null
            val kanji = cols[1].trim()
            if (kanji.isEmpty() || !seen.add(kanji)) return@mapNotNull null
            ParsedWord(
                kanji = kanji,
                pronunciation = cols[2].trim(),
                meaning =
                    cols[3]
                        .trim()
                        .replace("\\\\n", "\n")
                        .replace("\\n", "\n"),
                partOfSpeech = cols[4].trim(),
            )
        }
    }

    /**
     * all_word_meanings.tsv → {급수 key: 예문 DTO 목록}.
     * 같은 (급수, 표기, meaning_index) 행들을 한 예문으로 묶고 각 행의 토큰을 모은다.
     * 예문 순서는 meaning_index 숫자 순 — 업로드 파서(parse_examples)와 동일 규칙.
     * [furigana] 는 (급수, 표기, meaning_index) 키로 parseFurigana 결과를 조회해 DTO 에 채운다.
     */
    fun parseExamples(
        file: File,
        furigana: Map<Triple<String, String, Int>, List<ExampleRubyDto>> = emptyMap(),
    ): Map<String, List<ExampleSentenceDto>> {
        // level -> kanji -> meaningIndex -> (jp, ko, tokens)
        data class Acc(
            val jp: String,
            val ko: String,
            val tokens: MutableList<ExampleTokenDto>,
        )
        val acc = LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<Int, Acc>>>()
        file.readLines().forEach { line ->
            val cols = line.split("\t")
            if (cols.size < 9) return@forEach
            val (level, sourceWord) = cols[0] to cols[1]
            val meaningIndex = cols[5].trim().toIntOrNull() ?: return@forEach
            val byKanji = acc.getOrPut(level) { LinkedHashMap() }
            val byIndex = byKanji.getOrPut(sourceWord) { LinkedHashMap() }
            val example = byIndex.getOrPut(meaningIndex) { Acc(jp = cols[3], ko = cols[4], tokens = mutableListOf()) }
            example.tokens.add(ExampleTokenDto(surface = cols[6], meaning = cols[8]))
        }
        return acc.mapValues { (level, byKanji) ->
            byKanji.flatMap { (kanji, byIndex) ->
                byIndex.entries
                    .sortedBy { it.key }
                    .mapIndexed { order, (meaningIndex, example) ->
                        ExampleSentenceDto(
                            sourceWordKanji = kanji,
                            japanese = example.jp,
                            korean = example.ko,
                            order = order,
                            tokens = example.tokens,
                            furigana = furigana[Triple(level, kanji, meaningIndex)].orEmpty(),
                        )
                    }
            }
        }
    }

    private val kanjiRun = Regex("[㐀-鿿々〆ヶ〇]+")

    /**
     * example_furigana.tsv → {(급수, 표기, meaning_index): 루비 세그먼트}.
     * 주석 파싱 규칙은 scripts/firestore_upload/parsing.py 의 parse_furigana 와 동일해야 한다
     * (마커 경계 분할·제거, 루비는 직전 한자 런 귀속). 규칙 변경 시 함께 수정한다.
     */
    fun parseFurigana(file: File): Map<Triple<String, String, Int>, List<ExampleRubyDto>> =
        file
            .readLines()
            .mapNotNull { line ->
                val cols = line.split("\t")
                if (cols.size < 4) return@mapNotNull null
                val midx = cols[2].trim().toIntOrNull() ?: return@mapNotNull null
                Triple(cols[0], cols[1], midx) to annotatedToSegments(cols[3])
            }.toMap()

    private fun annotatedToSegments(annotated: String): List<ExampleRubyDto> {
        val segments = mutableListOf<ExampleRubyDto>()
        val plain = StringBuilder()

        fun flush() {
            if (plain.isNotEmpty()) {
                segments.add(ExampleRubyDto(text = plain.toString(), reading = ""))
                plain.clear()
            }
        }

        var i = 0
        while (i < annotated.length) {
            when (val ch = annotated[i]) {
                '*' -> {
                    flush()
                    i++
                }
                '[' -> {
                    val end = annotated.indexOf(']', i)
                    require(end > i) { "닫는 대괄호 없음: $annotated" }
                    val ruby = annotated.substring(i + 1, end)
                    val match = kanjiRun.findAll(plain).lastOrNull()
                    require(match != null && match.range.last == plain.length - 1) { "루비 앞에 한자 런이 없음: $annotated" }
                    val base = plain.substring(match.range.first)
                    plain.setLength(match.range.first)
                    flush()
                    segments.add(ExampleRubyDto(text = base, reading = ruby))
                    i = end + 1
                }
                else -> {
                    plain.append(ch)
                    i++
                }
            }
        }
        flush()
        return segments
    }

    /** all_word_meanings.tsv → 전역 요미 사전 (첫 등장 우선) — 업로드 파서(parse_readings)와 동일 규칙. */
    fun parseReadings(file: File): Map<String, String> {
        val readings = LinkedHashMap<String, String>()
        file.readLines().forEach { line ->
            val cols = line.split("\t")
            if (cols.size < 9) return@forEach
            readings.getOrPut(cols[6]) { cols[7] }
        }
        return readings
    }

    /** kanji_readings.tsv → 한자 사전. 음독·훈독은 ·구분 문자열, 빈 값은 null — upload_kanji.py 와 동일 규칙. */
    fun parseKanji(file: File): List<ParsedKanji> =
        file
            .readLines()
            .drop(1) // 헤더
            .mapNotNull { line ->
                val cols = line.split("\t")
                if (cols.size < 4) return@mapNotNull null
                val kanji = cols[0].trim()
                if (kanji.isEmpty()) return@mapNotNull null
                ParsedKanji(
                    kanji = kanji,
                    koreanHanja = cols[1].trim().ifEmpty { null },
                    onYomi = cols[2].cleanYomi(),
                    kunYomi = cols[3].cleanYomi(),
                )
            }

    private fun String.cleanYomi(): String? =
        split("·")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("·")
            .ifEmpty { null }
}
