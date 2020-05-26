package com.ustadmobile.port.android.view

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingResource

class RecyclerViewIdlingResource(recyclerView: RecyclerView?, var minItemCount : Int = 0): IdlingResource {

    var recyclerView: RecyclerView? = recyclerView
        set(value) {
            field = value
            isIdleNow // call this so that if it has now transitioned to being idle, due to this being set, and there is a callback, it will be called
        }

    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
        return "RecyclerViewIdlingResource"
    }

    override fun isIdleNow(): Boolean {
        val recyclerViewVal = recyclerView
        val isIdle = recyclerViewVal != null && !recyclerViewVal.hasPendingAdapterUpdates() && (recyclerViewVal.adapter?.itemCount ?: 0) > minItemCount
        println("isIdle: $isIdle")

        if(isIdle) {
            resourceCallback?.onTransitionToIdle()
        }


        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

}