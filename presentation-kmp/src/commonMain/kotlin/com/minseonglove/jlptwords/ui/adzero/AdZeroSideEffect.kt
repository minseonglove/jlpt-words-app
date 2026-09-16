package com.minseonglove.jlptwords.ui.adzero

import org.jetbrains.compose.resources.StringResource

sealed interface AdZeroSideEffect {
    data class ShowMessage(
        val messageRes: StringResource,
    ) : AdZeroSideEffect

    data object RequestDismiss : AdZeroSideEffect
}
