package com.ustadmobile.port.android.netwokmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;

import java.util.List;

public class BleEntryStatusTaskAndroid extends BleEntryStatusTask {

    private BleMessageGattClientCallback mCallback;

    public BleEntryStatusTaskAndroid(List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        super(entryUidsToCheck, peerToCheck);
    }

    @Override
    public void run() {

    }


    public BleMessageGattClientCallback getGattClientCallback() {
        return mCallback;
    }
}
