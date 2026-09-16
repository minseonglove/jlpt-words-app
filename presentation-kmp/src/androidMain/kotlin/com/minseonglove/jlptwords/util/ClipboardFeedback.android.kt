package com.minseonglove.jlptwords.util

import android.os.Build

actual val isSystemClipboardFeedbackShown: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
