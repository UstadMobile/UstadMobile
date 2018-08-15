package com.ustadmobile.port.android.netwokmanager;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.support.annotation.VisibleForTesting;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

/**
 * <h1>BleMessageGattClientCallback</h1>
 */

public class BleMessageGattClientCallback extends  BluetoothGattCallback{

    private BleMessage messageToSend, receivedMessage;

    private String destinationAddress;

    private BleMessageResponseListener responseListener;

    private int packetIteration = 0;




    public BleMessageGattClientCallback(BleMessage messageToSend, String destinationAddress){
        this.messageToSend = messageToSend;
        receivedMessage = new BleMessage();
        this.destinationAddress = destinationAddress;
    }

    public void setOnResponseReceived(BleMessageResponseListener responseListener){
        this.responseListener = responseListener;
    }



    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (status != BluetoothGatt.GATT_SUCCESS) {
            gatt.disconnect();
            return;
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            //TODO: add node to the known nodes
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            //TODO: remove node from known nodes
            gatt.disconnect();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        List<BluetoothGattService> serviceList = gatt.getServices();
        BluetoothGattCharacteristic characteristic = serviceList.get(0).getCharacteristics().get(0);
        if(characteristic.getUuid().equals(USTADMOBILE_BLE_SERVICE_UUID)){
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            gatt.setCharacteristicNotification(characteristic, true);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if(packetIteration < messageToSend.getPackets().length){
                characteristic.setValue(messageToSend.getPackets()[packetIteration]);
                gatt.writeCharacteristic(characteristic);
                packetIteration++;
            }else{
                packetIteration = 0;
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        readCharacteristics(characteristic);

    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        readCharacteristics(characteristic);
    }


    private void readCharacteristics(BluetoothGattCharacteristic characteristic){

        boolean isReceived = receivedMessage.onPackageReceived(characteristic.getValue());
        if(isReceived){
            responseListener.onResponseReceived(receivedMessage);
        }
    }

    @VisibleForTesting
    public BleMessageResponseListener getResponseListener(){
        return responseListener;
    }

    @VisibleForTesting
    public BleMessage getReceivedMessage() {
        return receivedMessage;
    }
}
