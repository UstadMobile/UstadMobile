package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID


/**
 * This class handle all the GATT server device's Bluetooth Low Energy callback
 *
 *
 *
 * **Note: Operation Flow**
 *
 *
 * - When a client wants to send data it requests for a permission to write
 * on the characteristic. Upon receiving that request
 * [BluetoothGattServerCallback.onCharacteristicWriteRequest] will be invoked
 * , permission will be granted with [BluetoothGatt.GATT_SUCCESS]
 * and the packets will be received on the same
 * [BluetoothGattServerCallback.onCharacteristicWriteRequest] method.
 *
 *
 * - When a client device tries to read modified characteristic value,
 * [BluetoothGattServerCallback.onCharacteristicReadRequest] will be invoked
 * and the response will be sent back depending on what kind of device tried to read it.
 * If device has same service UUID then [BluetoothGatt.GATT_SUCCESS]
 * will be granted, otherwise [BluetoothGatt.GATT_FAILURE]
 *
 * @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleGattServer
/**
 * Constructor which will be used when creating new instance of BleGattServer
 * @param context Application context
 * @param networkManager Instance of a NetworkManagerBle for getting
 * BluetoothManager instance.
 */
(context: Context, networkManager: NetworkManagerBle) : BleGattServerCommon(context) {

    /**
     * Get instance of a BluetoothGattServer
     * @return Instance of BluetoothGattServer
     */
    @set:VisibleForTesting
    var gattServer: BluetoothGattServer? = null

    private val messageAssembler = BleMessageAssembler()

    @get:VisibleForTesting
            /**
             * Grant permission a peer device to start reading characteristics values
             *//* Reject all direct characteristics read from unknown source
                (one of our characteristics has NO_RESPONSE set).*/
            /**
             * Start receiving message packets sent from peer device
             *///Grant permission to the peer device to write on this characteristics
    //start receiving packets from the client device
    //Send back response
    //Our service doesn't require confirmation, if it does then reject sending packets
    val gattServerCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback() {

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            val needResponse = characteristic.properties == BluetoothGattCharacteristic.PROPERTY_WRITE
            if (needResponse) {
                gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0,
                        characteristic.value)
            } else {
                gattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                        characteristic.value)

            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int,
                                                  characteristic: BluetoothGattCharacteristic,
                                                  preparedWrite: Boolean, responseNeeded: Boolean,
                                                  offset: Int, value: ByteArray) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value)

            if (USTADMOBILE_BLE_SERVICE_UUID == characteristic.uuid.toString()) {
                val granted = gattServer!!.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, 0, null)
                UMLog.l(UMLog.DEBUG, 691,
                        "Write permission granted for " + device.address + " " + granted)
                val messageReceived = messageAssembler.handleIncomingPacket(
                        device.address, value)
                UMLog.l(UMLog.DEBUG, 691,
                        "Received all packets from " + device.address + " "
                                + (messageReceived != null))
                if (messageReceived != null) {
                    val currentMtuSize = messageReceived.mtu

                    UMLog.l(UMLog.DEBUG, 691,
                            "Request received with default MTU size of $currentMtuSize")
                    val messageToSend = handleRequest(messageReceived)

                    UMLog.l(UMLog.DEBUG, 691,
                            "Prepare response to send back to " + device.address)
                    val requireConfirmation = characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE
                    if (!requireConfirmation) {
                        val packets = messageToSend!!.getPackets(currentMtuSize)
                        var packetTracker = 0
                        for (packet in packets) {
                            characteristic.value = packet
                            val notified = gattServer!!.notifyCharacteristicChanged(device, characteristic, false)
                            if (notified) {
                                packetTracker++
                                UMLog.l(UMLog.DEBUG, 691,
                                        "Peer device notified on characteristics change for packet #${packetTracker} size ${packet.size}")
                            } else {
                                UMLog.l(UMLog.ERROR, 691,
                                        "Failed to notify peer device for packet #${packetTracker}")
                            }
                        }
                        UMLog.l(UMLog.DEBUG, 691,
                                "Response sent to " + device.address)

                        gattServer!!.cancelConnection(device)
                        UMLog.l(UMLog.DEBUG, 691,
                                "Response finished, canceled connection with  " + device.address)
                    }
                }
            }
        }
    }

    init {
        this.gattServer = networkManager.getBluetoothManager().openGattServer(context, gattServerCallback)
        setNetworkManager(networkManager)
    }
}
