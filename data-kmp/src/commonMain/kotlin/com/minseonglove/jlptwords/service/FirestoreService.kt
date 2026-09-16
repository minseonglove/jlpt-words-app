package com.minseonglove.jlptwords.service

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.service.dto.AllContentUpdateDates
import com.minseonglove.jlptwords.service.dto.ContentUpdateDate
import com.minseonglove.jlptwords.service.dto.KanjiInfoDto

expect class FirestoreService {
    suspend fun getWords(level: JLPTLevel): List<Word>

    suspend fun getWordCount(level: JLPTLevel): Int

    /**
     * examples_{level} 청크 문서들의 data(bytes) 필드를 그대로 돌려준다.
     * 내용은 raw deflate 로 압축된 ExampleChunk(protobuf) — 해제·디코드는 공통 코드가 수행한다.
     */
    suspend fun getExampleChunkBlobs(level: JLPTLevel): List<ByteArray>

    /**
     * readings/all 문서의 data(bytes) 필드. 내용은 raw deflate 로 압축된 ReadingsBlob(protobuf).
     * 문서나 필드가 없으면 null — 호출자가 실패로 처리한다(빈 사전으로 교체하지 않도록).
     */
    suspend fun getReadingsBlob(): ByteArray?

    suspend fun getContentUpdateDate(level: JLPTLevel): ContentUpdateDate

    /**
     * content_update_date 컬렉션 전체(급수·요미·한자 날짜)를 1회 조회한다.
     * 문서별 개별 조회의 순차 왕복을 없애기 위한 것. 실패(오프라인 등) 시 [AllContentUpdateDates.EMPTY].
     */
    suspend fun getAllContentUpdateDates(): AllContentUpdateDates

    suspend fun getReadingsUpdateDate(): Long

    suspend fun getKanjiInfo(): List<KanjiInfoDto>

    suspend fun getKanjiUpdateDate(): Long
}
