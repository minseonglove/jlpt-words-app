package com.minseonglove.jlptwords.service

import com.minseonglove.jlptwords.proto.ExampleChunk
import com.minseonglove.jlptwords.util.inflateRaw
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 업로드 스크립트(scripts/firestore_upload/example_blob.py)가 인코딩·압축한 골든 blob 을
 * 앱 경로(raw deflate 해제 → Wire 디코드)로 복원하는 교차 검증.
 * proto 스키마나 인코더를 바꾸면 골든 blob 을 재생성해야 한다 (example_blob.py 주석 참고).
 */
@OptIn(ExperimentalEncodingApi::class)
class ExampleChunkCodecTest {
    // example_blob.build_blobs 결과 (食べる 예문 2건 + 水 예문 1건)
    private val goldenBlob =
        "VY/NCoJAFIUpKfBWRENBSItwG7TohXoVHVtUrrIgCsLAohZBUrkYiF7mQiDjQzReC201cOY7PxdGoL3DBxtAUz3IvTi4IbfQ4qwlL89kN+nL" +
            "0zRx99I9DI0KwTArgx4HPtoCucu2JeiivYqDK9lJtl9ob9KQtgyPFHIWyTyS7pRyGlDNHExT/0YP6kUfqxdpU5Uq2GyAlhqUUfUsSeWe" +
            "CanqM0X4GSjMGuh5/xg66CzQuRe2+dl1IvoNW+eriFWrREQ5P+tfDxV8cz4="

    @Test
    fun `파이썬 인코더의 골든 blob 을 해제·디코드하면 원본 예문이 복원된다`() {
        val chunk = ExampleChunk.ADAPTER.decode(inflateRaw(Base64.decode(goldenBlob)))

        assertEquals(2, chunk.words.size)

        // 인코더가 한자 코드포인트 순으로 정렬한다 (水 U+6C34 < 食 U+98DF)
        val mizu = chunk.words[0]
        val taberu = chunk.words[1]
        assertEquals("水", mizu.kanji)
        assertEquals("食べる", taberu.kanji)

        assertEquals(1, mizu.examples.size)
        assertEquals("水を飲む。", mizu.examples[0].japanese)
        assertEquals("물을 마신다.", mizu.examples[0].korean)
        assertEquals("水", mizu.examples[0].tokens[0].surface)
        // 빈 문자열 필드는 인코더가 생략 — proto3 기본값 "" 로 복원돼야 한다
        assertEquals("", mizu.examples[0].tokens[0].meaning)

        assertEquals(2, taberu.examples.size)
        assertEquals("ご飯を食べます。", taberu.examples[0].japanese)
        assertEquals("밥을 먹습니다.", taberu.examples[0].korean)
        assertEquals(2, taberu.examples[0].tokens.size)
        assertEquals("ご飯", taberu.examples[0].tokens[0].surface)
        assertEquals("밥", taberu.examples[0].tokens[0].meaning)
        assertEquals("食べます", taberu.examples[0].tokens[1].surface)
        assertEquals("먹습니다", taberu.examples[0].tokens[1].meaning)
        assertEquals("パンを食べた。", taberu.examples[1].japanese)
        assertEquals("빵", taberu.examples[1].tokens[0].meaning)

        // furigana (신규 field 4)
        val taberuFurigana = taberu.examples[0].furigana
        assertEquals(6, taberuFurigana.size)
        assertEquals("飯", taberuFurigana[1].text)
        assertEquals("はん", taberuFurigana[1].reading)
        assertEquals("ご", taberuFurigana[0].text)
        assertEquals("", taberuFurigana[0].reading)
        // furigana 없는 예문은 빈 리스트로 복원
        assertEquals(0, mizu.examples[0].furigana.size)
    }

    @Test
    fun `빈 입력은 빈 결과로 해제된다`() {
        assertEquals(0, inflateRaw(ByteArray(0)).size)
    }
}
