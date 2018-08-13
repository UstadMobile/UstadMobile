package com.ustadmobile.core.networkmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;

import java.util.List;

/**
 * <h1>NetworkManagerCoreBle</h1>
 *
 *
 */
public abstract class NetworkManagerCoreBle {


    public abstract boolean isWiFiEnabled();


    public abstract boolean setWifiEnabled(boolean enabled);


    public abstract void startMonitoringAvailability(Object monitor, List<Long> entryUidsToMonitor);

    public abstract void stopMonitoringAvailability(Object monitor);
}
