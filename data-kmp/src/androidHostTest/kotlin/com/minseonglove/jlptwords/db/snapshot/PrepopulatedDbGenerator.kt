package com.minseonglove.jlptwords.db.snapshot

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import com.minseonglove.jlptwords.datasource.ContentMetaDataSource
import com.minseonglove.jlptwords.db.JLPTWordsDatabase
import com.minseonglove.jlptwords.db.entity.ContentMetaEntity
import com.minseonglove.jlptwords.db.entity.KanjiInfoEntity
import com.minseonglove.jlptwords.db.entity.WordEntity
import com.minseonglove.jlptwords.entity.JLPTLevel
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 번들 스냅샷 DB 생성기. 앱의 Room 정의·DAO(initWord, replaceLevelExamples 등)를 그대로 재사용해
 * TSV 원본으로부터 사전 적재 DB 를 만들고 android-app assets / ios-app 리소스 경로에 복사한다.
 *
 * 산출물 쓰기는 -PwriteSnapshot=true 를 명시한 실행에서만 수행한다.
 * 그 외(전체 테스트 실행 등)에는 생성·검증만 하고 파일을 건드리지 않는다.
 *
 * 실행: ./gradlew :data-kmp:testAndroidHostTest --tests "*PrepopulatedDbGenerator*" -PwriteSnapshot=true
 * 단어 데이터(TSV)를 수정해 upload.py 를 실행할 때마다 이 생성기도 다시 돌려 산출물을 커밋한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class PrepopulatedDbGenerator {
    @Test
    fun `TSV 원본으로 사전 적재 스냅샷 DB 를 생성한다`() =
        runTest {
            val repoRoot = findRepoRoot()
            val context = ApplicationProvider.getApplicationContext<Context>()
            val dbFile = File(context.cacheDir, "prepopulated_snapshot.db")
            dbFile.delete()

            val meaningsTsv = repoRoot.resolve("scripts/meaning_gen/all_word_meanings.tsv")
            val wordsByLevel =
                JLPTLevel.entries.associateWith { level ->
                    TsvCatalog.parseWords(repoRoot.resolve("scripts/final/${level.key}words.tsv"))
                }
            val furigana = TsvCatalog.parseFurigana(repoRoot.resolve("scripts/meaning_gen/example_furigana.tsv"))
            val examplesByLevel = TsvCatalog.parseExamples(meaningsTsv, furigana)
            val readings = TsvCatalog.parseReadings(meaningsTsv)
            val kanjis = TsvCatalog.parseKanji(repoRoot.resolve("docs/kanji_readings.tsv"))

            val database =
                Room
                    .databaseBuilder<JLPTWordsDatabase>(context, dbFile.absolutePath)
                    .setDriver(AndroidSQLiteDriver())
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .build()
            try {
                JLPTLevel.entries.forEach { level ->
                    val entities =
                        wordsByLevel.getValue(level).map { word ->
                            WordEntity(
                                id = 0,
                                kanji = word.kanji,
                                pronunciation = word.pronunciation,
                                meaning = word.meaning,
                                partOfSpeech = word.partOfSpeech,
                            )
                        }
                    database.wordInitializeDao().initWord(entities, level)
                }

                // 예문 표제어 필터 — 앱 동기화 경로와 동일하게 전 급수 단어 적재 후 1회 생성
                val knownKanji =
                    wordsByLevel.values
                        .flatten()
                        .map { it.kanji }
                        .toHashSet()
                JLPTLevel.entries.forEach { level ->
                    val dtos = examplesByLevel[level.key].orEmpty().filter { it.sourceWordKanji in knownKanji }
                    database.exampleDao().replaceLevelExamples(level.code, dtos)
                }

                database.exampleDao().replaceReadings(readings)
                database.kanjiInfoDao().upsertAll(
                    kanjis.map {
                        KanjiInfoEntity(
                            kanji = it.kanji,
                            koreanHanja = it.koreanHanja,
                            onYomi = it.onYomi,
                            kunYomi = it.kunYomi,
                        )
                    },
                )

                // 스냅샷 생성 시점 — 앱이 첫 실행에서 동기화 기준 날짜로 시딩한다
                database.contentMetaDao().set(
                    ContentMetaEntity(ContentMetaDataSource.KEY_SNAPSHOT_DATE, System.currentTimeMillis()),
                )

                val wordCount = database.wordDao().getWordCount()
                val vocabularyCount = database.exampleDao().getVocabularyCount()
                val kanjiCount = database.kanjiInfoDao().getCount()
                val exampleCounts =
                    JLPTLevel.entries.associate { it.key to database.exampleDao().getExampleCountByLevel(it.code) }
                println("snapshot words=$wordCount readings=$vocabularyCount kanji=$kanjiCount examples=$exampleCounts")

                assertTrue(wordCount > 8_000, "단어 수 이상: $wordCount")
                assertTrue(vocabularyCount > 10_000, "요미 수 이상: $vocabularyCount")
                assertTrue(kanjiCount > 2_000, "한자 수 이상: $kanjiCount")
                assertTrue(exampleCounts.values.all { it > 0 }, "예문 없는 급수 존재: $exampleCounts")

                val furiganaMissing =
                    JLPTLevel.entries.sumOf { level ->
                        database.exampleDao().getExampleCountWithoutFuriganaByLevel(level.code)
                    }
                assertTrue(furiganaMissing == 0, "후리가나 없는 예문 존재: $furiganaMissing")
            } finally {
                database.close()
            }

            if (System.getProperty("jlptwords.writeSnapshot") != "true") {
                println("snapshot 검증만 수행 (산출물 쓰기는 -PwriteSnapshot=true 로 실행)")
                return@runTest
            }

            // 파일 크기 최소화 후 산출물 복사
            val connection = AndroidSQLiteDriver().open(dbFile.absolutePath)
            try {
                connection.execSQL("VACUUM")
            } finally {
                connection.close()
            }
            val outputs =
                listOf(
                    repoRoot.resolve("android-app/src/main/assets/database/jlpt_words_prepopulated.db"),
                    repoRoot.resolve("ios-app/ios-app/jlpt_words_prepopulated.db"),
                )
            outputs.forEach { output ->
                output.parentFile.mkdirs()
                dbFile.copyTo(output, overwrite = true)
            }
            println("snapshot size=${dbFile.length()} bytes → ${outputs.joinToString { it.relativeTo(repoRoot).path }}")
        }

    private fun findRepoRoot(): File {
        var dir = File("").absoluteFile
        while (!dir.resolve("settings.gradle.kts").exists()) {
            dir = dir.parentFile ?: error("저장소 루트(settings.gradle.kts)를 찾지 못했습니다")
        }
        return dir
    }
}
