package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageBytesToLong
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import kotlinx.coroutines.Runnable

/**
 * This is an abstract class which is used to implement platform specific BleEntryStatus
 *
 * @see BleMessageResponseListener
 *
 * @see Runnable
 *
 * @author kileha3
 */
abstract class BleEntryStatusTask : Runnable, BleMessageResponseListener {

    /**
     * Message object which carries list of entry Ids to be checked for availability.
     */
    /**
     * Get BleMessage instance
     * @return Created BleMessage
     */
    var message: BleMessage? = null
        protected set

    /**
     * Get NetworkNode instance
     * @return Created NetworkNode
     */
    lateinit var networkNode: NetworkNode
        protected set

    lateinit var context: Any

    var status:Int = STATUS_NONE

    private var entryUidsToCheck: List<Long>? = null

    var responseListener: BleMessageResponseListener? = null

    private lateinit var managerBle: NetworkManagerBleCommon

    /**
     * Constructor which will be used when creating new instance of a task
     * @param context Application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     */
    constructor(context: Any, managerBle: NetworkManagerBleCommon, entryUidsToCheck: List<Long>,
                peerToCheck: NetworkNode) {
        this.networkNode = peerToCheck
        this.context = context
        this.entryUidsToCheck = entryUidsToCheck
        this.managerBle = managerBle
    }

    /**
     * Constructor which will be used when creating new instance for WiFi direct group creation request
     * @param context Application context
     * @param message Message to be sent to the peer device (Carried WiFi group creation request)
     * @param peerToSendMessageTo Peer to send message to
     * @param responseListener Message response listener object
     */
    constructor(context: Any, managerBle: NetworkManagerBleCommon, message: BleMessage, peerToSendMessageTo: NetworkNode,
                responseListener: BleMessageResponseListener) {
        this.networkNode = peerToSendMessageTo
        this.context = context
        this.message = message
        this.managerBle = managerBle
        this.responseListener = responseListener
    }


    /**
     * Default constructor for Mockito to spy on this class
     */
    protected constructor()

    /**
     * Set content, for test purpose
     * @param context Mocked context
     */
    fun setViewContext(context: Any) {
        this.context = context
    }

    /**
     * Set networkManagerBle for testing purpose.
     * @param managerBle NetworkManagerBleCommon object
     */
    internal fun setManagerBle(managerBle: NetworkManagerBleCommon) {
        this.managerBle = managerBle
    }

    /**
     * Set list of entry uuids , for test purpose
     * @param entryUidsToCheck List of uuids
     */
    fun setEntryUidsToCheck(entryUidsToCheck: List<Long>) {
        this.entryUidsToCheck = entryUidsToCheck
    }

    /**
     * Handle response from the entry status task
     * @param sourceDeviceAddress Server device bluetooth MAC address
     * @param response Message received as a response from the server device.
     */
    override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {

        when (response?.requestType ?: -1) {

            ENTRY_STATUS_RESPONSE -> {
                val umAppDatabase = UmAppDatabase.getInstance(context)
                val entryStatusResponseDao = umAppDatabase.entryStatusResponseDao
                val networkNodeDao = umAppDatabase.networkNodeDao

                val networkNodeId = networkNodeDao.findNodeByBluetoothAddress(sourceDeviceAddress)!!.nodeId
                val entryFileStatusResponseList = ArrayList<EntryStatusResponse>()
                val statusCheckResponse = bleMessageBytesToLong(response!!.payload!!)

                val time = getSystemTimeInMillis()
                if (entryUidsToCheck == null)
                    return

                for (entryCounter in entryUidsToCheck!!.indices) {
                    val containerUid = entryUidsToCheck!![entryCounter]

                    entryFileStatusResponseList.add(EntryStatusResponse(containerUid, time,
                            networkNodeId, statusCheckResponse[entryCounter] != 0L))

                }
                val rowCount = entryStatusResponseDao.insertList(entryFileStatusResponseList)
                if (rowCount.size == entryFileStatusResponseList.size) {
                    UMLog.l(UMLog.DEBUG, 698, rowCount.size.toString()
                            + " response(s) logged from " + sourceDeviceAddress)
                }

                managerBle.handleLocalAvailabilityResponsesReceived(entryFileStatusResponseList)
            }
        }

        responseListener?.onResponseReceived(sourceDeviceAddress, response, error)

    }

    companion object{

        /**
         * Status to indicate that the task has not been started yet
         */
        const val STATUS_NONE = 0

        /**
         * Status to indicate that the task has been queued to the list
         */
        const val STATUS_QUEUED = 1

        /**
         * Status to indicate that the task is currently running
         */
        const val STATUS_RUNNING = 2

        /**
         * Status to indicate that the task has been completed successfully
         */
        const val STATUS_COMPLETED = 3

        /**
         * Status to indicate that the task execution encountered an error
         */
        const val STATUS_FAILURE = 4

    }
}
