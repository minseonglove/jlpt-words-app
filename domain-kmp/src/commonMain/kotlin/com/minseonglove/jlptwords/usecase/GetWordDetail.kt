package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.entity.WordDetail
import com.minseonglove.jlptwords.repository.KanjiInfoRepository
import com.minseonglove.jlptwords.repository.WordRepository
import com.minseonglove.jlptwords.util.extractKanji

/**
 * (표기, 발음)으로 단어 상세 정보(단어 + 전체 예문 + 한자 분해)를 조회한다.
 * 동형이의어(같은 표기, 다른 발음·뜻)를 구분하기 위해 발음까지 받는다.
 * 해당하는 단어가 없으면 null을 반환한다.
 */
class GetWordDetail(
    private val wordRepository: WordRepository,
    private val kanjiInfoRepository: KanjiInfoRepository,
) {
    suspend operator fun invoke(
        kanji: String,
        pronunciation: String,
    ): WordDetail? {
        val word = wordRepository.getWord(kanji, pronunciation) ?: return null
        val kanjiInfos = kanjiInfoRepository.getKanjiInfos(extractKanji(word.kanji))
        val jlptLevel = wordRepository.getJlptLevel(word.id)
        return WordDetail(word, kanjiInfos, jlptLevel)
    }
}
