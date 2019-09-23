package com.ustadmobile.core.networkmanager

import kotlin.js.JsName

interface LocalAvailabilityMonitor {
    @JsName("startMonitoringAvailability")
    fun startMonitoringAvailability(monitor: Any, entryUidsToMonitor: List<Long>)

    @JsName("stopMonitoringAvailability")
    fun stopMonitoringAvailability(monitor: Any)

}
