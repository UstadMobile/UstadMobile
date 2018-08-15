package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil;

import java.util.List;

import static android.content.Context.BLUETOOTH_SERVICE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;

public class BleEntryStatusTaskAndroid extends BleEntryStatusTask {

    private BleMessageGattClientCallback mCallback;

    private BluetoothManager bluetoothManager;

    private NetworkNode peerToCheck;

    private Context context;




    public BleEntryStatusTaskAndroid(Context context, List<Long> entryUidsToCheck, NetworkNode peerToCheck) {
        super(context,entryUidsToCheck,peerToCheck);
        this.context = context;
        this.peerToCheck = peerToCheck;
        this.bluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        byte [] messagePayload = BleMessageUtil.bleMessageLongToBytes(entryUidsToCheck);
        this.message = new BleMessage(ENTRY_STATUS_REQUEST,messagePayload,DEFAULT_MTU);
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager){
        this.bluetoothManager = bluetoothManager;
    }

    @Override
    public void run() {
        mCallback = new BleMessageGattClientCallback(message,peerToCheck.getBluetoothMacAddress());
        mCallback.setOnResponseReceived(this);
        BluetoothDevice destinationPeer = bluetoothManager.getAdapter().getRemoteDevice(
                peerToCheck.getBluetoothMacAddress());
        destinationPeer.connectGatt(context,false,mCallback);
    }


    public BleMessageGattClientCallback getGattClientCallback() {
        return mCallback;
    }


}
