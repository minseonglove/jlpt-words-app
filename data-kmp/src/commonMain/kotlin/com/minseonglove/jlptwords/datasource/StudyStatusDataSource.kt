package com.minseonglove.jlptwords.datasource

import com.minseonglove.jlptwords.db.util.DecodedRuby
import com.minseonglove.jlptwords.db.util.buildRubySegments
import com.minseonglove.jlptwords.entity.Example
import com.minseonglove.jlptwords.entity.ExampleToken
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.StudyStatus
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.proto.ProtoExample
import com.minseonglove.jlptwords.proto.ProtoExampleToken
import com.minseonglove.jlptwords.proto.ProtoRuby
import com.minseonglove.jlptwords.proto.ProtoStudyStatus
import com.minseonglove.jlptwords.proto.ProtoWord
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.firstOrNull

class StudyStatusDataSource {
    private val dataStore = createStudyStatusDataStore()

    suspend fun getStudyStatus(): StudyStatus? {
        val proto = dataStore.data.firstOrNull() ?: return null
        if (proto.schemaVersion != SCHEMA_VERSION) {
            // 이전 세대 스냅샷은 복원하지 않고 지운다. 지울 때 현재 세대를 찍어 두므로 이 경로는 한 번만 탄다.
            setStudyStatus(null)
            return null
        }
        return proto.takeIf { it.levelCode in 1..5 }?.toStudyStatus()
    }

    suspend fun setStudyStatus(status: StudyStatus?) {
        dataStore.updateData {
            status?.toProto() ?: ProtoStudyStatus(schemaVersion = SCHEMA_VERSION)
        }
    }

    // entity -> proto 변환
    private fun StudyStatus.toProto(): ProtoStudyStatus =
        ProtoStudyStatus(
            schemaVersion = SCHEMA_VERSION,
            sessionId = sessionId,
            sessionPosition = sessionPosition,
            levelCode = level.code,
            words = words.map { it.toProto() },
            totalKnownWordIds = totalKnownWordIds.toList(),
            currentKnownWordIds = currentKnownWordIds.toList(),
            currentPage = currentPage,
            totalElapsedTimeSeconds = totalElapsedTimeSeconds,
            currentElapsedTimeSeconds = currentElapsedTimeSeconds,
            totalWordSize = totalWordSize,
            totalAppearanceCount = totalAppearanceCount,
            roundProgresses = roundProgresses,
        )

    internal fun Word.toProto(): ProtoWord =
        ProtoWord(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            appearanceCount = appearanceCount,
            correctCount = correctCount,
            partOfSpeech = partOfSpeech,
            examples =
                examples.map { ex ->
                    ProtoExample(
                        japanese = ex.japanese,
                        korean = ex.korean,
                        tokens = ex.tokens.map { ProtoExampleToken(surface = it.surface, reading = it.reading, meaning = it.meaning) },
                        furigana = ex.furigana.map { ProtoRuby(text = it.text, reading = it.reading) },
                    )
                },
        )

    // proto -> entity 변환
    private fun ProtoStudyStatus.toStudyStatus(): StudyStatus =
        StudyStatus(
            sessionId = sessionId,
            sessionPosition = sessionPosition,
            level = JLPTLevel.fromCode(levelCode),
            words = words.map { it.toWord() },
            totalKnownWordIds = totalKnownWordIds.toSet(),
            currentKnownWordIds = currentKnownWordIds.toSet(),
            currentPage = currentPage,
            totalElapsedTimeSeconds = totalElapsedTimeSeconds,
            currentElapsedTimeSeconds = currentElapsedTimeSeconds,
            totalWordSize = totalWordSize,
            totalAppearanceCount = totalAppearanceCount,
            roundProgresses = roundProgresses,
        )

    internal fun ProtoWord.toWord(): Word =
        Word(
            id = id,
            kanji = kanji,
            pronunciation = pronunciation,
            meaning = meaning,
            partOfSpeech = partOfSpeech,
            appearanceCount = appearanceCount,
            correctCount = correctCount,
            examples =
                examples.map { ex ->
                    Example(
                        japanese = ex.japanese,
                        korean = ex.korean,
                        tokens =
                            ex.tokens
                                .map { ExampleToken(surface = it.surface, reading = it.reading, meaning = it.meaning) }
                                .toImmutableList(),
                        furigana = buildRubySegments(ex.japanese, ex.furigana.map { DecodedRuby(text = it.text, reading = it.reading) }),
                    )
                },
        )

    companion object {
        /** 현재 저장 포맷 세대. 필드를 더하는 것만으로 복원이 성립하지 않게 되면 올린다. */
        private const val SCHEMA_VERSION = 1
    }
}
