package com.ustadmobile.core.networkmanager;

import java.util.Set;

/**
 * Created by mike on 8/8/17.
 */

public class AvailabilityMonitorRequest {

    Set<String> entryIdsToMonitor;

    public AvailabilityMonitorRequest(Set<String> entryIdsToMonitor) {
        this.entryIdsToMonitor = entryIdsToMonitor;
    }

    public Set<String> getEntryIdsToMonitor() {
        return entryIdsToMonitor;
    }




}
