package com.minseonglove.jlptwords.entity

/** 한자 1글자의 사전 정보 (단어 상세 화면의 한자 분해 박스용). */
data class KanjiInfo(
    val kanji: String,
    /** 한국 한자 훈음 (예: "간 간"). 사전 미등재 시 null. */
    val koreanHanja: String?,
    /** 음독 (예: ["かん"]). */
    val onYomi: List<String>,
    /** 훈독 (예: ["きも"]). */
    val kunYomi: List<String>,
)
