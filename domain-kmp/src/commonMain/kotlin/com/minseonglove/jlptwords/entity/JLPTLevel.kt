package com.minseonglove.jlptwords.entity

import kotlinx.serialization.Serializable

@Serializable
enum class JLPTLevel(
    val code: Int,
    val key: String,
) {
    N5(5, "n5"),
    N4(4, "n4"),
    N3(3, "n3"),
    N2(2, "n2"),
    N1(1, "n1"),
    ;

    companion object {
        fun fromCode(code: Int): JLPTLevel {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown JLPT level code: $code")
        }
    }
}
