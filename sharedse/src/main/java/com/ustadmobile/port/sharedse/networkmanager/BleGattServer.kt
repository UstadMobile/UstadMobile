package com.ustadmobile.port.sharedse.networkmanager

import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.impl.UmAccountManager
import java.util.ArrayList
import java.util.concurrent.TimeUnit

import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong
import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_RESPONSE
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST

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

    private var networkManager: NetworkManagerBle? = null

    private var context: Any? = null

    constructor(context: Any) {
        this.context = context
    }

    /**
     * Set NetworkManagerBle instance
     * @param networkManager Instance of NetworkManagerBle
     */
    fun setNetworkManager(networkManager: NetworkManagerBle) {
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
                    entryStatusResponse.add(if (foundLocalContainerUid != null && foundLocalContainerUid != 0)
                        1L
                    else
                        0L)
                }
                return BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(),
                        bleMessageLongToBytes(entryStatusResponse))
            }

            WIFI_GROUP_REQUEST -> {
                val group = networkManager!!.awaitWifiDirectGroupReady(5000,
                        TimeUnit.MILLISECONDS)
                return BleMessage(WIFI_GROUP_CREATION_RESPONSE, 42.toByte(),
                        networkManager!!.getWifiGroupInfoAsBytes(group))
            }
            else -> return null
        }
    }

}