package com.ustadmobile.core.networkmanager;

import java.util.List;

public interface LocalAvailabilityMonitor {

    void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor);

    void stopMonitoringAvailability(Object monitor);

}
