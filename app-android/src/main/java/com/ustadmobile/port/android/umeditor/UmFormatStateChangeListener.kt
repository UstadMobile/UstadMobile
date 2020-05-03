package com.ustadmobile.port.android.umeditor

/**
 * Interface to listen for the state change of the formatting item
 */
interface UmFormatStateChangeListener {

    /**
     * Invoked when formatting item has been updated
     * @param formatList updated format list
     */
    fun onStateChanged(formatList: List<UmFormat>)
}