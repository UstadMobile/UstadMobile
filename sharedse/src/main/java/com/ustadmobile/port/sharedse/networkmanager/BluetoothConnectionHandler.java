package com.ustadmobile.port.sharedse.networkmanager;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <h1>BluetoothConnectionHandler</h1>
 *
 * This is an interface which handle all bluetooth connection events.
 *
 *
 * @author kileha3
 */
public interface BluetoothConnectionHandler {
    /**
     * Method to be invoked when connection is made
     * @param inputStream InputStream to read data from.
     * @param outputStream OutputStream to write data to.
     */
    void onConnected(InputStream inputStream, OutputStream outputStream);
}
