package com.minseonglove.jlptwords.extension

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter

actual fun Int.formatWithCommas(): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = platform.Foundation.NSNumberFormatterDecimalStyle
    return formatter.stringFromNumber(NSNumber(this)) ?: this.toString()
}
