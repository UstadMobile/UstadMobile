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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.MAXIMUM_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 * <p>
 * <b>Note: Operation Flow</b>
 *<p>
 * - When a device is connected to a BLE node, if it  has android version 5 and above
 * it will request for the MTU change and upon receiving a call back on
 * {{@link BleMessageGattClientCallback#onMtuChanged}} it will update MTU and request for
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

    private int defaultMtuSize = DEFAULT_MTU_SIZE;

    private final CountDownLatch mLatch = new CountDownLatch(1);


    /**
     * Constructor to be called when creating new callback
     * @param messageToSend Payload to be sent to the peer device (List of entry Id's)
     */
    BleMessageGattClientCallback(BleMessage messageToSend){
        this.messageToSend = messageToSend;
        receivedMessage = new BleMessage();
    }

    /**
     * Set listener to report back results on the listening part.
     * @param responseListener BleMessageResponseListener listener
     */
    void setOnResponseReceived(BleMessageResponseListener responseListener){
        this.responseListener = responseListener;
    }

    /**
     * Changing Maximum Transfer Unit
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        //Successfully changed the MTU, update message and notify to start discovering service
        this.defaultMtuSize = mtu;
        mLatch.countDown();
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
            UstadMobileSystemImpl.l(UMLog.DEBUG,698,
                    "Connection failed with error code "+status);
            return;
        }

        if(newState == BluetoothProfile.STATE_CONNECTED) {
            /*Check if the device has android version 5 or above and request for the MTU change,
            MTU change is not supported on lower android version devices*/
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                if(gatt.requestMtu(MAXIMUM_MTU_SIZE)){
                    try {
                        mLatch.wait(TimeUnit.SECONDS.toMillis(2));
                    } catch (InterruptedException e) {
                        mLatch.countDown();
                        e.printStackTrace();
                    }
                }
            }
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
        BluetoothGattService service = findMatchingService(gatt.getServices());
        if(service == null){
            return;
        }
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
        byte[][] packets = messageToSend.getPackets(defaultMtuSize);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if(packetIteration < packets.length){
                characteristic.setValue(packets[packetIteration]);
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
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        readCharacteristics(gatt.getDevice().getAddress(),characteristic);
    }

    /**
     * Receive notification when characteristics value has been changed from GATT server's side.
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        readCharacteristics(gatt.getDevice().getAddress(),characteristic);
    }

    /**
     * Read values from the service characteristic
     * @param sourceDeviceAddress Peer device bluetooth MAC address from which is reading from.
     * @param characteristic Modified service characteristic to read that value from
     */
    private void readCharacteristics(String sourceDeviceAddress,
                                     BluetoothGattCharacteristic characteristic){
        boolean isReceived = receivedMessage.onPackageReceived(characteristic.getValue());
        if(isReceived){
            responseListener.onResponseReceived(sourceDeviceAddress,receivedMessage);
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
}
