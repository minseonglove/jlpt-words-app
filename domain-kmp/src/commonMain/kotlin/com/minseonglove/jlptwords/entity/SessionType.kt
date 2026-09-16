package com.minseonglove.jlptwords.entity

enum class SessionType(
    val code: Int,
) {
    NORMAL(0),
    LOW_ACCURACY(1),
    RANDOM(2),
    ;

    companion object {
        fun fromCode(code: Int): SessionType {
            return SessionType.entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown SessionType code: $code")
        }
    }
}
