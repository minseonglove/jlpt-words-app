package com.minseonglove.jlptwords.entity

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class Example(
    val japanese: String,
    val korean: String,
    val tokens: ImmutableList<ExampleToken> = persistentListOf(),
    val furigana: ImmutableList<RubySegment> = persistentListOf(),
)
