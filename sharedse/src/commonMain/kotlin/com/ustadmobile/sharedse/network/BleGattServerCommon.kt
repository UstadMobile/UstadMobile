package com.ustadmobile.sharedse.network

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import org.kodein.di.*

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
abstract class BleGattServerCommon(override val di: DI): DIAware {

    val networkManager: NetworkManagerBle by instance()

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
                val payload = requestReceived.payload ?: throw IllegalArgumentException("Payload has no bytes")
                val statusRequest = EntryStatusRequest.fromBytes(payload)
                val endpoint = Endpoint(statusRequest.endpointUrl)
                val endpointDb: UmAppDatabase by on(endpoint).instance(tag = TAG_DB)
                val containerDao = endpointDb.containerDao
                val responseArr = statusRequest.entryList.map { containerDao.findLocalAvailabilityByUid(it) }
                val responsePayload = bleMessageLongToBytes(responseArr)

                return BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(), responsePayload)
            }

            WIFI_GROUP_REQUEST -> {
                UMLog.l(UMLog.DEBUG, 691,
                        "BLEGattServerCommon: received wifi group request message")
                val group = networkManager.awaitWifiDirectGroupReady(5000)
                return BleMessage(WIFI_GROUP_CREATION_RESPONSE, 42.toByte(),
                        group.toBytes())
            }

            else -> {
                UMLog.l(UMLog.ERROR, 691,
                        "BLEGattServerCommon: Unknown message type")
                return null
            }
        }
    }

}