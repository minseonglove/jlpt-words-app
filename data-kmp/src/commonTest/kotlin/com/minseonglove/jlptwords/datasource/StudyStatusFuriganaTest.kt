package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.entity.RubySegment
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.proto.ProtoWord
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * StudyStatusDataSource 의 Word.toProto()/ProtoWord.toWord() 는 원래 private 이었으나, 이 왕복
 * 배선(특히 furigana 매핑)의 회귀를 테스트가 실제로 잡을 수 있도록 internal 로 가시성을 낮췄다
 * (Kotlin 의 test-friend-path 로 commonTest 에서 접근 가능. 나머지 private 함수는 그대로 둔다).
 * `StudyStatusDataSource()` 생성 자체는 안전하다 — 내부의 `createStudyStatusDataStore()` 가 만드는
 * DataStoreImpl/OkioStorage 는 producePath 람다를 지연 평가하므로, `getStudyStatus()`/`setStudyStatus()`
 * 를 호출하지 않는 한 미초기화 `appContext` 에 접근하지 않는다.
 */
class StudyStatusFuriganaTest {
    private val dataSource = StudyStatusDataSource()

    @Test
    fun `Word toProto 에서 ADAPTER 왕복 후 toWord 로 복원하면 furigana 의 하이라이트 구간이 원본과 일치한다`() {
        // "空気を*吸う*。" — 마커 안 구간(吸う)만 하이라이트, 。는 마커 밖이므로 별도 세그먼트여야 한다.
        val japanese = "空気を*吸う*。"
        val originalSegments =
            persistentListOf(
                RubySegment(text = "空気", reading = "くうき", isHighlight = false),
                RubySegment(text = "を", reading = "", isHighlight = false),
                RubySegment(text = "吸", reading = "す", isHighlight = true),
                RubySegment(text = "う", reading = "", isHighlight = true),
                RubySegment(text = "。", reading = "", isHighlight = false),
            )
        val word =
            Word(
                id = 1,
                kanji = "空気",
                pronunciation = "くうき",
                meaning = "공기",
                examples =
                    listOf(
                        Example(japanese = japanese, korean = "공기를 마신다.", furigana = originalSegments),
                    ),
            )

        val protoWord = with(dataSource) { word.toProto() }
        val decodedProtoWord = ProtoWord.ADAPTER.decode(ProtoWord.ADAPTER.encode(protoWord))
        val restoredWord = with(dataSource) { decodedProtoWord.toWord() }

        assertEquals(originalSegments, restoredWord.examples[0].furigana)
    }

    @Test
    fun `furigana 가 없는 예문은 왕복 후에도 빈 목록이다`() {
        val word =
            Word(
                id = 2,
                kanji = "水",
                pronunciation = "みず",
                meaning = "물",
                examples = listOf(Example(japanese = "水を飲む。", korean = "물을 마신다.")),
            )

        val protoWord = with(dataSource) { word.toProto() }
        val decodedProtoWord = ProtoWord.ADAPTER.decode(ProtoWord.ADAPTER.encode(protoWord))
        val restoredWord = with(dataSource) { decodedProtoWord.toWord() }

        assertEquals(emptyList(), restoredWord.examples[0].furigana)
    }
}
