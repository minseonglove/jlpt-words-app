package com.minseonglove.jlptwords.navigation.type

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import com.minseonglove.jlptwords.navigation.dto.StudyDTO
import kotlinx.serialization.json.Json

class StudyNavType :
    NavType<StudyDTO>(
        isNullableAllowed = false,
    ) {
    // 구버전 라우트 JSON(과거 studyStatusDTO 필드 포함)도 무시하고 역직렬화하도록 허용
    private val json = Json { ignoreUnknownKeys = true }

    override fun put(
        bundle: SavedState,
        key: String,
        value: StudyDTO,
    ) {
        bundle.write {
            putString(key, json.encodeToString(value))
        }
    }

    override fun get(
        bundle: SavedState,
        key: String,
    ): StudyDTO? {
        val jsonString = bundle.read { getString(key) }
        return parseValue(jsonString)
    }

    override fun parseValue(
        value: String,
    ): StudyDTO {
        return json.decodeFromString<StudyDTO>(value)
    }

    override fun serializeAsValue(value: StudyDTO): String {
        return json.encodeToString(value)
    }
}
