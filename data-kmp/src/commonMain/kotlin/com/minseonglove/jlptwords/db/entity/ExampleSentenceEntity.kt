package com.minseonglove.jlptwords.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// words 와는 (source_word_kanji, level_code) 로 느슨하게 연결된다.
// words.kanji 가 더 이상 단독 unique 가 아니므로(동형이의어 공존) FK 로 묶지 않고,
// 레벨 예문 교체(replaceLevelExamples)와 적재 전 표제어 필터로 정합성을 유지한다.
@Entity(
    tableName = "example_sentences",
    indices = [
        Index(value = ["source_word_kanji"]),
        Index(value = ["level_code"]),
    ],
)
data class ExampleSentenceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "source_word_kanji")
    val sourceWordKanji: String,
    @ColumnInfo(name = "level_code")
    val levelCode: Int,
    @ColumnInfo(name = "sentence_jp")
    val sentenceJp: String,
    @ColumnInfo(name = "sentence_ko")
    val sentenceKo: String,
    @ColumnInfo(name = "example_order")
    val exampleOrder: Int,
    // 예문 토큰(표면형+문맥뜻)을 직렬화해 한 컬럼에 보관한다(별도 word_in_example 테이블 제거).
    // 직렬화/역직렬화는 ExampleTokenCodec, reading 은 표시 시 vocabulary_words 에서 조회.
    @ColumnInfo(name = "tokens", defaultValue = "")
    val tokens: String,
    // 예문 루비 세그먼트 직렬화 (FuriganaCodec). 비어 있으면 후리가나 미제공 데이터.
    @ColumnInfo(name = "furigana", defaultValue = "")
    val furigana: String,
)
