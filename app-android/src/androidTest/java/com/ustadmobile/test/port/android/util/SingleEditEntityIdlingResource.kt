package com.ustadmobile.test.port.android.util

import androidx.test.espresso.IdlingResource
import com.ustadmobile.port.android.view.UstadEditFragment

class SingleEditEntityIdlingResource(var editFragment: UstadEditFragment<*>) : IdlingResource{

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName() = "EditFragmentIdlingResource"

    override fun isIdleNow(): Boolean {
        val isIdle = !editFragment.loading && editFragment.fieldsEnabled

        if(isIdle) {
            resourceCallback?.onTransitionToIdle()
        }

        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}