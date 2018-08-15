package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

public class BleGattServerAndroid extends BleGattServer {

    private BluetoothGattServer gattServer;

    private BleMessage receivedMessage;

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

    public BleGattServerAndroid(Context context, BluetoothManager bluetoothManager) {
        this.receivedMessage = new BleMessage();
        this.gattServer = bluetoothManager.openGattServer(context,mCallback);
    }


    @VisibleForTesting
    protected BluetoothGattServerCallback getGattServerCallback() {
        return mCallback;
    }

}
