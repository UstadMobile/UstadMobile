package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.lib.db.entities.NetworkNode

/**
 * Created by mike on 1/30/18.
 */
@Dao
abstract class NetworkNodeDao {

    @Query("SELECT * FROM NetworkNode")
    abstract fun all(): List<NetworkNode>

    @Query("SELECT * From NetworkNode WHERE ipAddress = :ipAddress")
    abstract fun findNodeByIpAddress(ipAddress: String): NetworkNode?

    @Query("SELECT * From NetworkNode WHERE nodeId = :nodeId")
    abstract fun findNodeById(nodeId: Long): NetworkNode?

    @Query("Select * From NetworkNode Where ((ipAddress = :ipAddress AND ipAddress IS NOT NULL) OR (wifiDirectMacAddress = :wifiDirectMacAddress AND wifiDirectMacAddress IS NOT NULL))")
    abstract fun findNodeByIpOrWifiDirectMacAddress(ipAddress: String, wifiDirectMacAddress: String): NetworkNode?

    @Query(findByBluetoothAddrSql)
    abstract fun findNodeByBluetoothAddress(bluetoothAddress: String): NetworkNode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(node: NetworkNode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAsync(node: NetworkNode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertList(nodeList: List<NetworkNode>): Array<Long>

    @Update
    abstract fun update(node: NetworkNode)

    @Query("DELETE FROM NetworkNode WHERE bluetoothMacAddress = :bluetoothAddress")
    abstract fun deleteByBluetoothAddress(bluetoothAddress: String)

    @Query("DELETE FROM NetworkNode")
    abstract suspend fun deleteAllAsync()

    @Query("UPDATE NetworkNode SET numFailureCount = numFailureCount + 1 WHERE nodeId = :nodeId")
    abstract suspend fun updateRetryCountAsync(nodeId: Long)

    @Query("Select * From NetworkNode WHERE lastUpdateTimeStamp >= :lastUpdatedTime" + " AND numFailureCount <= :maxNumFailure")
    abstract fun findAllActiveNodes(lastUpdatedTime: Long, maxNumFailure: Int): List<NetworkNode>

    @Query("UPDATE NetworkNode set lastUpdateTimeStamp = :lastUpdateTimeStamp, numFailureCount = 0 " + "WHERE bluetoothMacAddress = :bluetoothAddress")
    abstract suspend fun updateLastSeenAsync(bluetoothAddress: String, lastUpdateTimeStamp: Long): Int

    @Query("SELECT NetworkNode.* FROM NetworkNode " +
            "LEFT JOIN EntryStatusResponse ON NetworkNode.nodeId = EntryStatusResponse.erNodeId " +
            "WHERE EntryStatusResponse.erContainerUid = :containerUid " +
            "AND EntryStatusResponse.available " +
            "AND NetworkNode.lastUpdateTimeStamp > :minLastSeenTimestamp " +
            "AND (Select COUNT(*) FROM DownloadJobItemHistory " +
            "WHERE DownloadJobItemHistory.networkNode = NetworkNode.nodeId " +
            "AND NOT successful AND startTime > :maxFailuresFromTimestamp) < :maxFailuresInPeriod " +
            "LIMIT 1")
    abstract fun findLocalActiveNodeByContainerUid(containerUid: Long,
                                                   minLastSeenTimestamp: Long,
                                                   maxFailuresInPeriod: Int,
                                                   maxFailuresFromTimestamp: Long): NetworkNode?

    @Query("DELETE FROM NetworkNode WHERE NetworkNode.lastUpdateTimeStamp < :minLastSeenTimestamp " + "OR NetworkNode.numFailureCount >= :maxFailuresInPeriod")
    abstract fun deleteOldAndBadNode(minLastSeenTimestamp: Long,
                                     maxFailuresInPeriod: Int)


    @Query("UPDATE NetworkNode SET groupSsid = :groupSsid, endpointUrl = :endpointUrl  WHERE nodeId = :nodeId")
    abstract fun updateNetworkNodeGroupSsid(nodeId: Long, groupSsid: String, endpointUrl: String)

    @Query("SELECT endpointUrl FROM NetworkNode WHERE groupSsid = :ssid")
    abstract fun getEndpointUrlByGroupSsid(ssid: String): String?

    @Transaction
    open suspend fun updateNodeLastSeen(knownNodes: MutableMap<String, Long>) {
        val nodeIterator = knownNodes.entries.iterator()
        while (nodeIterator.hasNext()) {
            val nodeUpdates = nodeIterator.next()
            updateLastSeenAsync(nodeUpdates.key, nodeUpdates.value)
            nodeIterator.remove()

        }
    }

    companion object {

        const val findByBluetoothAddrSql = "SELECT * from NetworkNode WHERE bluetoothMacAddress = :bluetoothAddress"
    }

}
