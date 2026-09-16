package com.minseonglove.jlptwords.service.dto

data class KanjiInfoDto(
    val kanji: String,
    val koreanHanja: String?,
    val onYomi: List<String>,
    val kunYomi: List<String>,
)
