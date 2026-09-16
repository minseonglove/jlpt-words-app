package com.minseonglove.jlptwords.service

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.proto.ChunkWordExamples
import com.minseonglove.jlptwords.proto.ExampleChunk
import com.minseonglove.jlptwords.proto.ReadingsBlob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

class FirestoreServiceRealTest {
    private lateinit var service: FirestoreService

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        service = FirestoreService(Firebase.firestore("words"))
    }

    @Test
    fun n5_단어본체에_품사가_포함되고_중복이_없다() =
        runTest {
            val words = service.getWords(JLPTLevel.N5)
            assertTrue("N5 단어 수가 비정상: ${words.size}", words.size > 500)
            assertTrue("kanji 누락", words.all { it.kanji.isNotBlank() })
            // 인사말·감동사 등 품사 없는 단어가 소수 존재할 수 있으므로 90% 이상 품사 보유를 검증
            val withPos = words.count { it.partOfSpeech.isNotBlank() }
            assertTrue("품사 보유율 비정상: $withPos/${words.size}", withPos * 100 / words.size >= 90)
            assertTrue("kanji 중복 존재", words.map { it.kanji }.toSet().size == words.size)
        }

    @Test
    fun n5_예문은_단어당_1에서2개이며_토큰을_가진다() =
        runTest {
            val words = loadExampleWords()
            assertTrue("예문 없음", words.isNotEmpty())
            assertTrue("예문 개수 비정상", words.all { it.examples.size in 1..2 })
            val examples = words.flatMap { it.examples }
            assertTrue("예문 텍스트 누락", examples.all { it.japanese.isNotBlank() && it.korean.isNotBlank() })
            assertTrue("토큰 없음", examples.any { it.tokens.isNotEmpty() })
        }

    @Test
    fun 요미사전과_날짜가_적재되어_있다() =
        runTest {
            val readings = loadReadings()
            assertTrue("요미 사전 비정상: ${readings.size}", readings.size > 100)

            val date = service.getContentUpdateDate(JLPTLevel.N5)
            assertTrue("content date 누락", date.wordsDate > 0 && date.examplesDate > 0)
            assertTrue("readings date 누락", service.getReadingsUpdateDate() > 0)
        }

    @Test
    fun 예문_토큰_surface는_요미사전에_존재한다() =
        runTest {
            val readings = loadReadings()
            val surfaces =
                loadExampleWords()
                    .flatMap { it.examples }
                    .flatMap { it.tokens }
                    .map { it.surface }
                    .toSet()
            val missing = surfaces.filter { it !in readings }
            assertTrue("요미 미존재 surface: ${missing.take(10)}", missing.isEmpty())
        }

    private suspend fun loadExampleWords(): List<ChunkWordExamples> =
        service.getExampleChunkBlobs(JLPTLevel.N5).flatMap { blob ->
            ExampleChunk.ADAPTER.decode(inflateRaw(blob)).words
        }

    private suspend fun loadReadings(): Map<String, String> {
        val blob = service.getReadingsBlob()
        assertNotNull("readings/all 문서에 data 필드가 없음", blob)
        return ReadingsBlob.ADAPTER
            .decode(inflateRaw(requireNotNull(blob)))
            .entries
            .associate { it.surface to it.reading }
    }

    // data-kmp 의 inflateRaw 유틸은 internal 이라 테스트에서 같은 로직(raw deflate 해제)을 사용한다.
    private fun inflateRaw(data: ByteArray): ByteArray {
        if (data.isEmpty()) return ByteArray(0)
        val inflater = Inflater(true)
        return try {
            inflater.setInput(data)
            val out = ByteArrayOutputStream(data.size * 4)
            val buffer = ByteArray(8192)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    error("손상된 deflate 스트림")
                }
                out.write(buffer, 0, count)
            }
            out.toByteArray()
        } finally {
            inflater.end()
        }
    }
}
