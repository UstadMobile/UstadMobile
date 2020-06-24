package com.ustadmobile.port.android.view

/**
 * created by @author kileha3
 */
interface FragmentBackHandler{
    /**
     * Returns true if event was handled otherwise false
     */
    fun onHostBackPressed(): Boolean
}