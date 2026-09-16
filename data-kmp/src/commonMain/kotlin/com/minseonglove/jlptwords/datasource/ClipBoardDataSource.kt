package com.minseonglove.jlptwords.datasource

expect class ClipBoardDataSource {
    suspend fun copyToClipBoard(kanji: String)
}
