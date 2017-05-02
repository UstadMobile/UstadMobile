package com.ustadmobile.port.sharedse.network;

/**
 * Created by kileha3 on 07/03/2017.
 */

public interface NetworkTaskListener {
    void downloadTaskEnded(DownloadTask task);
    void bluetoothTaskEnded(BluetoothTask task);

}
