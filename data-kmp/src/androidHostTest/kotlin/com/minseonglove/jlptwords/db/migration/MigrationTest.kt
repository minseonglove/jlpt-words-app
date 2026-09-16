package com.minseonglove.jlptwords.db.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * v1(출시본) → v2(현재 스키마) 마이그레이션 검증.
 *
 * 새 AGP KMP 플러그인에서는 Room MigrationTestHelper 의 schema-assets 연결이 동작하지 않아,
 * Robolectric 이 제공하는 SQLite(AndroidSQLiteDriver)로 v1 스키마를 직접 만들고
 * [MIGRATION_1_2] 를 호출한 뒤 실제 스키마(sqlite_master)와 데이터로 결과를 검증한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class MigrationTest {
    /** v1 스키마: words, words_statistics, word_level_mapping (예문·사전 테이블은 v2 에서 추가). */
    private fun createV1Database(): SQLiteConnection {
        val connection = AndroidSQLiteDriver().open(":memory:")
        connection.execSQL(
            "CREATE TABLE words (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "kanji TEXT NOT NULL, pronunciation TEXT NOT NULL, meaning TEXT NOT NULL)",
        )
        connection.execSQL("CREATE UNIQUE INDEX index_words_kanji ON words (kanji)")
        connection.execSQL(
            "CREATE TABLE words_statistics (kanji TEXT NOT NULL, " +
                "appearance_count INTEGER NOT NULL DEFAULT 0, correct_count INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(kanji), FOREIGN KEY(kanji) REFERENCES words(kanji) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        connection.execSQL("CREATE INDEX index_words_statistics_kanji ON words_statistics (kanji)")
        connection.execSQL(
            "CREATE TABLE word_level_mapping (word_id INTEGER NOT NULL, level_code INTEGER NOT NULL, " +
                "PRIMARY KEY(word_id, level_code), " +
                "FOREIGN KEY(word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        return connection
    }

    private fun SQLiteConnection.count(sql: String): Long {
        val statement = prepare(sql)
        try {
            assertTrue(statement.step())
            return statement.getLong(0)
        } finally {
            statement.close()
        }
    }

    @Test
    fun `MIGRATION_1_2 는 통계를 word_id 기준으로 옮기며 값을 보존한다`() =
        runTest {
            val connection = createV1Database()
            connection.execSQL("INSERT INTO words (id, kanji, pronunciation, meaning) VALUES (1, '漢', 'かん', '한나라 한')")
            connection.execSQL("INSERT INTO words_statistics (kanji, appearance_count, correct_count) VALUES ('漢', 10, 5)")

            MIGRATION_1_2.migrate(connection)

            val statement = connection.prepare("SELECT word_id, appearance_count, correct_count FROM words_statistics")
            try {
                assertTrue(statement.step(), "통계 행이 존재해야 한다")
                assertEquals(1L, statement.getLong(0), "kanji 가 word_id(=words.id) 로 매핑되어야 한다")
                assertEquals(10L, statement.getLong(1))
                assertEquals(5L, statement.getLong(2))
                assertFalse(statement.step(), "통계 행은 1개여야 한다")
            } finally {
                statement.close()
            }
            connection.close()
        }

    @Test
    fun `MIGRATION_1_2 후 words 인덱스가 발음 포함 복합 유니크 인덱스로 교체되고 품사 컬럼이 추가된다`() =
        runTest {
            val connection = createV1Database()

            MIGRATION_1_2.migrate(connection)

            assertEquals(
                0L,
                connection.count("SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND name='index_words_kanji'"),
                "옛 단일 인덱스는 제거되어야 한다",
            )
            assertEquals(
                1L,
                connection.count(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND name='index_words_kanji_pronunciation'",
                ),
                "(kanji, pronunciation) 복합 인덱스가 있어야 한다",
            )
            assertEquals(
                1L,
                connection.count("SELECT COUNT(*) FROM pragma_table_info('words') WHERE name='part_of_speech'"),
                "words 에 품사 컬럼이 추가되어야 한다",
            )
            connection.close()
        }

    @Test
    fun `MIGRATION_1_2 는 예문 테이블을 tokens 컬럼 포함해 만들고 중간 스키마의 word_in_example 은 만들지 않는다`() =
        runTest {
            val connection = createV1Database()

            MIGRATION_1_2.migrate(connection)

            assertEquals(
                1L,
                connection.count("SELECT COUNT(*) FROM pragma_table_info('example_sentences') WHERE name='tokens'"),
                "example_sentences 에 tokens 컬럼이 있어야 한다",
            )
            assertEquals(
                0L,
                connection.count("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='word_in_example'"),
                "출시된 적 없는 중간 스키마의 word_in_example 은 생성되지 않아야 한다",
            )
            assertEquals(
                1L,
                connection.count("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='vocabulary_words'"),
            )
            assertEquals(
                1L,
                connection.count("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='kanji_info'"),
            )
            connection.close()
        }

    /**
     * 마이그레이션은 품사를 빈 문자열로만 만들고, 값 채우기는 동기화의 단어 갱신에 맡긴다.
     * 그 갱신을 날짜 비교로만 판정하면 원격 날짜가 그대로일 때 품사가 영영 비게 되므로,
     * WordDao.getWordCountMissingPartOfSpeech 가 쓰는 조건이 마이그레이션 직후 감지되는지 확인한다.
     */
    @Test
    fun `MIGRATION_1_2 직후에는 품사 미채움이 급수별로 감지된다`() =
        runTest {
            val connection = createV1Database()
            connection.execSQL("INSERT INTO words (id, kanji, pronunciation, meaning) VALUES (1, '上', 'うえ', '위')")
            connection.execSQL("INSERT INTO word_level_mapping (word_id, level_code) VALUES (1, 5)")

            MIGRATION_1_2.migrate(connection)

            val missingSql =
                "SELECT COUNT(*) FROM words words " +
                    "INNER JOIN word_level_mapping wlm ON words.id = wlm.word_id " +
                    "WHERE wlm.level_code = %d AND words.part_of_speech = ''"
            assertEquals(
                1L,
                connection.count(missingSql.format(5)),
                "마이그레이션 직후에는 해당 급수의 품사 미채움이 감지되어야 한다",
            )
            assertEquals(
                0L,
                connection.count(missingSql.format(4)),
                "단어가 없는 급수는 미채움으로 잡히지 않아야 한다",
            )

            connection.execSQL("UPDATE words SET part_of_speech = '명사' WHERE id = 1")
            assertEquals(
                0L,
                connection.count(missingSql.format(5)),
                "품사가 채워지면 가드가 해제되어 재동기화가 반복되지 않아야 한다",
            )
            connection.close()
        }

    @Test
    fun `MIGRATION_1_2 는 content_meta 테이블을 추가한다`() =
        runTest {
            val connection = createV1Database()

            MIGRATION_1_2.migrate(connection)

            assertEquals(
                1L,
                connection.count("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='content_meta'"),
                "content_meta 테이블이 생성되어야 한다",
            )
            connection.execSQL("INSERT INTO content_meta (`key`, value) VALUES ('snapshot_date', 123)")
            assertEquals(1L, connection.count("SELECT COUNT(*) FROM content_meta"))
            connection.close()
        }
}
