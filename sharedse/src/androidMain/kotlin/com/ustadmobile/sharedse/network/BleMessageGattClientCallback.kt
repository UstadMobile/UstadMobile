package com.ustadmobile.sharedse.network


import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.sharedse.network.NetworkManagerBle.Companion.USTADMOBILE_BLE_SERVICE_UUID_UUID
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MINIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.MAXIMUM_MTU_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.USTADMOBILE_BLE_SERVICE_UUID
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class handle all the GATT client Bluetooth Low Energy callback
 *
 *
 *
 * **Note: Operation Flow**
 *
 *
 * - When a device is connected to a BLE node, if it  has android version 5 and above
 * it will request for the MTU change and upon receiving a call back on
 * {[BleMessageGattClientCallback.onMtuChanged]} it will updateState MTU and request for
 * all available services from the GATT otherwise it will request for available services.
 * This will be achieved by calling [BluetoothGatt.discoverServices].
 * Once services are found, all characteristics in those services will be listed.
 *
 *
 * - When trying to send a message, it will need write permission from the BLE node,
 * and when requested response might be as discussed in [BleGattServer].
 *
 *
 * - If it will receive [BluetoothGatt.GATT_SUCCESS] response, then it will start data
 * transmission to the BLE node. Upon receiving response the
 * [BleMessageGattClientCallback.onCharacteristicChanged] method will be invoked.
 *
 * For more explanation about MTU and MTU throughput, below is an article you gan go through
 * @link https://interrupt.memfault.com/blog/ble-throughput-primer
 *
 * @author kileha3
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleMessageGattClientCallback
/**
 * Constructor to be called when creating new callback
 * @param messageToSend Payload to be sent to the peer device (List of entry Id's)
 */
(private val messageToSend: BleMessage) : BluetoothGattCallback() {
    private val receivedMessage: BleMessage

    private var responseListener: BleMessageResponseListener? = null

    private var packetIteration = 0

    private val serviceDiscoveryRef = AtomicBoolean(false)

    private val mConnected = AtomicBoolean(true)

    private val mClosed = AtomicBoolean(false)

    @Volatile
    private var lastActive: Long

    private var packetTransferAttemptsRemaining = 3

    private val MAX_DELAY_TIME  = 80L

    private var defaultMtuSizeToUse = MINIMUM_MTU_SIZE

    private val mtuCompletableDeferred = CompletableDeferred(MINIMUM_MTU_SIZE)

    init {
        receivedMessage = BleMessage()
        lastActive = System.currentTimeMillis()
    }

    /**
     * Set listener to report back results on the listening part.
     * @param responseListener BleMessageResponseListener listener
     */
    internal fun setOnResponseReceived(responseListener: BleMessageResponseListener) {
        this.responseListener = responseListener
    }

    /**
     * Receive MTU change event when server device changed it's MTU to the requested value.
     */
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        UMLog.l(UMLog.DEBUG, 698, "MTU changed from $defaultMtuSizeToUse to $mtu")
        defaultMtuSizeToUse = mtu
        mtuCompletableDeferred.complete(mtu)
    }

    /**
     * Start discovering GATT services when peer device is connected or disconnects from GATT
     * when connection failed.
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)

        val remoteDeviceAddress = gatt.device.address

        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            UMLog.l(UMLog.DEBUG, 698,
                    "Device connected to $remoteDeviceAddress")

            if (!serviceDiscoveryRef.get()) {
                UMLog.l(UMLog.DEBUG, 698,
                        "Discovering services offered by $remoteDeviceAddress")
                serviceDiscoveryRef.set(true)
                gatt.discoverServices()
            }
        } else {
            cleanup(gatt)
            UMLog.l(UMLog.DEBUG, 698,
                    "Connection disconnected " + status + "from "
                            + remoteDeviceAddress)
            if (responseListener != null) {
                responseListener!!.onResponseReceived(remoteDeviceAddress, null,
                        IOException("BLE onConnectionStateChange not successful." +
                                "Status = " + status))
            }
        }

    }


    /**
     * Enable notification to be sen't back when characteristics are modified
     * from the GATT server's side.
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        val service = findMatchingService(gatt.services)
        if (service == null) {
            UMLog.l(UMLog.ERROR, 698,
                    "ERROR Ustadmobile Service not found on " + gatt.device.address)
            responseListener!!.onResponseReceived(gatt.device.address, null,
                    IOException("UstadMobile service not found on device"))
            cleanup(gatt)
            return
        }

        UMLog.l(UMLog.DEBUG, 698,
                "Ustadmobile Service found on " + gatt.device.address)
        val characteristics = service.characteristics

        val characteristic = characteristics[0]
        if (characteristic.uuid == USTADMOBILE_BLE_SERVICE_UUID_UUID) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.setCharacteristicNotification(characteristic, true)
            GlobalScope.launch(Dispatchers.Main) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    UMLog.l(UMLog.DEBUG, 698,
                            "Requesting MTU changed from $defaultMtuSizeToUse to $MAXIMUM_MTU_SIZE")
                    gatt.requestMtu(MAXIMUM_MTU_SIZE)
                    withTimeoutOrNull(MAX_DELAY_TIME) { mtuCompletableDeferred.await() }
                }
                onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS)
            }
        }
    }


    /**
     * Start transmitting message packets to the peer device once given permission
     * to write on the characteristic
     */
    override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                       characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)

        UMLog.l(UMLog.DEBUG, 698, (if(status == BluetoothGatt.GATT_SUCCESS) "Allowed to send packets"
        else "Not allowed to send packets" ) + " to ${gatt.device.address}")

        val packets = messageToSend.getPackets(defaultMtuSizeToUse)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (packetIteration < packets.size) {
                characteristic.value = packets[packetIteration]
                gatt.writeCharacteristic(characteristic)
                UMLog.l(UMLog.DEBUG, 698, "Transferring packet #${packetIteration + 1}  of size " +
                                "${packets[packetIteration].size} to ${gatt.device.address}")
                packetIteration++

            } else {
                packetIteration = 0
                UMLog.l(UMLog.DEBUG, 698,
                        packets.size.toString() + " packet(s) transferred successfully to " +
                                "the remote device =" + gatt.device.address)
                //We now expect the server to send a response
            }
        }else if(packetTransferAttemptsRemaining > 0) {
            packetTransferAttemptsRemaining--
            UMLog.l(UMLog.DEBUG, 698, "Failed to send packet to ${gatt.device.address}" +
                    " remain $packetTransferAttemptsRemaining trial, trying now...")
            GlobalScope.launch(Dispatchers.Main) {
                delay(MAX_DELAY_TIME)
                onCharacteristicWrite(gatt, characteristic, BluetoothGatt.GATT_SUCCESS)
            }

        }else {
            UMLog.l(UMLog.DEBUG, 698,
                    "Failed many times to send packet #${packetIteration} to ${gatt.device.address} disconnecting now")
            cleanup(gatt)
        }
    }

    /**
     * Read modified valued from the characteristics when changed from GATT server's end.
     */
    override fun onCharacteristicRead(gatt: BluetoothGatt,
                                      characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        readCharacteristics(gatt, characteristic)
    }

    /**
     * Receive notification when characteristics value has been changed from GATT server's side.
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                         characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)
        readCharacteristics(gatt, characteristic)
    }

    /**
     * Read values from the service characteristic
     * @param gatt Bluetooth Gatt object
     * @param characteristic Modified service characteristic to read that value from
     */
    private fun readCharacteristics(gatt: BluetoothGatt,
                                    characteristic: BluetoothGattCharacteristic) {
        val messageComplete = receivedMessage.onPackageReceived(characteristic.value)
        if (messageComplete) {
            UMLog.l(UMLog.DEBUG, 698," Message received successfully")
            responseListener!!.onResponseReceived(gatt.device.address, receivedMessage, null)
            //The server should disconnect us shortly.
        }
    }


    /**
     * Find the matching service among services found by peer devices
     * @param serviceList List of all found services
     * @return Matching service
     */
    private fun findMatchingService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
        for (service in serviceList) {
            val serviceIdString = service.uuid.toString()
            if (matchesServiceUuidString(serviceIdString)) {
                return service
            }
        }
        return null
    }

    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, USTADMOBILE_BLE_SERVICE_UUID)
    }


    private fun uuidMatches(uuidString: String, vararg matches: String): Boolean {
        for (match in matches) {
            if (uuidString.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun cleanup(gatt: BluetoothGatt) {
        try {
            if (mConnected.get()) {
                gatt.disconnect()
                mConnected.set(false)
                UMLog.l(UMLog.INFO, 698, "GattClientCallback: disconnected")
            }

            if (!mClosed.get()) {
                gatt.close()
                mClosed.set(true)
                UMLog.l(UMLog.INFO, 698, "GattClientCallback: closed")
            }
        } catch (e: Exception) {
            UMLog.l(UMLog.ERROR, 698, "GattClientCallback: ERROR disconnecting")
        } finally {
            UMLog.l(UMLog.INFO, 698, "GattClientCallback: closed")
        }
    }
}
