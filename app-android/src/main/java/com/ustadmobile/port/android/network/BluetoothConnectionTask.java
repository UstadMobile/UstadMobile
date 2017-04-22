package com.ustadmobile.port.android.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.ustadmobile.port.sharedse.network.BluetoothTask;
import com.ustadmobile.port.sharedse.network.NetworkNode;

/**
 * Created by kileha3 on 17/04/2017.
 */

public class BluetoothConnectionTask extends BluetoothTask {

    private BluetoothConnectionManager bConnectionManager;
    private BluetoothAdapter mBluetoothAdapter;

    BluetoothConnectionTask(NetworkNode networkNode, NetworkManagerAndroid p2pManager) {
        super(networkNode);
        bConnectionManager=p2pManager.getBluetoothConnectionManager();
        mBluetoothAdapter=bConnectionManager.getBluetoothAdapter();
    }

    @Override
    public synchronized void start() {

        String bluetoothAddress=getNode().getNodeBluetoothAddress();
        BluetoothDevice bluetoothDevice=mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        bConnectionManager.connectToBluetoothDevice(bluetoothDevice,false);

    }
}
