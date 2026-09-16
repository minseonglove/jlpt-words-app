package com.minseonglove.jlptwords.datasource

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual class ClipBoardDataSource(
    private val context: Context,
) {
    actual suspend fun copyToClipBoard(kanji: String) {
        val clipboardManager =
            context.getSystemService(
                Context.CLIPBOARD_SERVICE,
            ) as? ClipboardManager
                ?: return

        val clipData =
            ClipData.newPlainText(
                "mollu",
                kanji,
            )
        clipboardManager.setPrimaryClip(clipData)
    }
}
