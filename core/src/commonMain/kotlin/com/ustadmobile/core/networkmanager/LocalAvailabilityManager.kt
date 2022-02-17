package com.ustadmobile.core.networkmanager


class AvailabilityMonitorRequest(val containerUidsToMonitor: List<Long>,
                                 val onContainerAvailabilityChanged: (Map<Long, Boolean>) -> Unit = {})

