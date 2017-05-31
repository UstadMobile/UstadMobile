package com.ustadmobile.port.sharedse.networkmanager;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kileha3 on 09/05/2017.
 */

public interface BluetoothConnectionHandler {
    void onConnected(InputStream inputStream, OutputStream outputStream);
    void onConnectionFailed(String bluetoothAddress);
}
