package com.ustadmobile.core.networkmanager

interface LocalAvailabilityListener {

    fun onLocalAvailabilityChanged(locallyAvailableEntries: Set<Long>)

}
