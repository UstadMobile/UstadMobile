package com.ustadmobile.port.sharedse.networkmanager;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by kileha3 on 09/05/2017.
 */

public abstract class BluetoothServer {

    public static final int BLUETOOTH_STATE_NONE=-1;
    public static final int BLUETOOTH_STATE_CONNECTING=1;
    public static final int BLUETOOTH_STATE_CONNECTED=2;
    public static final int BLUETOOTH_STATE_FAILED =0;
    public BluetoothConnectionHandler bluetoothConnectionHandler;

    public abstract void start();
    public abstract void stop();
    public abstract void handleNodeConnected(String deviceAddress,DataInputStream inputStream,
                                             DataOutputStream outputStream);
    public void setBluetoothConnectionHandler(
            BluetoothConnectionHandler handler){
        this.bluetoothConnectionHandler=handler;
    }

}
