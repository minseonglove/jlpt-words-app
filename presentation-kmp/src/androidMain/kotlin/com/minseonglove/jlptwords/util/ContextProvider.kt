package com.minseonglove.jlptwords.util

import android.app.Application
import android.content.Context
import java.lang.ref.WeakReference

object ContextProvider {
    private var _applicationContext: WeakReference<Context>? = null

    var applicationContext: Context?
        get() = _applicationContext?.get()
        private set(value) {
            _applicationContext = value?.let { WeakReference(it) }
        }

    fun register(application: Application) {
        applicationContext = application.applicationContext
    }
}
