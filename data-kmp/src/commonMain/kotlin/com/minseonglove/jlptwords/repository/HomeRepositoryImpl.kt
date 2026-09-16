package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.DailyExampleRecord
import com.minseonglove.jlptwords.datasource.PreferenceDataSource
import com.minseonglove.jlptwords.datasource.StudiedWordSnapshot
import com.minseonglove.jlptwords.datasource.WordDataSource
import com.minseonglove.jlptwords.entity.DailyExample
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.OverviewValue
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.util.getCurrentDaysFromEpoch

class HomeRepositoryImpl(
    private val wordDataSource: WordDataSource,
    private val preferenceDataSource: PreferenceDataSource,
) : HomeRepository {
    override suspend fun getTodayExample(
        level: JLPTLevel,
    ): DailyExample? {
        val today = getCurrentDaysFromEpoch()

        val stored = preferenceDataSource.getDailyExampleRecord()
        if (stored != null && stored.day == today && stored.levelCode == level.code) {
            val word =
                wordDataSource.getWordWithExamplesForLevel(
                    kanji = stored.kanji,
                    pronunciation = stored.pronunciation,
                    levelCode = level.code,
                )
            val example = word?.examples?.getOrNull(stored.exampleOrder)
            if (example != null) {
                return DailyExample(
                    kanji = word.kanji,
                    pronunciation = word.pronunciation,
                    japanese = example.japanese,
                    korean = example.korean,
                    furigana = example.furigana,
                )
            }
            // 동기화 등으로 저장된 예문이 사라졌으면 아래에서 새로 추첨한다.
        }

        val word = wordDataSource.getRandomWordHavingExamples(level.code) ?: return null
        val wordWithExamples =
            wordDataSource
                .attachExamples(listOf(word), level.code)
                .firstOrNull()
                ?.takeIf { it.examples.isNotEmpty() }
                ?: return null
        val exampleOrder = wordWithExamples.examples.indices.random()

        preferenceDataSource.setDailyExampleRecord(
            DailyExampleRecord(
                day = today,
                levelCode = level.code,
                kanji = wordWithExamples.kanji,
                pronunciation = wordWithExamples.pronunciation,
                exampleOrder = exampleOrder,
            ),
        )
        return wordWithExamples.toDailyExample(exampleOrder)
    }

    override suspend fun getStudiedWordCount(): OverviewValue {
        val today = getCurrentDaysFromEpoch()
        val currentCount = wordDataSource.getStudiedWordCount()

        val snapshot = preferenceDataSource.getStudiedWordSnapshot()
        val baselineValue =
            when {
                snapshot == null -> currentCount
                snapshot.baselineDay == today -> snapshot.baselineValue
                // 날짜가 바뀌면 직전 관측값이 자정 기준값이 된다.
                else -> snapshot.lastSeenValue
            }
        val newSnapshot =
            StudiedWordSnapshot(
                baselineValue = baselineValue,
                baselineDay = today,
                lastSeenValue = currentCount,
            )
        // 변화가 없으면 디스크 쓰기를 생략한다(홈 진입마다 호출되므로).
        if (newSnapshot != snapshot) {
            preferenceDataSource.setStudiedWordSnapshot(newSnapshot)
        }
        return OverviewValue(
            value = currentCount,
            isUpdatedToday = currentCount != baselineValue,
        )
    }

    private fun Word.toDailyExample(
        exampleOrder: Int,
    ): DailyExample? {
        val example = examples.getOrNull(exampleOrder) ?: return null
        return DailyExample(
            kanji = kanji,
            pronunciation = pronunciation,
            japanese = example.japanese,
            korean = example.korean,
            furigana = example.furigana,
        )
    }
}
