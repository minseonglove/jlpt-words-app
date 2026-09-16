package com.minseonglove.jlptwords.navigation.dto

import com.minseonglove.jlptwords.entity.JLPTLevel
import kotlinx.serialization.Serializable

@Serializable
data class LevelSelectionDTO(
    val currentLevel: JLPTLevel,
)
