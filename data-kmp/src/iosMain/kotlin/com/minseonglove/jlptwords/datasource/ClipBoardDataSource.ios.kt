package com.minseonglove.jlptwords.datasource

import platform.UIKit.UIPasteboard

actual class ClipBoardDataSource {
    actual suspend fun copyToClipBoard(kanji: String) {
        UIPasteboard.generalPasteboard.string = kanji
    }
}
