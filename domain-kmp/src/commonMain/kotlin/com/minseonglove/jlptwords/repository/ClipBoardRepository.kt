package com.minseonglove.jlptwords.repository

interface ClipBoardRepository {
    suspend fun copyToClipBoard(
        kanji: String,
    )
}
