package com.minseonglove.jlptwords.navigation.dto

import kotlinx.serialization.Serializable

@Serializable
data class StudyDTO(
    val sessionId: Int,
    val sessionIndex: Int,
)
