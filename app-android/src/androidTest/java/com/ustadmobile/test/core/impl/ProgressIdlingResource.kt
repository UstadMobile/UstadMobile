package com.ustadmobile.test.core.impl

import android.app.Activity
import android.view.View
import androidx.test.espresso.IdlingResource
import com.toughra.ustadmobile.R

class ProgressIdlingResource(var activity: Activity) : IdlingResource {

    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private var isIdle: Boolean = false

    override fun getName(): String {
        return ProgressIdlingResource::class.java.name
    }

    override fun isIdleNow(): Boolean {
        if (isIdle) return true

        var progressBar = activity.findViewById<View>(R.id.progressBar)
        isIdle = progressBar.visibility == View.INVISIBLE || progressBar.visibility == View.GONE
        if (isIdle) {
            resourceCallback?.onTransitionToIdle()
        }
        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.resourceCallback = callback
    }
}