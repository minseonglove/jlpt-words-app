package com.minseonglove.jlptwords.repository

import com.minseonglove.jlptwords.datasource.WordDataSource
import com.minseonglove.jlptwords.entity.JLPTLevel
import com.minseonglove.jlptwords.entity.SearchedWord
import com.minseonglove.jlptwords.entity.Word

class WordRepositoryImpl(
    private val wordDataSource: WordDataSource,
) : WordRepository {
    override suspend fun increaseAppearanceCount(
        wordId: Int,
    ) {
        wordDataSource.increaseAppearanceCount(wordId)
    }

    override suspend fun increaseCorrectAndAppearanceCount(
        wordId: Int,
    ) {
        wordDataSource.increaseCorrectAndAppearanceCount(wordId)
    }

    override suspend fun getWord(
        kanji: String,
        pronunciation: String,
    ): Word? = wordDataSource.getWord(kanji, pronunciation)

    override suspend fun getJlptLevel(wordId: Int): JLPTLevel = wordDataSource.getJlptLevel(wordId)

    override suspend fun searchWords(query: String): List<SearchedWord> = wordDataSource.searchWords(query)
}
