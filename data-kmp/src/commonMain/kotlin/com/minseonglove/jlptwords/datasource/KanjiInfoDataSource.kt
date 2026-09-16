package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.dao.KanjiInfoDao
import com.minseonglove.jlptwords.db.entity.KanjiInfoEntity
import com.minseonglove.jlptwords.entity.KanjiInfo
import com.minseonglove.jlptwords.service.FirestoreService
import com.minseonglove.jlptwords.service.dto.KanjiInfoDto

/** 음독·훈독 다중 값을 한 컬럼에 저장할 때 쓰는 구분자. 저장(toEntity)·조회(toDomain)가 공유한다. */
private const val YOMI_DELIMITER = "·"

class KanjiInfoDataSource(
    private val kanjiInfoDao: KanjiInfoDao,
    private val firestoreService: FirestoreService,
) {
    suspend fun getKanjiUpdateDate(): Long = firestoreService.getKanjiUpdateDate()

    suspend fun getLocalKanjiCount(): Int = kanjiInfoDao.getCount()

    suspend fun syncKanjiInfo() {
        val dtos = firestoreService.getKanjiInfo()
        kanjiInfoDao.upsertAll(dtos.map { it.toEntity() })
    }

    /**
     * 입력 [kanjis] 순서를 보존하며 사전 정보를 조회한다. 미등재 한자는 결과에서 제외된다.
     * 단어 1개의 고유 한자 수는 한 자릿수라 IN 절 변수 한도(999)에 닿지 않으므로 청크 분할은 두지 않는다.
     */
    suspend fun getKanjiInfos(kanjis: List<String>): List<KanjiInfo> {
        if (kanjis.isEmpty()) return emptyList()
        val byKanji = kanjiInfoDao.getByKanjis(kanjis).associateBy { it.kanji }
        return kanjis.mapNotNull { byKanji[it]?.toDomain() }
    }

    private fun KanjiInfoEntity.toDomain(): KanjiInfo =
        KanjiInfo(
            kanji = kanji,
            koreanHanja = koreanHanja,
            onYomi = onYomi?.split(YOMI_DELIMITER) ?: emptyList(),
            kunYomi = kunYomi?.split(YOMI_DELIMITER) ?: emptyList(),
        )

    private fun KanjiInfoDto.toEntity(): KanjiInfoEntity =
        KanjiInfoEntity(
            kanji = kanji,
            koreanHanja = koreanHanja,
            onYomi = onYomi.joinToString(YOMI_DELIMITER).ifEmpty { null },
            kunYomi = kunYomi.joinToString(YOMI_DELIMITER).ifEmpty { null },
        )
}
