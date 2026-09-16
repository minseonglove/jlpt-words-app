package com.minseonglove.jlptwords.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// v1 은 출시본으로, words·words_statistics 와 세션/기록/스트릭 테이블만 갖는다.
// v2 는 그 위에 품사 컬럼·후리가나 전역 사전·예문(토큰 직렬화)·한자 사전·content_meta 를 더한 현재 스키마다.
// 개발 중 거쳐간 중간 스키마(예문 토큰을 별도 테이블로 두던 형태, content_meta 를 별도 버전으로 두던 형태)는
// 출시된 적이 없으므로, v1 사용자를 현재 스키마로 한 번에 올리는 단일 v1→v2 마이그레이션으로 통합했다.
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            // 1) words: 품사 컬럼
            connection.execSQL(
                "ALTER TABLE words ADD COLUMN part_of_speech TEXT NOT NULL DEFAULT ''",
            )

            // 1-1) words unique 인덱스 교체: (kanji) → (kanji, pronunciation)
            // 정정하지 않으면 동형이의어(같은 표기·다른 발음)가 IGNORE 충돌로 누락된다.
            connection.execSQL("DROP INDEX IF EXISTS index_words_kanji")
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_words_kanji_pronunciation ON words (kanji, pronunciation)",
            )

            // 1-2) words_statistics: 키를 kanji → word_id 로 재구조화한다.
            // v1 은 (kanji PK, FK→words.kanji), v2 는 (word_id PK, FK→words.id).
            // PK/FK 변경은 테이블 재생성으로만 가능하므로 새 테이블로 복사 후 교체한다.
            // v1 에서 words.kanji 는 unique 이므로 kanji→words.id 매핑은 1:1 로 모호하지 않다.
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS words_statistics_new (
                    word_id INTEGER NOT NULL,
                    appearance_count INTEGER NOT NULL DEFAULT 0,
                    correct_count INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(word_id),
                    FOREIGN KEY(word_id) REFERENCES words(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT OR IGNORE INTO words_statistics_new (word_id, appearance_count, correct_count)
                SELECT w.id, s.appearance_count, s.correct_count
                FROM words_statistics s JOIN words w ON w.kanji = s.kanji
                """.trimIndent(),
            )
            connection.execSQL("DROP TABLE words_statistics")
            connection.execSQL("ALTER TABLE words_statistics_new RENAME TO words_statistics")

            // 2) vocabulary_words (요미가나 전역 사전)
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS vocabulary_words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    surface TEXT NOT NULL,
                    reading TEXT NOT NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_vocabulary_words_surface ON vocabulary_words (surface)",
            )

            // 3) example_sentences (레벨별) — 토큰은 예문당 1행의 tokens 컬럼에 직렬화한다.
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS example_sentences (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    source_word_kanji TEXT NOT NULL,
                    level_code INTEGER NOT NULL,
                    sentence_jp TEXT NOT NULL,
                    sentence_ko TEXT NOT NULL,
                    example_order INTEGER NOT NULL,
                    tokens TEXT NOT NULL DEFAULT '',
                    furigana TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent(),
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS index_example_sentences_source_word_kanji ON example_sentences (source_word_kanji)",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS index_example_sentences_level_code ON example_sentences (level_code)",
            )

            // 4) kanji_info (한자 사전 — 한국한자/음독/훈독)
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS kanji_info (
                    kanji TEXT NOT NULL PRIMARY KEY,
                    korean_hanja TEXT,
                    on_yomi TEXT,
                    kun_yomi TEXT
                )
                """.trimIndent(),
            )

            // 5) content_meta (key-value 메타). 번들 스냅샷 DB 가 생성 시점을 담아
            // 첫 실행에서 동기화 기준 날짜를 시딩하기 위한 테이블. 기존 설치는 빈 테이블로 충분하다
            // (날짜는 이미 DataStore 에 있음).
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS content_meta (
                    `key` TEXT NOT NULL,
                    value INTEGER NOT NULL,
                    PRIMARY KEY(`key`)
                )
                """.trimIndent(),
            )
        }
    }
