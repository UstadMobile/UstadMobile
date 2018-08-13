package com.ustadmobile.port.android.netwokmanager;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;

/**
 * <h1>BleMessageGattClientCallback</h1>
 */

public class BleMessageGattClientCallback {

    private BleMessage message;

    private String destinationAddress;

    private BleMessageResponseListener responseListener;

    private BluetoothManager bluetoothManager;


    private BluetoothGattCallback mCallback = new BluetoothGattCallback(){

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    public BleMessageGattClientCallback(BluetoothManager bluetoothManager, BleMessage message, String destinationAddress){
        this.message = message;
        this.destinationAddress = destinationAddress;
        this.bluetoothManager = bluetoothManager;
    }

    public void onResponseReceived(BleMessageResponseListener responseListener){
        this.responseListener = responseListener;
    }

    protected BluetoothGattCallback getGattClientCallback(){
        return mCallback;
    }

    public BleMessage getMessage() {
        return message;
    }
}
