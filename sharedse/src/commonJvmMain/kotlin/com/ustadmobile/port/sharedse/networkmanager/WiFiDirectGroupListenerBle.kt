package com.ustadmobile.port.sharedse.networkmanager

import com.ustadmobile.sharedse.network.WiFiDirectGroupBle

/**
 * Interface for listening WiFi Direct group creation and removal
 *
 * @author kileha3
 */

interface WiFiDirectGroupListenerBle {

    /**
     * A new WiFi direct group was created - or creation failed
     *
     * @param group The group created or null if it was not created due to an error
     * @param err The exception if any occurred attempting to create the group,
     * otherwise null
     */
    fun groupCreated(group: WiFiDirectGroupBle, err: Exception)

    /**
     * WiFi direct group removal was attempted.
     *
     * @param successful True if the group was successfully removed
     * @param err The exception if any
     */
    fun groupRemoved(successful: Boolean, err: Exception)

}
