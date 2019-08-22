package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode

/**
 * Created by mike on 1/31/18.
 */
@Dao
abstract class EntryStatusResponseDao {


    class EntryWithoutRecentResponse {

        var containerUid: Long = 0

        var nodeId: Int = 0
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertList(responses: List<EntryStatusResponse>): Array<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(response: EntryStatusResponse): Long

    @Query("DELETE FROM EntryStatusResponse")
    abstract suspend fun deleteAllAsync()

    @Query("SELECT * FROM EntryStatusResponse " +
            " LEFT JOIN NetworkNode ON EntryStatusResponse.erNodeId = NetworkNode.nodeId " +
            "WHERE erContainerUid = :erContainerUid AND EntryStatusResponse.available = :available ")
    abstract fun findByContainerUidAndAvailability(erContainerUid: Long, available: Boolean): List<EntryStatusResponseWithNode>

    @Query("SELECT * FROM EntryStatusResponse WHERE erContainerUid =:erContainerUid AND erNodeId=:erNodeId")
    abstract fun findByContainerUidAndNetworkNode(erContainerUid: Long, erNodeId: Long): EntryStatusResponse?

    @Query("SELECT * FROM EntryStatusResponse WHERE erContainerUid =:erContainerUid")
    abstract fun findByContainerUid(erContainerUid: Long): EntryStatusResponse?

    @Query("SELECT * FROM EntryStatusResponse WHERE erContainerUid = :erContainerUid")
    abstract fun getLiveEntryStatus(erContainerUid: Long): DoorLiveData<List<EntryStatusResponse>>

    @Query("SELECT Container.containerUid, NetworkNode.nodeId FROM Container, NetworkNode " +
            " WHERE Container.containerUid IN (:erContainerUids) " +
            " AND NetworkNode.nodeId IN (:nodeIds)  " +
            " AND NOT EXISTS(Select erId FROM EntryStatusResponse WHERE erContainerUid = Container.containerContentEntryUid" +
            " AND erNodeId = NetworkNode.nodeId AND responseTime > :sinceTime) ORDER BY NetworkNode.nodeId")
    abstract fun findEntriesWithoutRecentResponse(
            erContainerUids: List<Long>, nodeIds: List<Long>, sinceTime: Long): List<EntryWithoutRecentResponse>
}
