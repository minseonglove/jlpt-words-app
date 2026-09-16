package com.minseonglove.jlptwords.service

import com.minseonglove.jlptwords.proto.ReadingsBlob
import com.minseonglove.jlptwords.util.inflateRaw
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 업로드 스크립트(scripts/firestore_upload/example_blob.py)가 인코딩·압축한 요미 사전 골든 blob 을
 * 앱 경로(raw deflate 해제 → Wire 디코드)로 복원하는 교차 검증.
 * proto 스키마나 인코더를 바꾸면 골든 blob 을 재생성해야 한다 (example_blob.py 주석 참고).
 */
@OptIn(ExperimentalEncodingApi::class)
class ReadingsBlobCodecTest {
    // example_blob.build_readings_blob 결과 (표면형 4건, surface 코드포인트 정렬)
    private val goldenBlob =
        "4xLj4nzcvOpx8+bHTVuEOB83dT1umvy4cQqXBhfPk719zzobnu6b/qKhVUjiceOyx40tjxunP25qf9y44nFjP5jdzMXLxfxswxYhtseN" +
            "+x83zuICGvhy8fzHjTsfN3UDDWyEMgE="

    @Test
    fun `파이썬 인코더의 골든 blob 을 해제·디코드하면 요미 사전이 복원된다`() {
        val readings =
            ReadingsBlob.ADAPTER
                .decode(inflateRaw(Base64.decode(goldenBlob)))
                .entries
                .associate { it.surface to it.reading }

        assertEquals(4, readings.size)
        assertEquals("たべる", readings["食べる"])
        assertEquals("みず", readings["水"])
        assertEquals("りんご", readings["リンゴ"])
        assertEquals("ていしょとくしゃ", readings["低所得者"])
    }
}
