package com.minseonglove.jlptwords.service

import com.google.firebase.firestore.FirebaseFirestore
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.service.dto.AllContentUpdateDates
import com.minseonglove.jlptwords.service.dto.ContentUpdateDate
import com.minseonglove.jlptwords.service.dto.KanjiInfoDto
import kotlinx.coroutines.tasks.await

actual class FirestoreService(
    private val firestore: FirebaseFirestore,
) {
    actual suspend fun getWords(level: JLPTLevel): List<Word> {
        val snapshot = firestore.collection("words_${level.key}").get().await()
        // 문서 1개 = 단어 청크({words: [...]}). 문서 id(청크 index)를 숫자 순으로
        // 이어 붙여 행 순서를 복원한다 (문자열 정렬은 "10" < "2" 가 되므로 금지).
        val words = mutableListOf<Word>()
        // id 가 숫자가 아닌 문서는 청크가 아니므로 건너뛴다(toInt 로 두면 예외가 난다).
        val chunkDocs =
            snapshot.documents
                .mapNotNull { doc -> doc.id.toIntOrNull()?.let { it to doc } }
                .sortedBy { it.first }
        for ((_, doc) in chunkDocs) {
            val chunk = doc.get(WORDS_KEY) as? List<*> ?: continue
            for (item in chunk) {
                val word = (item as? Map<*, *>)?.toWord(id = words.size) ?: continue
                words.add(word)
            }
        }
        return getValidWords(words)
    }

    actual suspend fun getWordCount(level: JLPTLevel): Int {
        // 단어가 청크 문서로 묶여 있어 문서 수 ≠ 단어 수. 업로드 시 기록한 메타데이터를 읽는다.
        val doc =
            firestore
                .collection("content_update_date")
                .document(level.key)
                .get()
                .await()
        return (doc.getLong(WORD_COUNT_KEY) ?: 0L).toInt()
    }

    actual suspend fun getExampleChunkBlobs(level: JLPTLevel): List<ByteArray> {
        val snapshot = firestore.collection("examples_${level.key}").get().await()
        return snapshot.documents.mapNotNull { it.getBlob(DATA_KEY)?.toBytes() }
    }

    actual suspend fun getReadingsBlob(): ByteArray? {
        val doc =
            firestore
                .collection("readings")
                .document("all")
                .get()
                .await()
        return doc.getBlob(DATA_KEY)?.toBytes()
    }

    actual suspend fun getContentUpdateDate(level: JLPTLevel): ContentUpdateDate {
        return runCatching {
            val doc =
                firestore
                    .collection("content_update_date")
                    .document(level.key)
                    .get()
                    .await()
            ContentUpdateDate(
                wordsDate = doc.getLong("wordsDate") ?: 0L,
                examplesDate = doc.getLong("examplesDate") ?: 0L,
            )
        }.getOrDefault(ContentUpdateDate(0L, 0L))
    }

    actual suspend fun getReadingsUpdateDate(): Long {
        return runCatching {
            firestore
                .collection("content_update_date")
                .document("readings")
                .get()
                .await()
                .getLong("date") ?: 0L
        }.getOrDefault(0L)
    }

    actual suspend fun getAllContentUpdateDates(): AllContentUpdateDates {
        return runCatching {
            val snapshot = firestore.collection("content_update_date").get().await()
            val docs = snapshot.documents.associateBy { it.id }
            AllContentUpdateDates(
                byLevel =
                    JLPTLevel.entries.associateWith { level ->
                        val doc = docs[level.key]
                        ContentUpdateDate(
                            wordsDate = doc?.getLong("wordsDate") ?: 0L,
                            examplesDate = doc?.getLong("examplesDate") ?: 0L,
                        )
                    },
                readingsDate = docs["readings"]?.getLong("date") ?: 0L,
                kanjiDate = docs["kanji"]?.getLong("date") ?: 0L,
            )
        }.getOrDefault(AllContentUpdateDates.EMPTY)
    }

    actual suspend fun getKanjiInfo(): List<KanjiInfoDto> {
        val doc =
            firestore
                .collection("kanji_info")
                .document("all")
                .get()
                .await()
        val data = doc.data ?: return emptyList()
        return data.mapNotNull { (kanji, value) ->
            if (kanji.isEmpty()) return@mapNotNull null
            val entry = value as? Map<*, *> ?: return@mapNotNull null
            KanjiInfoDto(
                kanji = kanji,
                koreanHanja = entry["korean_hanja"] as? String,
                onYomi = (entry["on_yomi"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                kunYomi = (entry["kun_yomi"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            )
        }
    }

    actual suspend fun getKanjiUpdateDate(): Long {
        return runCatching {
            firestore
                .collection("content_update_date")
                .document("kanji")
                .get()
                .await()
                .getLong("date") ?: 0L
        }.getOrDefault(0L)
    }

    private fun getValidWords(words: List<Word>): List<Word> {
        val seen = mutableSetOf<String>()
        return words.filter { seen.add(it.kanji) }.sortedBy { it.id }
    }

    /** 청크 내 단어 map → [Word]. 표기가 비어 있으면 손상 데이터로 보고 버린다(null). */
    private fun Map<*, *>.toWord(id: Int): Word? {
        val kanji = this[KANJI_KEY]?.toString().orEmpty()
        if (kanji.isEmpty()) return null
        return Word(
            id = id,
            kanji = kanji,
            pronunciation = this[PRONUNCIATION_KEY]?.toString().orEmpty(),
            meaning = this[MEANING_KEY]?.toString().orEmpty().replace("\\n", "\n"),
            partOfSpeech = this[PART_OF_SPEECH_KEY]?.toString().orEmpty(),
        )
    }

    companion object {
        private const val WORDS_KEY = "words"
        private const val DATA_KEY = "data"
        private const val WORD_COUNT_KEY = "wordCount"
        private const val KANJI_KEY = "kanji"
        private const val PRONUNCIATION_KEY = "pronunciation"
        private const val MEANING_KEY = "meaning"
        private const val PART_OF_SPEECH_KEY = "partOfSpeech"
    }
}
