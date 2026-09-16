package com.minseonglove.jlptwords.ui.selection.level

import com.minseonglove.jlptwords.entity.JLPTLevel
import jlptwords.presentation_kmp.generated.resources.Res
import jlptwords.presentation_kmp.generated.resources.ic_n1_mountain
import jlptwords.presentation_kmp.generated.resources.ic_n1_raccoon
import jlptwords.presentation_kmp.generated.resources.ic_n2_mountain
import jlptwords.presentation_kmp.generated.resources.ic_n2_raccoon
import jlptwords.presentation_kmp.generated.resources.ic_n3_mountain
import jlptwords.presentation_kmp.generated.resources.ic_n3_raccoon
import jlptwords.presentation_kmp.generated.resources.ic_n4_mountain
import jlptwords.presentation_kmp.generated.resources.ic_n4_raccoon
import jlptwords.presentation_kmp.generated.resources.ic_n5_mountain
import jlptwords.presentation_kmp.generated.resources.ic_n5_raccoon
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n1_subtitle
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n2_subtitle
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n3_subtitle
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n4_subtitle
import jlptwords.presentation_kmp.generated.resources.jlpt_level_n5_subtitle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

// 급수선택 화면의 레벨별 에셋/문자열 매핑. 타입 세이프하게 when 으로 연결한다.

internal fun JLPTLevel.raccoonResource(): DrawableResource =
    when (this) {
        JLPTLevel.N1 -> Res.drawable.ic_n1_raccoon
        JLPTLevel.N2 -> Res.drawable.ic_n2_raccoon
        JLPTLevel.N3 -> Res.drawable.ic_n3_raccoon
        JLPTLevel.N4 -> Res.drawable.ic_n4_raccoon
        JLPTLevel.N5 -> Res.drawable.ic_n5_raccoon
    }

internal fun JLPTLevel.mountainResource(): DrawableResource =
    when (this) {
        JLPTLevel.N1 -> Res.drawable.ic_n1_mountain
        JLPTLevel.N2 -> Res.drawable.ic_n2_mountain
        JLPTLevel.N3 -> Res.drawable.ic_n3_mountain
        JLPTLevel.N4 -> Res.drawable.ic_n4_mountain
        JLPTLevel.N5 -> Res.drawable.ic_n5_mountain
    }

internal fun JLPTLevel.subtitleResource(): StringResource =
    when (this) {
        JLPTLevel.N1 -> Res.string.jlpt_level_n1_subtitle
        JLPTLevel.N2 -> Res.string.jlpt_level_n2_subtitle
        JLPTLevel.N3 -> Res.string.jlpt_level_n3_subtitle
        JLPTLevel.N4 -> Res.string.jlpt_level_n4_subtitle
        JLPTLevel.N5 -> Res.string.jlpt_level_n5_subtitle
    }
