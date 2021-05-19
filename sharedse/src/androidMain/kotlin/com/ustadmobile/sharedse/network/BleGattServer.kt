package com.ustadmobile.sharedse.network

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import io.github.aakira.napier.Napier
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.port.sharedse.util.AsyncServiceManager
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.BLE_CHARACTERISTICS
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * BleGattServer on Android contains the Gatt Server and an AdvertisingManager. Advertising must
 * be started before discovery. In production the BleGattServer is instantiated by the dependency
 * injection startup. After advertising has started it will tell the networkmanager to start discovery.
 *
 * This class handle all the GATT server device's Bluetooth Low Energy callback
 *
 * Operation flow:
 *
 * When a client wants to send a BleMessage it will make one characteristicWriteRequest for each
 * packet in the outgoing request. The server will store the packet. Once all packets are received
 * the server will prepare a response BleMessage and store it. The client will then make a
 * characteristicReadRequest for each packet in the response.
 *
 * @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class BleGattServer
/**
 * Constructor which will be used when creating new instance of BleGattServer
 * @param context Application context
 * BluetoothManager instance.
 */
(val context: Context, di: DI) : BleGattServerCommon(di) {

    class PendingReplyMessage(val destAddr: String, val characteristicUuid: UUID,
                              val message: BleMessage, mtu: Int,
                              val currentPacket: AtomicInteger = AtomicInteger(0)) {
        val packetsToSend = message.getPackets(mtu - ATT_HEADER_SIZE)

        val packetNum = AtomicInteger(0)
    }

    val pendingReplies: MutableList<PendingReplyMessage> = CopyOnWriteArrayList()

    private val parcelServiceUuid = ParcelUuid(UUID.fromString(NetworkManagerBleCommon.USTADMOBILE_BLE_SERVICE_UUID))

    /**
     * Get instance of a BluetoothGattServer
     * @return Instance of BluetoothGattServer
     */
    @set:VisibleForTesting
    var gattServer: BluetoothGattServer? = null

    private val messageAssembler = BleMessageAssembler()

    private val delayedExecutor = Executors.newSingleThreadScheduledExecutor()

    inner class BleGattServerCallback: BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int,
                                                 characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            Napier.d(" onCharacteristicReadRequest from ${device.address}")
            if(characteristic.uuid in CHARACTERISTIC_UUIDS) {
                val clientMessage = pendingReplies.firstOrNull { it.destAddr == device.address
                        && it.characteristicUuid == characteristic.uuid }
                Napier.d(" readRequest from ${device.address}")
                if(clientMessage != null) {
                    val packetToSend = clientMessage.packetsToSend[clientMessage.packetNum.get()]
                    val responseSent = gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            0, packetToSend) ?: false
                    if(responseSent) {
                        Napier.d("SendResponse #${clientMessage.packetNum.get()} to ${device.address}")
                        val packetSent = clientMessage.packetNum.incrementAndGet()
                        if(packetSent == clientMessage.packetsToSend.size) {
                            Napier.d("Response ${clientMessage.message.messageId} completely sent")
                            pendingReplies.remove(clientMessage)
                        }
                    }else {
                        Napier.e("SendResponse not accepted")
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                                0, null)
                    }
                }else {
                    Napier.e("Read request: no response to send")
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                            0, null)
                }
            }else {
                Napier.e("Read request: wrong characteristic")
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE,
                        0, null)
            }
        }


        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int,
                                                  characteristic: BluetoothGattCharacteristic,
                                                  preparedWrite: Boolean, responseNeeded: Boolean,
                                                  offset: Int, value: ByteArray) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value)

            if (characteristic.uuid in CHARACTERISTIC_UUIDS) {
                Napier.d(" write permission requested by ${device.address}")
                if(responseNeeded) {
                    val granted = gattServer?.sendResponse(device, requestId,
                            BluetoothGatt.GATT_SUCCESS, 0, null)
                    Napier.d("Sent response to ${device.address}. accepted=$granted")
                }else {
                    Napier.d("Response not required by ${device.address}")
                }

                val messageReceived = messageAssembler.handleIncomingPacket(
                        device.address, value)
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServer: Received all packets from " + device.address + " "
                                + (messageReceived != null))
                if (messageReceived != null) {
                    val currentMtuSize = messageReceived.mtu

                    UMLog.l(UMLog.DEBUG, 691,
                            "BLEGattServer: Request received with default MTU size of $currentMtuSize")
                    //remove any stuck or pending replies that might be there
                    pendingReplies.removeAll { it.destAddr == device.address
                            && it.characteristicUuid == characteristic.uuid }
                    handleRequest(messageReceived, device.address)?.also {reply ->
                        pendingReplies.add(PendingReplyMessage(device.address, characteristic.uuid,
                                reply, currentMtuSize))
                    }

                    UMLog.l(UMLog.DEBUG, 691,
                            "BLEGattServer: Prepare response to send back to " + device.address)
                }


            }else {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServer: wrong clientToServerCharacteristic: ${characteristic.uuid}")
            }
        }
    }

    inner class AdvertisingServiceManager: AsyncServiceManager(STATE_STOPPED, { runnable, delay -> delayedExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS) }) {
        override fun start() {
            if (canDeviceAdvertise()) {
                gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
                UMLog.l(UMLog.DEBUG, 689,
                        "Starting BLE advertising service")
                val service = BluetoothGattService(parcelServiceUuid.uuid,
                        BluetoothGattService.SERVICE_TYPE_PRIMARY)


                val androidCharacteristics = BLE_CHARACTERISTICS.map {charUuidStr ->
                    val charUuid = ParcelUuid(UUID.fromString(charUuidStr)).uuid
                    BluetoothGattCharacteristic(charUuid,
                            BluetoothGattCharacteristic.PROPERTY_WRITE
                                    or BluetoothGattCharacteristic.PROPERTY_READ,
                            BluetoothGattCharacteristic.PERMISSION_WRITE or
                                    BluetoothGattCharacteristic.PERMISSION_READ)
                }

                androidCharacteristics.forEach {
                    service.addCharacteristic(it)
                }


                val bleServiceAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

                gattServer?.addService(service)

                val settings = AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                        .setConnectable(true)
                        .setTimeout(0)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                        .build()

                val data = AdvertiseData.Builder()
                        .addServiceUuid(parcelServiceUuid).build()

                bleServiceAdvertiser.startAdvertising(settings, data,
                        object : AdvertiseCallback() {
                            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                                super.onStartSuccess(settingsInEffect)
                                notifyStateChanged(STATE_STARTED)
                                UMLog.l(UMLog.DEBUG, 689,
                                        "Service advertised successfully")

                                //Now that advertising has started, tell the networkmanager to start discovery
                                networkManager.checkP2PBleServices(bleAdvertisingStartTime = System.currentTimeMillis())
                            }

                            override fun onStartFailure(errorCode: Int) {
                                super.onStartFailure(errorCode)
                                notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
                                UMLog.l(UMLog.ERROR, 689,
                                        "Service could'nt start, with error code $errorCode")
                            }
                        })
            } else {
                notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
            }
        }

        override fun stop() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    //val mGattServer = (gattServerAndroid as BleGattServer).gattServer
                    gattServer?.clearServices()
                    gattServer?.close()
                }
                gattServer = null
            } catch (e: Exception) {
                //maybe because bluetooth is actually off?
                UMLog.l(UMLog.ERROR, 689,
                        "Exception trying to stop gatt server", e)
            }

            notifyStateChanged(STATE_STOPPED)
        }
    }

    private val gattServerCallback = BleGattServerCallback()


    private val serviceManager = AdvertisingServiceManager()

    val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    init {
        UMLog.l(UMLog.DEBUG, 691, "BLEGattServer: Opened")
        GlobalScope.launch(Dispatchers.Main) {
            delay(5000)
            serviceManager.start()
        }
    }

    fun canDeviceAdvertise(): Boolean {
        return Build.VERSION.SDK_INT > NetworkManagerBle.BLE_ADVERTISE_MIN_SDK_VERSION &&
                bluetoothManager.adapter != null && bluetoothManager.adapter.isMultipleAdvertisementSupported
    }

    companion object {

        /**
         * Each BLE clientToServerCharacteristic notification etc. packet has a 1 byte Op-Code and a 2 byte
         * Attribute Handle. When we send a clientToServerCharacteristic the maximum amount of data we can send
         * is the MTU minus this overhead (e.g. MTU - 3). See "ATT MTU" here:
         *  https://punchthrough.com/maximizing-ble-throughput-part-2-use-larger-att-mtu-2/
         */
        const val ATT_HEADER_SIZE = 3

        val CHARACTERISTIC_UUIDS = NetworkManagerBleCommon.BLE_CHARACTERISTICS.map { UUID.fromString(it) }

        const val LOG_TAG = "BleGattServer"

    }
}
