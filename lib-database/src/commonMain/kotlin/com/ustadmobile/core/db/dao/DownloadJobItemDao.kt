package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.JobStatus.COMPLETE
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.lib.db.entities.*

/**
 * DAO for the DownloadJobItem class
 */
@Dao
abstract class DownloadJobItemDao {

    class DownloadJobItemToBeCreated2 {

        var cepcjUid: Long = 0

        var contentEntryUid: Long = 0

        var containerUid: Long = 0

        var fileSize: Long = 0

        var parentEntryUid: Long = 0
    }


    @Transaction
    open fun insertListAndSetIds(jobItems: List<DownloadJobItem>) {
        for (item in jobItems) {
            item.djiUid = insert(item).toInt()
        }
    }

    @Transaction
    open fun updateDownloadJobItemsProgressList(statusList: List<DownloadJobItemStatus>) {
        for (status in statusList) {
            updateDownloadJobItemProgress(status.jobItemUid, status.bytesSoFar,
                    status.totalBytes)
        }
    }

    @Query("UPDATE DownloadJobItem SET downloadedSoFar = :bytesSoFar, " + "downloadLength = :totalLength WHERE djiUid = :djiUid")
    abstract fun updateDownloadJobItemProgress(djiUid: Int, bytesSoFar: Long, totalLength: Long)

    @Update
    abstract fun update(downloadJob: DownloadJob): Int

    @Update
    abstract fun updateList(downloadJobList: List<DownloadJobItem>)

    /**
     * Insert a single DownloadJobItem
     *
     * @param jobRunItem DownloadJobItem to insert
     */
    @Insert
    abstract fun insert(jobRunItem: DownloadJobItem): Long

    @Query("UPDATE DownloadJobItem SET djiStatus = :status WHERE djiUid = :djiUid")
    abstract fun updateItemStatusInt(djiUid: Int, status: Int)

    @Transaction
    open fun updateStatus(djiUid: Int, status: Int) {
        println("DownloadJob #$djiUid updating status to $status")
        updateItemStatusInt(djiUid, status)
    }


    @Query("SELECT DownloadJobItem.* FROM " +
            "DownloadJobItem " +
            "WHERE DownloadJobItem.djiUid = :djiUid")
    abstract fun findByUid(djiUid: Int): DownloadJobItem?

    @Query("""SELECT DownloadJobItem.* FROM
        DownloadJobItem WHERE djiDjUid = :djUid AND
        djiContentEntryUid = (SELECT djRootContentEntryUid FROM DownloadJob WHERE djUid = :djUid)""")
    abstract fun findRootForDownloadJob(djUid: Int): DownloadJobItem?


    @Query("SELECT djiStatus FROM DownloadJobItem WHERE djiUid = :djiUid")
    abstract fun getLiveStatus(djiUid: Int): DoorLiveData<Int>

    @Query("SELECT * FROM DownloadJobItem")
    abstract fun findAllLive(): DoorLiveData<List<DownloadJobItem>>

    @Query("SELECT * FROM DownloadJobItem")
    abstract fun findAll(): List<DownloadJobItem>

    @Query("SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid =:djiDjUid")
    abstract fun getTotalDownloadJobItems(djiDjUid: Int): Int

    @Query("SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid =:djiDjUid")
    abstract suspend fun getTotalDownloadJobItemsAsync(djiDjUid: Int): Int

    @Query("SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid =:djiDjUid")
    abstract fun countDownloadJobItems(djiDjUid: Long): DoorLiveData<Int>

    @Query("SELECT destinationFile FROM DownloadJobItem WHERE djiUid != 0 AND djiDsiUid IN(:djiDsiUids)")
    abstract fun getDestinationFiles(djiDsiUids: List<Long>): List<String>

    @Query("""SELECT DownloadJobItem.* FROM DownloadJobItem 
            LEFT JOIN DownloadJob ON DownloadJobItem.djiDjUid = DownloadJob.djUid 
            WHERE 
             DownloadJobItem.djiContainerUid != 0 
             AND DownloadJobItem.djiStatus >= ${JobStatus.WAITING_MIN} 
             AND DownloadJobItem.djiStatus < ${JobStatus.RUNNING_MIN} 
             AND (((SELECT connectivityState FROM ConnectivityStatus) =  ${ConnectivityStatus.STATE_UNMETERED} ) 
             OR (((SELECT connectivityState FROM ConnectivityStatus) = ${ConnectivityStatus.STATE_METERED} ) 
                AND DownloadJob.meteredNetworkAllowed) 
             OR EXISTS(SELECT laContainerUid FROM LocallyAvailableContainer WHERE 
                laContainerUid = DownloadJobItem.djiContainerUid)) 
             ORDER BY DownloadJob.timeRequested, DownloadJobItem.djiUid LIMIT 6""")
    @QueryLiveTables(["DownloadJobItem", "ConnectivityStatus", "DownloadJob"])
    abstract fun findNextDownloadJobItems(): DoorLiveData<List<DownloadJobItem>>

    @Query("""SELECT DownloadJobItem.* FROM DownloadJobItem 
            LEFT JOIN DownloadJob ON DownloadJobItem.djiDjUid = DownloadJob.djUid 
            WHERE 
             DownloadJobItem.djiContainerUid != 0 
             AND DownloadJobItem.djiStatus >= ${JobStatus.WAITING_MIN} 
             AND DownloadJobItem.djiStatus < ${JobStatus.RUNNING_MIN} 
             AND (:unmeteredNetworkAvailable OR DownloadJob.meteredNetworkAllowed 
             OR EXISTS(SELECT laContainerUid FROM LocallyAvailableContainer WHERE 
                laContainerUid = DownloadJobItem.djiContainerUid))
            ORDER BY DownloadJob.timeRequested, DownloadJobItem.djiUid LIMIT :limit
    """)
    abstract fun findNextDownloadJobItems2(limit: Int, unmeteredNetworkAvailable: Boolean): List<DownloadJobItem>


    @Query("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.djiUid DESC LIMIT 1")
    abstract fun findByContentEntryUid(contentEntryUid: Long): DownloadJobItem?


    @Query("SELECT * " +
            "FROM DownloadJobItem " +
            "WHERE djiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    abstract fun findByContentEntryUid2(contentEntryUid: Long): DownloadJobItem?


    @Query("SELECT * " +
            "FROM DownloadJobItem " +
            "WHERE djiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    abstract fun findByContentEntryUidLive(contentEntryUid: Long): DoorLiveData<DownloadJobItem?>

    @Query("""SELECT Container.containerUid, Container.mimeType from DownloadJobItem 
        LEFT JOIN Container ON DownloadJobItem.djiContainerUid = Container.containerUid 
        WHERE DownloadJobItem.djiContentEntryUid = :contentEntryUid AND DownloadJobItem.djiStatus = $COMPLETE
        ORDER BY Container.cntLastModified DESC LIMIT 1
    """)
    abstract suspend fun findMostRecentContainerDownloaded(contentEntryUid: Long): ContainerUidAndMimeType?

    @Query("SELECT DownloadJobItem.* " +
            "FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiUid IN (:contentEntryUids) " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    abstract fun findByDjiUidsList(contentEntryUids: List<Long>): List<DownloadJobItem>

    @Query("SELECT djiUid AS jobItemUid, " +
            "djiContentEntryUid AS contentEntryUid, " +
            "downloadedSoFar AS bytesSoFar, " +
            "downloadLength AS totalBytes," +
            "djiStatus AS status " +
            "FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiDjUid = :downloadJobUid")
    abstract fun findStatusByDownlaodJobUid(downloadJobUid: Int): List<DownloadJobItemStatus>

    @Query("SELECT ContentEntryParentChildJoin.cepcjUid AS cepcjUid, " +
            " ContentEntryParentChildJoin.cepcjChildContentEntryUid AS contentEntryUid," +
            " Container.containerUid AS containerUid," +
            " Container.fileSize AS fileSize," +
            " ContentEntryParentChildJoin.cepcjParentContentEntryUid AS parentEntryUid " +
            " FROM ContentEntryParentChildJoin " +
            " LEFT JOIN Container ON Container.containerContentEntryUid = " +
            "   ContentEntryParentChildJoin.cepcjChildContentEntryUid" +
            "   AND Container.cntLastModified = " +
            "   (SELECT MAX(cntLastModified) FROM Container WHERE containerContentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid) " +
            "WHERE " +
            "ContentEntryParentChildJoin.cepcjParentContentEntryUid in (:parentContentEntryUids)")
    abstract fun findByParentContentEntryUuids(
            parentContentEntryUids: List<Long>): List<DownloadJobItemToBeCreated2>

    @Query("""SELECT DownloadJobItem.* FROM DownloadJobItem 
        LEFT JOIN DownloadJobItemParentChildJoin ON DownloadJobItemParentChildJoin.djiChildDjiUid = DownloadJobItem.djiUid
        WHERE DownloadJobItemParentChildJoin.djiParentDjiUid IN (:parentDownloadJobUids)
         """)
    abstract fun findByParentDownloadJobUids(parentDownloadJobUids: List<Int>): List<DownloadJobItem>

    /**
     * Runs a given function block for each level of child download job items
     */
    fun forAllChildDownloadJobItemsRecursive(parentDownloadJobUid: Int, block: (batch: List<DownloadJobItem>) -> Unit) {
        var lastParentUids = listOf(parentDownloadJobUid)
        block.invoke(listOf(findByUid(parentDownloadJobUid)?: throw IllegalArgumentException("Please provide the parent content Entry")))
        do {
            val childItems = findByParentDownloadJobUids(lastParentUids)
            block.invoke(childItems)
            lastParentUids = childItems.filter { it.djiContainerUid == 0L }. map { it.djiUid }
        } while(childItems.isNotEmpty())
    }

    /**
     * Runs a given function block for each level of child download job items
     */
    suspend fun forAllChildDownloadJobItemsRecursiveAsync(parentDownloadJobUid: Int, block: suspend (batch: List<DownloadJobItem>) -> Unit) {
        var lastParentUids = listOf(parentDownloadJobUid)
        block.invoke(listOf(findByUid(parentDownloadJobUid)?: throw IllegalArgumentException("Please provide the parent content Entry")))
        do {
            val childItems = findByParentDownloadJobUids(lastParentUids)
            block.invoke(childItems)
            lastParentUids = childItems.filter { it.djiContainerUid == 0L }. map { it.djiUid }
        } while(childItems.isNotEmpty())
    }


    @Insert
    abstract fun insertDownloadJobItemParentChildJoin(dj: DownloadJobItemParentChildJoin)

    @Transaction
    open fun updateJobItemStatusList(statusList: List<DownloadJobItemStatus>) {
        for (status in statusList) {
            updateStatus(status.jobItemUid, status.status)
        }
    }

    @Query("SELECT * FROM DownloadJobItem WHERE djiDjUid = :downloadJobUid")
    abstract fun findByDownloadJobUid(downloadJobUid: Int): List<DownloadJobItem>

    @Query("""UPDATE DownloadJobItem SET djiStatus = :status, 
        downloadedSoFar = :downloadedSoFar, 
        downloadLength = :downloadLength
        WHERE djiUid = :djiUid""")
    abstract fun updateStatusAndProgress(djiUid: Int, status: Int, downloadedSoFar: Long, downloadLength: Long)

    @Transaction
    open fun updateStatusAndProgressList(downloadJobItems: List<DownloadJobItem>) {
        downloadJobItems.forEach {
            updateStatusAndProgress(it.djiUid, it.djiStatus, it.downloadedSoFar, it.downloadLength)
        }
    }

    @Query("""SELECT djiUid, djiStatus FROM DownloadJobItem WHERE djiUid IN 
        (SELECT djiChildDjiUid FROM DownloadJobItemParentChildJoin WHERE djiParentDjiUid = :parentDjiUid)""")
    abstract fun getUidAndStatusByParentJobItem(parentDjiUid: Int): List<DownloadJobItemUidAndStatus>


    /**
     * Update the status of any waiting items.
     */
    @Query("""UPDATE DownloadJobItem SET djiStatus = :status 
            WHERE djiDjUid = :downloadJobId
            AND DownloadJobItem.djiStatus < ${JobStatus.RUNNING_MIN} 
            AND DownloadJobItem.djiUid NOT IN (:excludedJobItemUids)""")
    abstract fun updateWaitingItemStatus(downloadJobId: Int, status: Int, excludedJobItemUids: List<Int>)
}
