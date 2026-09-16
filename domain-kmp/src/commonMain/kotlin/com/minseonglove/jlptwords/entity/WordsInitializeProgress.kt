package com.minseonglove.jlptwords.entity

enum class WordsInitializeProgress {
    IDLE,
    ERROR,
    INITIALIZING,
    UPDATING,
    COMPLETE,
    ALREADY_UP_TO_DATE,
    ;

    /** 초기화 흐름이 종결됐는지(성공·최신·실패). `first { it.isTerminal }` 대기 조건에 사용한다. */
    val isTerminal: Boolean
        get() = this == COMPLETE || this == ALREADY_UP_TO_DATE || this == ERROR
}
