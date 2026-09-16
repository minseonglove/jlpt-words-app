package com.minseonglove.jlptwords.entity

/** 단어 검색 결과 정렬 기준. */
enum class WordSortType {
    /** 단어 번호(id) 순. 기본값. */
    WORD_NUMBER,

    /** 발음(かな) 사전 순. */
    DICTIONARY,

    /** 정답률 순. 학습 이력이 없는 단어는 정렬 방향과 무관하게 항상 뒤에 둔다. */
    ACCURACY,
}
