package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * <h1>BleEntryStatusTask</h1>
 *
 * BleEntryStatusTask is executed by an Executor. It will query a given peer to determine if
 * it has a set of entryUids for local download.
 */
public abstract class BleEntryStatusTask implements Runnable,BleMessageResponseListener {

    public BleMessage message;

    public BleEntryStatusTask(List<Long> entryUidsToCheck, NetworkNode peerToCheck) {

    }

    @Override
    public void onResponseReceived(BleMessage response) {

    }

    public BleMessage getMessage() {
        return message;
    }
}
