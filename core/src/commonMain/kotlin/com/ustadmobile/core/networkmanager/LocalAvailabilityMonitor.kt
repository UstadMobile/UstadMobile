package com.ustadmobile.core.networkmanager

interface LocalAvailabilityMonitor {

    fun startMonitoringAvailability(monitor: Any, entryUidsToMonitor: List<Long>)

    fun stopMonitoringAvailability(monitor: Any)

}
