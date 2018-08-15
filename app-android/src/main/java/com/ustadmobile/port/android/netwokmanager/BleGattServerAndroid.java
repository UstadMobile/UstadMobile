package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.ustadmobile.port.sharedse.networkmanager.BleGattServer;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;

public class BleGattServerAndroid extends BleGattServer{

    private BluetoothGattServer gattServer;

    private BleMessage receivedMessage;

    private NetworkManagerAndroidBle networkManager;

    private BluetoothGattServerCallback mCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            boolean needResponse = characteristic.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE;
            if(needResponse){
                //Reject all direct characteristics read from unknown source (one of our characteristics has NO_RESPONSE set).
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, characteristic.getValue());
            }else{
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, characteristic.getValue());

            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            if (USTADMOBILE_BLE_SERVICE_UUID.equals(characteristic.getUuid())) {
                //Grant permission to the client device to write on this characteristics
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                //start receiving packets from the client device
                receivedMessage.onPackageReceived(value);
            }
        }


        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }
    };

    public BleGattServerAndroid(Context context, NetworkManagerAndroidBle networkManager ) {
        this.receivedMessage = new BleMessage();
        this.gattServer = networkManager.getBluetoothManager().openGattServer(context,mCallback);
        this.networkManager = networkManager;
    }

    @VisibleForTesting
    protected BluetoothGattServerCallback getGattServerCallback() {
        return mCallback;
    }

}
