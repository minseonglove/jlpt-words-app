package com.minseonglove.jlptwords.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.minseonglove.jlptwords.db.migration.MIGRATION_1_2
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class Migration1to2Test {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            instrumentation = instrumentation,
            file = File(instrumentation.targetContext.cacheDir, "migration_test.db"),
            driver = BundledSQLiteDriver(),
            databaseClass = JLPTWordsDatabase::class,
        )

    @Test
    fun v1_학습기록은_v2_마이그레이션_후에도_보존된다() =
        runTest {
            helper.createDatabase(version = 1).use { connection ->
                connection.execSQL(
                    "INSERT INTO words (id, kanji, pronunciation, meaning) VALUES (1, '吸う', 'すう', '마시다')",
                )
                connection.execSQL(
                    "INSERT INTO words_statistics (kanji, appearance_count, correct_count) VALUES ('吸う', 7, 5)",
                )
            }

            helper.runMigrationsAndValidate(version = 2, migrations = listOf(MIGRATION_1_2)).use { connection ->
                // v2 에서 words_statistics 는 kanji 가 아니라 word_id(=words.id) 로 키가 재구조화된다.
                // 학습기록(appearance/correct)은 word_id 로 이관되어 보존되어야 한다.
                connection.prepare("SELECT appearance_count, correct_count FROM words_statistics WHERE word_id = 1").use { stmt: SQLiteStatement ->
                    assertEquals(true, stmt.step())
                    assertEquals(7, stmt.getLong(0).toInt())
                    assertEquals(5, stmt.getLong(1).toInt())
                }
                connection.prepare("SELECT part_of_speech FROM words WHERE id = 1").use { stmt: SQLiteStatement ->
                    assertEquals(true, stmt.step())
                    assertEquals("", stmt.getText(0))
                }
                connection.prepare("SELECT COUNT(*) FROM vocabulary_words").use { stmt: SQLiteStatement ->
                    assertEquals(true, stmt.step())
                    assertEquals(0, stmt.getLong(0).toInt())
                }
                connection.prepare("SELECT COUNT(*) FROM example_sentences").use { stmt: SQLiteStatement ->
                    assertEquals(true, stmt.step())
                    assertEquals(0, stmt.getLong(0).toInt())
                }
                // v2 의 example_sentences 는 tokens·furigana 컬럼(직렬화 TEXT, 기본 '')을 가진다.
                connection.execSQL(
                    "INSERT INTO example_sentences (source_word_kanji, level_code, sentence_jp, sentence_ko, example_order) " +
                        "VALUES ('吸う', 5, '空気を吸う。', '공기를 마시다.', 0)",
                )
                connection.prepare("SELECT tokens, furigana FROM example_sentences WHERE source_word_kanji = '吸う'").use { stmt: SQLiteStatement ->
                    assertEquals(true, stmt.step())
                    assertEquals("", stmt.getText(0))
                    assertEquals("", stmt.getText(1))
                }
            }
        }
}
