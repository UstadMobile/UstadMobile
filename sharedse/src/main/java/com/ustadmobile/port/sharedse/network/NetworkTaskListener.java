package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 07/03/2017.
 */

public interface NetworkTaskListener {

    void taskEnded(P2PTask task);
    void taskEnded(BluetoothTask task);

}
