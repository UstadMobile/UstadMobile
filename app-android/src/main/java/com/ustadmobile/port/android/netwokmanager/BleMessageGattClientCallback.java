package com.ustadmobile.port.android.netwokmanager;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.annotation.VisibleForTesting;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 * <p>
 * <b>Note: Operation Flow</b>
 *<p>
 * - When a device is connected to a BLE node, it will request for all available services
 * from the GATT. This will be achieved by calling {@link BluetoothGatt#discoverServices()}.
 * Once services are found, all characteristics in those services will be listed.
 *<p>
 * - When trying to send a message, it will need write permission from the BLE node,
 * and when requested response might be as discussed in {@link BleGattServerAndroid}.
 *<p>
 * - If it will receive {@link BluetoothGatt#GATT_SUCCESS} response, then it will start data
 * transmission to the BLE node. Upon receiving response the
 * {@link BleMessageGattClientCallback#onCharacteristicChanged} method will be invoked.
 * </p>
 *
 *  @author kileha3
 */

public class BleMessageGattClientCallback extends  BluetoothGattCallback{

    private BleMessage messageToSend, receivedMessage;

    private String destinationAddress;

    private BleMessageResponseListener responseListener;

    private int packetIteration = 0;

    /**
     * Constructor to be called when creating new callback
     * @param messageToSend Payload to be sent to the peer device (List of entry Id's)
     * @param destinationAddress Bluetooth MAC address for the peer device.
     */
    public BleMessageGattClientCallback(BleMessage messageToSend, String destinationAddress){
        this.messageToSend = messageToSend;
        receivedMessage = new BleMessage();
        this.destinationAddress = destinationAddress;
    }

    /**
     * Set listener to report back results on the listening part.
     * @param responseListener BleMessageResponseListener listener
     */
    public void setOnResponseReceived(BleMessageResponseListener responseListener){
        this.responseListener = responseListener;
    }

    /**
     * Start discovering GATT services when peer device is connected or disconnects from GATT
     * when connection failed.
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (status != BluetoothGatt.GATT_SUCCESS) {
            gatt.disconnect();
            return;
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.disconnect();
        }
    }

    /**
     * Enable notification to be sen't back when characteristics are modified
     * from the GATT server's side.
     */
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

    /**
     * Start transmitting message packets to the peer device once given permission
     * to write on the characteristic
     */
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

    /**
     * Read modified valued from the characteristics when changed from GATT server's end.
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        readCharacteristics(gatt.getDevice().getAddress(),characteristic);

    }

    /**
     * Receive notification when characteristics value has been changed from GATT server's side.
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        readCharacteristics(gatt.getDevice().getAddress(),characteristic);
    }

    /**
     * Read values from the service characteristic
     * @param sourceDeviceAddress Peer device bluetooth MAC address from which is reading from.
     * @param characteristic Modified service characteristic to read that value from
     */
    private void readCharacteristics(String sourceDeviceAddress,BluetoothGattCharacteristic characteristic){
        boolean isReceived = receivedMessage.onPackageReceived(characteristic.getValue());
        if(isReceived){
            responseListener.onResponseReceived(sourceDeviceAddress,receivedMessage);
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
