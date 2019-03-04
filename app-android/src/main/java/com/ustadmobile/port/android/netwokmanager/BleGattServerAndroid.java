package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.sharedse.networkmanager.BleGattServer;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;

/**
 * This class handle all the GATT server device's Bluetooth Low Energy callback
 *
 * <p>
 * <b>Note: Operation Flow</b>
 *<p>
 * - When a client wants to send data it requests for a permission to write
 * on the characteristic. Upon receiving that request
 * {@link BluetoothGattServerCallback#onCharacteristicWriteRequest} will be invoked
 * , permission will be granted with {@link BluetoothGatt#GATT_SUCCESS}
 * and the packets will be received on the same
 * {@link BluetoothGattServerCallback#onCharacteristicWriteRequest} method.
 *<p>
 * - When a client device tries to read modified characteristic value,
 * {@link BluetoothGattServerCallback#onCharacteristicReadRequest} will be invoked
 * and the response will be sent back depending on what kind of device tried to read it.
 * If device has same service UUID then {@link BluetoothGatt#GATT_SUCCESS}
 * will be granted, otherwise {@link BluetoothGatt#GATT_FAILURE}
 *
 *  @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleGattServerAndroid extends BleGattServer {

    private BluetoothGattServer gattServer;

    private BleMessage receivedMessage;


    private BluetoothGattServerCallback mCallback = new BluetoothGattServerCallback() {

        /**
         * Grant permission a peer device to start reading characteristics values
         */
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            boolean needResponse = characteristic.getProperties() ==
                    BluetoothGattCharacteristic.PROPERTY_WRITE;
            if(needResponse){
               /* Reject all direct characteristics read from unknown source
                (one of our characteristics has NO_RESPONSE set).*/
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0,
                        characteristic.getValue());
            }else{
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                        characteristic.getValue());

            }
        }

        /**
         * Start receiving message packets sent from peer device
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);

            if (USTADMOBILE_BLE_SERVICE_UUID.equals(characteristic.getUuid())) {
                //Grant permission to the peer device to write on this characteristics
                boolean granted= gattServer.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, 0, null);
                UstadMobileSystemImpl.l(UMLog.DEBUG,691,
                        "Write permission granted for "+device.getAddress()+" "+granted);
                //start receiving packets from the client device
                boolean packetsReceived = receivedMessage.onPackageReceived(value);
                UstadMobileSystemImpl.l(UMLog.DEBUG,691,
                        "Received all packets from "+device.getAddress()+" "
                                + packetsReceived);
                if(packetsReceived){
                    int currentMtuSize = receivedMessage.getMtu();

                    UstadMobileSystemImpl.l(UMLog.ERROR,691,
                            "Request received with default MTU size of " + currentMtuSize);

                    //Send back response
                    BleMessage messageToSend =  handleRequest(receivedMessage);
                    receivedMessage.reset();
                    UstadMobileSystemImpl.l(UMLog.DEBUG,691,
                            "Prepare response to send back to "+device.getAddress());
                    //Our service doesn't require confirmation, if it does then reject sending packets
                    boolean requireConfirmation = (characteristic.getProperties() &
                            BluetoothGattCharacteristic.PROPERTY_INDICATE)
                            == BluetoothGattCharacteristic.PROPERTY_INDICATE;
                    if(!requireConfirmation){
                        byte[][] packets = messageToSend.getPackets(currentMtuSize);
                        for (byte[] packet : packets) {
                            characteristic.setValue(packet);
                             boolean notified = gattServer.notifyCharacteristicChanged(device,
                                     characteristic, false);
                             if(notified){
                                 UstadMobileSystemImpl.l(UMLog.DEBUG,691,
                                         "Pee device notified on characteristics change");
                             }else{
                                 UstadMobileSystemImpl.l(UMLog.ERROR,691,
                                         "Failed to notify peer device");
                             }
                        }
                        UstadMobileSystemImpl.l(UMLog.DEBUG,691,
                                "Response sent to "+device.getAddress());
                    }
                }
            }
        }
    };

    /**
     * Constructor which will be used when creating new instance of BleGattServerAndroid
     * @param context Application context
     * @param networkManager Instance of a NetworkManagerAndroidBle for getting
     *                       BluetoothManager instance.
     */
    BleGattServerAndroid(Context context, NetworkManagerAndroidBle networkManager) {
        super(context);
        this.receivedMessage = new BleMessage();
        this.gattServer = networkManager.getBluetoothManager().openGattServer(context,mCallback);
        setNetworkManager(networkManager);
    }

    @VisibleForTesting
    BluetoothGattServerCallback getGattServerCallback() {
        return mCallback;
    }

    /**
     * Get instance of a BluetoothGattServer
     * @return Instance of BluetoothGattServer
     */
    BluetoothGattServer getGattServer() {
        return gattServer;
    }
}
