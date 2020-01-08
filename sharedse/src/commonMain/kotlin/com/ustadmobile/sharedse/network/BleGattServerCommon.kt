package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageBytesToLong
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream

/**
 * This is an abstract class which is used to implement platform specific BleGattServerCommon.
 * It is responsible for processing the message received from peer devices and return
 * the response to the respective peer device.
 *
 *
 *
 * **Note: Operation Flow**
 * When server device receives a message, it calls [BleGattServerCommon.handleRequest]
 * and handle it according to the request type. If the Request type will be about
 * checking entry statuses, it will check the status from the database otherwise
 * it will be for Wifi direct group creation.
 *
 * @author kileha3
 */
abstract class BleGattServerCommon() {

    lateinit var networkManager: NetworkManagerBleCommon

    lateinit var httpSessionFactory: HttpSessionFactory

    lateinit var context: Any

    constructor(aContext: Any, aNetworkManager: NetworkManagerBleCommon, aSessionFactory: HttpSessionFactory): this() {
        context = aContext
        networkManager = aNetworkManager
        httpSessionFactory = aSessionFactory
    }

    /**
     * Handle request from peer device
     * @param requestReceived Message received from the peer device
     * @param clientDeviceAddr The bluetooth device address the message was received from
     * @return Newly constructed message as a response to the peer device
     *
     * @see BleMessage
     */
    fun handleRequest(requestReceived: BleMessage, clientDeviceAddr: String): BleMessage? {
        val requestType = requestReceived.requestType

        when (requestType) {
            ENTRY_STATUS_REQUEST -> {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServerCommon: entry status request message")
                val entryStatusResponse = ArrayList<Long>()

                val containerDao = UmAccountManager.getActiveDatabase(context).containerDao
                for (containerUid in bleMessageBytesToLong(requestReceived.payload!!)) {
                    val foundLocalContainerUid = containerDao.findLocalAvailabilityByUid(containerUid)
                    entryStatusResponse.add(if (foundLocalContainerUid != 0L)
                        1L
                    else
                        0L)
                }
                return BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(),
                        bleMessageLongToBytes(entryStatusResponse))
            }

            WIFI_GROUP_REQUEST -> {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServerCommon: received wifi group request message")
                val group = networkManager.awaitWifiDirectGroupReady(5000)
                return BleMessage(WIFI_GROUP_CREATION_RESPONSE, 42.toByte(),
                        group.toBytes())
            }

            BleMessage.MESSAGE_TYPE_HTTP -> {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServerCommon: received HTTP proxy message")
                return handleHttpRequest(requestReceived, clientDeviceAddr)
            }
            else -> {
                UMLog.l(UMLog.ERROR, 691,
                        "BLEGattServerCommon: Unknown message type")
                return null
            }
        }
    }

    abstract fun handleHttpRequest(bleMessageReceived: BleMessage, clientDeviceAddr: String): BleMessage

}