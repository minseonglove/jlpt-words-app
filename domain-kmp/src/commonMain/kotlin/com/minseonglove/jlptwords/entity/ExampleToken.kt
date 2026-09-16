package com.minseonglove.jlptwords.entity

data class ExampleToken(
    val surface: String,
    val reading: String,
    val meaning: String,
    /** 학습 단어 테이블(words)에 등재된 JLPT 기출 단어이면 true (상세 화면에서 파란색 표시). */
    val isJlptWord: Boolean = false,
)
