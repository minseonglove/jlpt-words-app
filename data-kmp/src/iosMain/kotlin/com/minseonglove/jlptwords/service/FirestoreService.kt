package com.minseonglove.jlptwords.service

import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.Word
import com.minseonglove.jlptwords.service.dto.AllContentUpdateDates
import com.minseonglove.jlptwords.service.dto.ContentUpdateDate
import com.minseonglove.jlptwords.service.dto.KanjiInfoDto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSNumber
import platform.posix.memcpy
import swiftPMImport.jlpt.words.data.kmp.FIRFirestore
import swiftPMImport.jlpt.words.data.kmp.FIRQueryDocumentSnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual class FirestoreService(
    private val firestore: FIRFirestore,
) {
    actual suspend fun getWords(level: JLPTLevel): List<Word> =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("words_${level.key}").getDocumentsWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> {
                        // 문서 1개 = 단어 청크({words: [...]}). 문서 id(청크 index)를 숫자 순으로
                        // 이어 붙여 행 순서를 복원한다 (문자열 정렬은 "10" < "2" 가 되므로 금지).
                        val words = mutableListOf<Word>()
                        // id 가 숫자가 아닌 문서는 청크가 아니므로 건너뛴다. toInt 로 두면 예외가
                        // Firestore 콜백(ObjC 프레임) 위에서 던져져 코루틴으로 전달되지 않고 앱이 죽는다.
                        val docs =
                            snapshot.documents
                                .mapNotNull { it as? FIRQueryDocumentSnapshot }
                                .mapNotNull { doc -> doc.documentID.toIntOrNull()?.let { it to doc } }
                                .sortedBy { it.first }
                        for ((_, doc) in docs) {
                            val chunk = doc.valueForField("words") as? List<*> ?: continue
                            for (item in chunk) {
                                val word = (item as? Map<*, *>)?.toWord(id = words.size) ?: continue
                                words.add(word)
                            }
                        }
                        cont.resume(getValidWords(words))
                    }
                    else -> cont.resume(emptyList())
                }
            }
        }

    actual suspend fun getWordCount(level: JLPTLevel): Int =
        suspendCancellableCoroutine { cont ->
            // 단어가 청크 문서로 묶여 있어 문서 수 ≠ 단어 수. 업로드 시 기록한 메타데이터를 읽는다.
            firestore
                .collectionWithPath("content_update_date")
                .documentWithPath(level.key)
                .getDocumentWithCompletion { snapshot, error ->
                    when {
                        error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                        snapshot != null ->
                            cont.resume((snapshot.valueForField("wordCount") as? NSNumber)?.longValue?.toInt() ?: 0)
                        else -> cont.resume(0)
                    }
                }
        }

    actual suspend fun getExampleChunkBlobs(level: JLPTLevel): List<ByteArray> =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("examples_${level.key}").getDocumentsWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> {
                        val blobs =
                            snapshot.documents
                                .mapNotNull { it as? FIRQueryDocumentSnapshot }
                                .mapNotNull { (it.valueForField("data") as? NSData)?.toByteArray() }
                        cont.resume(blobs)
                    }
                    else -> cont.resume(emptyList())
                }
            }
        }

    actual suspend fun getReadingsBlob(): ByteArray? =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("readings").documentWithPath("all").getDocumentWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> cont.resume((snapshot.valueForField("data") as? NSData)?.toByteArray())
                    else -> cont.resume(null)
                }
            }
        }

    actual suspend fun getContentUpdateDate(level: JLPTLevel): ContentUpdateDate =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("content_update_date").documentWithPath(level.key).getDocumentWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> {
                        val wd = (snapshot.valueForField("wordsDate") as? NSNumber)?.longValue ?: 0L
                        val ed = (snapshot.valueForField("examplesDate") as? NSNumber)?.longValue ?: 0L
                        cont.resume(ContentUpdateDate(wd, ed))
                    }
                    else -> cont.resume(ContentUpdateDate(0L, 0L))
                }
            }
        }

    actual suspend fun getReadingsUpdateDate(): Long =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("content_update_date").documentWithPath("readings").getDocumentWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> cont.resume((snapshot.valueForField("date") as? NSNumber)?.longValue ?: 0L)
                    else -> cont.resume(0L)
                }
            }
        }

    actual suspend fun getAllContentUpdateDates(): AllContentUpdateDates =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("content_update_date").getDocumentsWithCompletion { snapshot, error ->
                when {
                    // 실패 시 예외 대신 EMPTY — 오프라인이어도 로컬 데이터 검증 경로로 진행시키는 계약 (expect 참고)
                    error != null -> cont.resume(AllContentUpdateDates.EMPTY)
                    snapshot != null -> {
                        val docs =
                            snapshot.documents
                                .mapNotNull { it as? FIRQueryDocumentSnapshot }
                                .associateBy { it.documentID }
                        cont.resume(
                            AllContentUpdateDates(
                                byLevel =
                                    JLPTLevel.entries.associateWith { level ->
                                        val doc = docs[level.key]
                                        ContentUpdateDate(
                                            wordsDate = (doc?.valueForField("wordsDate") as? NSNumber)?.longValue ?: 0L,
                                            examplesDate = (doc?.valueForField("examplesDate") as? NSNumber)?.longValue ?: 0L,
                                        )
                                    },
                                readingsDate = (docs["readings"]?.valueForField("date") as? NSNumber)?.longValue ?: 0L,
                                kanjiDate = (docs["kanji"]?.valueForField("date") as? NSNumber)?.longValue ?: 0L,
                            ),
                        )
                    }
                    else -> cont.resume(AllContentUpdateDates.EMPTY)
                }
            }
        }

    actual suspend fun getKanjiInfo(): List<KanjiInfoDto> =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("kanji_info").documentWithPath("all").getDocumentWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> {
                        val data = snapshot.data() as? Map<*, *>
                        val result =
                            data?.mapNotNull { (kanjiAny, value) ->
                                val kanji = kanjiAny?.toString() ?: return@mapNotNull null
                                if (kanji.isEmpty()) return@mapNotNull null
                                val entry = value as? Map<*, *> ?: return@mapNotNull null
                                KanjiInfoDto(
                                    kanji = kanji,
                                    koreanHanja = entry["korean_hanja"] as? String,
                                    onYomi = entry.stringList("on_yomi"),
                                    kunYomi = entry.stringList("kun_yomi"),
                                )
                            } ?: emptyList()
                        cont.resume(result)
                    }
                    else -> cont.resume(emptyList())
                }
            }
        }

    actual suspend fun getKanjiUpdateDate(): Long =
        suspendCancellableCoroutine { cont ->
            firestore.collectionWithPath("content_update_date").documentWithPath("kanji").getDocumentWithCompletion { snapshot, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    snapshot != null -> cont.resume((snapshot.valueForField("date") as? NSNumber)?.longValue ?: 0L)
                    else -> cont.resume(0L)
                }
            }
        }

    private fun Map<*, *>.stringList(field: String): List<String> {
        val raw = this[field] as? List<*> ?: return emptyList()
        return raw.mapNotNull { it?.toString() }
    }

    private fun NSData.toByteArray(): ByteArray {
        val result = ByteArray(length.toInt())
        if (result.isNotEmpty()) {
            result.usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
        return result
    }

    private fun getValidWords(words: List<Word>): List<Word> {
        val seen = mutableSetOf<String>()
        return words.filter { seen.add(it.kanji) }.sortedBy { it.id }
    }

    /** 청크 내 단어 map → [Word]. 표기가 비어 있으면 손상 데이터로 보고 버린다(null). */
    private fun Map<*, *>.toWord(id: Int): Word? {
        val kanji = this["kanji"]?.toString().orEmpty()
        if (kanji.isEmpty()) return null
        return Word(
            id = id,
            kanji = kanji,
            pronunciation = this["pronunciation"]?.toString().orEmpty(),
            meaning = this["meaning"]?.toString().orEmpty().replace("\\n", "\n"),
            partOfSpeech = this["partOfSpeech"]?.toString().orEmpty(),
        )
    }
}
