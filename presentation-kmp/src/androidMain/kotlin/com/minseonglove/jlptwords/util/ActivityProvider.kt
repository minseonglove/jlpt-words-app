package com.minseonglove.jlptwords.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityProvider {
    private var _currentActivity: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = _currentActivity?.get()
        private set(value) {
            _currentActivity = value?.let { WeakReference(it) }
        }

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {
                    currentActivity = activity
                }

                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }

                override fun onActivityDestroyed(activity: Activity) {
                    if (currentActivity == activity) {
                        currentActivity = null
                    }
                }

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}
            },
        )
    }
}
