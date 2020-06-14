package com.ustadmobile.test.port.android.util

import androidx.test.espresso.IdlingResource
import com.ustadmobile.port.android.view.UstadBaseFragment
import com.ustadmobile.port.android.view.UstadDetailFragment
import com.ustadmobile.port.android.view.UstadEditFragment

class UstadSingleEntityFragmentIdlingResource(var ustadFragment: UstadBaseFragment) : IdlingResource{

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName() = "EditFragmentIdlingResource"

    override fun isIdleNow(): Boolean {
        val fragmentVal = ustadFragment
        val entityValue = (ustadFragment as? UstadEditFragment<*>)?.entity
                ?: (ustadFragment as? UstadDetailFragment<*>)?.entity

        val isIdle = (entityValue != null) && if(fragmentVal is UstadEditFragment<*>) {
            !fragmentVal.loading && fragmentVal.fieldsEnabled
        }else if(fragmentVal is UstadDetailFragment<*>){
            !fragmentVal.loading
        }else {
            true
        }

        if(isIdle) {
            resourceCallback?.onTransitionToIdle()
        }

        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}