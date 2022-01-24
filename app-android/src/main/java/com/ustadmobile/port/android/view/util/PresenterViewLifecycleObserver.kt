package com.ustadmobile.port.android.view.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ustadmobile.core.controller.UstadBaseController

class PresenterViewLifecycleObserver(var presenter: UstadBaseController<*>?) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        presenter?.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        presenter?.onPause()
    }

    override fun onStart(owner: LifecycleOwner) {
        presenter?.onStart()
    }

    override fun onStop(owner: LifecycleOwner) {
        presenter?.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        presenter?.onDestroy()
        presenter = null
    }
}
