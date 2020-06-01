package com.ustadmobile.port.android.view.util

import androidx.appcompat.app.ActionBar
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * This is intended to observe the view lifecycle of a fragment for purposes of setting the
 * title when the underlying lifecycle is active (eg resumed)
 */
class TitleLifecycleObserver(title: String?, var supportToolbar: ActionBar?): DefaultLifecycleObserver{

    var active: Boolean = false

    var title: String? = title
        set(value) {
            field = value
            if(active && value != null)
                supportToolbar?.title = value
        }

    override fun onResume(owner: LifecycleOwner) {
        supportToolbar?.takeIf { title != null }?.title = title
        active = true
    }

    override fun onPause(owner: LifecycleOwner) {
        active = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        supportToolbar = null
        title = null
    }
}