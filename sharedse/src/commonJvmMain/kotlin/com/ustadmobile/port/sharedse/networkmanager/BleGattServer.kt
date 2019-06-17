package com.ustadmobile.port.sharedse.networkmanager

import com.ustadmobile.core.impl.UmAccountManager
//import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong
//import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.BleMessage
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageBytesToLong
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import com.ustadmobile.sharedse.network.WiFiDirectGroupBle
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This is an abstract class which is used to implement platform specific BleGattServer.
 * It is responsible for processing the message received from peer devices and return
 * the response to the respective peer device.
 *
 *
 *
 * **Note: Operation Flow**
 * When server device receives a message, it calls [BleGattServer.handleRequest]
 * and handle it according to the request type. If the Request type will be about
 * checking entry statuses, it will check the status from the database otherwise
 * it will be for Wifi direct group creation.
 *
 * @author kileha3
 */
abstract class BleGattServer {

    private var networkManager: NetworkManagerBleCommon? = null

    private var context: Any? = null

    constructor(context: Any) {
        this.context = context
    }

    /**
     * Set NetworkManagerBleCommon instance
     * @param networkManager Instance of NetworkManagerBleCommon
     */
    fun setNetworkManager(networkManager: NetworkManagerBleCommon) {
        this.networkManager = networkManager
    }

    fun setContext(context: Any) {
        this.context = context
    }

    /**
     * Default constructor used by Mockito when spying this class
     */
    protected constructor() {}

    /**
     * Handle request from peer device
     * @param requestReceived Message received from the peer device
     * @return Newly constructed message as a response to the peer device
     *
     * @see BleMessage
     */
    fun handleRequest(requestReceived: BleMessage): BleMessage? {
        val requestType = requestReceived.requestType

        when (requestType) {
            ENTRY_STATUS_REQUEST -> {
                val entryStatusResponse = ArrayList<Long>()

                val containerDao = UmAccountManager.getRepositoryForActiveAccount(context!!)
                        .containerDao
                for (containerUid in bleMessageBytesToLong(requestReceived.payload!!)) {

                    val foundLocalContainerUid = containerDao.findLocalAvailabilityByUid(containerUid)
                    entryStatusResponse.add(if (foundLocalContainerUid != null && foundLocalContainerUid != 0L)
                        1L
                    else
                        0L)
                }
                return BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(),
                        bleMessageLongToBytes(entryStatusResponse))
            }

            WIFI_GROUP_REQUEST -> {
//                val group = networkManager!!.awaitWifiDirectGroupReady(5000,
//                        TimeUnit.MILLISECONDS)
                //TODO: implement this
                val group = WiFiDirectGroupBle(ssid = "fix", passphrase = "me")
                return BleMessage(WIFI_GROUP_CREATION_RESPONSE, 42.toByte(),
                        networkManager!!.getWifiGroupInfoAsBytes(group))
            }
            else -> return null
        }
    }

}