package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.ClipBoardDataSource

class ClipBoardRepositoryImpl(
    private val clipBoardDataSource: ClipBoardDataSource,
) : ClipBoardRepository {
    override suspend fun copyToClipBoard(kanji: String) {
        clipBoardDataSource.copyToClipBoard(kanji)
    }
}
