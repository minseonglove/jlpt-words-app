package com.minseonglove.jlptwords.usecase

import com.minseonglove.jlptwords.repository.ClipBoardRepository

class CopyToClipboard(
    private val clipBoardRepository: ClipBoardRepository,
) {
    suspend operator fun invoke(kanji: String) {
        clipBoardRepository.copyToClipBoard(kanji)
    }
}
