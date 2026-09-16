package com.minseonglove.jlptwords.navigation.type

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import com.minseonglove.jlptwords.navigation.dto.LevelSelectionDTO
import kotlinx.serialization.json.Json

class LevelSelectionNavType :
    NavType<LevelSelectionDTO>(
        isNullableAllowed = false,
    ) {
    override fun put(
        bundle: SavedState,
        key: String,
        value: LevelSelectionDTO,
    ) {
        bundle.write {
            putString(key, Json.Default.encodeToString(value))
        }
    }

    override fun get(
        bundle: SavedState,
        key: String,
    ): LevelSelectionDTO? {
        val jsonString = bundle.read { getString(key) }
        return parseValue(jsonString)
    }

    override fun parseValue(
        value: String,
    ): LevelSelectionDTO {
        return Json.Default.decodeFromString<LevelSelectionDTO>(value)
    }

    override fun serializeAsValue(value: LevelSelectionDTO): String {
        return Json.Default.encodeToString(value)
    }
}
