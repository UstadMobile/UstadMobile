package com.ustadmobile.core.networkmanager

import kotlin.js.JsName

/**
 * Listener for all entry availability status
 */
interface LocalAvailabilityListener {

    /**
     * Fired when availability status changes
     * @param locallyAvailableEntries set of all entry Uids
     */
    @JsName("onLocalAvailabilityChanged")
    fun onLocalAvailabilityChanged(locallyAvailableEntries: Set<Long>)

}
