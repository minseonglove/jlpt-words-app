package com.minseonglove.jlptwords.extension

import java.text.NumberFormat
import java.util.Locale

actual fun Int.formatWithCommas(): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
}
