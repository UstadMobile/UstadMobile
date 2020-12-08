package com.ustadmobile.port.android.view.util

import android.widget.ProgressBar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Helps fragments manage the progressbar which is hosted on the activity inside the AppBarLayout.
 */
class ProgressBarLifecycleObserver(var progressBar: ProgressBar?,
                                   visibility: Int): DefaultLifecycleObserver {


    private var active: Boolean = false

    var visibility: Int = visibility
        set(value) {
            progressBar?.takeIf { active }?.visibility = value
            field = value
        }

    override fun onResume(owner: LifecycleOwner) {
        active = true
        progressBar?.visibility = visibility
    }

    override fun onPause(owner: LifecycleOwner) {
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        progressBar = null
    }
}