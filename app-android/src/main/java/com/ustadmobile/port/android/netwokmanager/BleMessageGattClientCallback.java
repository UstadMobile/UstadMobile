package com.ustadmobile.port.android.netwokmanager;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 * <p>
 * <b>Note: Operation Flow</b>
 *<p>
 * - When a device is connected to a BLE node, if it  has android version 5 and above
 * it will request for the MTU change and upon receiving a call back on
 * {{@link BleMessageGattClientCallback#onMtuChanged}} it will updateState MTU and request for
 * all available services from the GATT otherwise it will request for available services.
 * This will be achieved by calling {@link BluetoothGatt#discoverServices()}.
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

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleMessageGattClientCallback extends  BluetoothGattCallback{

    private BleMessage messageToSend, receivedMessage;

    private BleMessageResponseListener responseListener;

    private int packetIteration = 0;

    private final AtomicBoolean serviceDiscoveryRef = new AtomicBoolean(false);

    private AtomicBoolean mConnected = new AtomicBoolean(true);

    private AtomicBoolean mClosed = new AtomicBoolean(false);

    private volatile long lastActive;

    private Runnable mTimeoutRunnable = () -> {

    };

    /**
     * Constructor to be called when creating new callback
     * @param messageToSend Payload to be sent to the peer device (List of entry Id's)
     */
    public BleMessageGattClientCallback(BleMessage messageToSend){
        this.messageToSend = messageToSend;
        receivedMessage = new BleMessage();
        lastActive = System.currentTimeMillis();
    }

    /**
     * Set listener to report back results on the listening part.
     * @param responseListener BleMessageResponseListener listener
     */
    void setOnResponseReceived(BleMessageResponseListener responseListener){
        this.responseListener = responseListener;
    }

    /**
     * Start discovering GATT services when peer device is connected or disconnects from GATT
     * when connection failed.
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        String remoteDeviceAddress = gatt.getDevice().getAddress();

        if(status == BluetoothGatt.GATT_SUCCESS &&
                newState == BluetoothProfile.STATE_CONNECTED) {
            UMLog.l(UMLog.DEBUG,698,
                    "Device connected to " + remoteDeviceAddress);

            if(!serviceDiscoveryRef.get()){
                UMLog.l(UMLog.DEBUG,698,
                        "Discovering services offered by " + remoteDeviceAddress);
                serviceDiscoveryRef.set(true);
                gatt.discoverServices();
            }
        }else {
            cleanup(gatt);
            UMLog.l(UMLog.DEBUG,698,
                    "Connection disconnected " + status + "from "
                            + remoteDeviceAddress);
            if(responseListener != null) {
                responseListener.onResponseReceived(remoteDeviceAddress, null,
                        new IOException("BLE onConnectionStateChange not successful." +
                                "Status = " + status));
            }
        }

    }



    /**
     * Enable notification to be sen't back when characteristics are modified
     * from the GATT server's side.
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        BluetoothGattService service = findMatchingService(gatt.getServices());
        if(service == null){
            UMLog.l(UMLog.ERROR,698,
                    "ERROR Ustadmobile Service not found on " + gatt.getDevice().getAddress());
            responseListener.onResponseReceived(gatt.getDevice().getAddress(), null,
                    new IOException("UstadMobile service not found on device"));
            cleanup(gatt);
            return;
        }

        UMLog.l(UMLog.DEBUG,698,
                "Ustadmobile Service found on " + gatt.getDevice().getAddress());
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

        BluetoothGattCharacteristic characteristic = characteristics.get(0);
        if(characteristic.getUuid().equals(USTADMOBILE_BLE_SERVICE_UUID)){
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            gatt.setCharacteristicNotification(characteristic, true);
            onCharacteristicWrite(gatt,characteristic,BluetoothGatt.GATT_SUCCESS);
        }
    }

    /**
     * Start transmitting message packets to the peer device once given permission
     * to write on the characteristic
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        byte[][] packets = messageToSend.getPackets(DEFAULT_MTU_SIZE);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if(packetIteration < packets.length){
                characteristic.setValue(packets[packetIteration]);
                gatt.writeCharacteristic(characteristic);
                packetIteration++;
                UMLog.l(UMLog.DEBUG,698,
                        "Transferring packet #" + packetIteration + " to "
                                + gatt.getDevice().getAddress());
            }else{
                packetIteration = 0;
                UMLog.l(UMLog.DEBUG,698,
                        packets.length + " packet(s) transferred successfully to " +
                                "the remote device =" + gatt.getDevice().getAddress());
                //We now expect the server to send a response
            }
        }
    }

    /**
     * Read modified valued from the characteristics when changed from GATT server's end.
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        readCharacteristics(gatt,characteristic);
    }

    /**
     * Receive notification when characteristics value has been changed from GATT server's side.
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        readCharacteristics(gatt,characteristic);
    }

    /**
     * Read values from the service characteristic
     * @param gatt Bluetooth Gatt object
     * @param characteristic Modified service characteristic to read that value from
     */
    private void readCharacteristics(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic){
        boolean messageComplete = receivedMessage.onPackageReceived(characteristic.getValue());
        if(messageComplete){
            responseListener.onResponseReceived(gatt.getDevice().getAddress(), receivedMessage,
                    null);
            //The server should disconnect us shortly.
        }
    }


    /**
     * Find the matching service among services found by peer devices
     * @param serviceList List of all found services
     * @return Matching service
     */
    private BluetoothGattService findMatchingService(List<BluetoothGattService> serviceList) {
        for (BluetoothGattService service : serviceList) {
            String serviceIdString = service.getUuid().toString();
            if (matchesServiceUuidString(serviceIdString)) {
                return service;
            }
        }
        return null;
    }

    private boolean matchesServiceUuidString(String serviceIdString) {
        return uuidMatches(serviceIdString, USTADMOBILE_BLE_SERVICE_UUID.toString());
    }


    private boolean uuidMatches(String uuidString, String... matches) {
        for (String match : matches) {
            if (uuidString.equalsIgnoreCase(match)) {
                return true;
            }
        }
        return false;
    }

    private void cleanup(BluetoothGatt gatt) {
        try {
            if(mConnected.get()) {
                gatt.disconnect();
                mConnected.set(false);
                UMLog.l(UMLog.INFO, 698, "GattClientCallback: disconnected");
            }

            if(!mClosed.get()) {
                gatt.close();
                mClosed.set(true);
                UMLog.l(UMLog.INFO, 698, "GattClientCallback: closed");
            }
        }catch(Exception e) {
            UMLog.l(UMLog.ERROR, 698, "GattClientCallback: ERROR disconnecting");
        }finally {
            UMLog.l(UMLog.INFO, 698, "GattClientCallback: closed");
        }
    }
}
