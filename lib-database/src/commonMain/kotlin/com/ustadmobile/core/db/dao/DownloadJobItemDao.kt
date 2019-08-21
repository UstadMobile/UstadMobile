package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
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
    open fun updateDownloadJobItemsProgress(statusList: List<DownloadJobItemStatus>) {
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

    @Query("DELETE FROM DownloadJobItem")
    abstract suspend fun deleteAllAsync()

    /**
     * Update the main status fields for the given DownloadJobitem
     *
     * @param djiUid DownloadJobItemId to updateStateAsync (primary key)
     * @param djiStatus status property to set
     * @param downloadedSoFar downloadedSoFar property to set
     * @param downloadLength downloadLength property to set
     * @param currentSpeed currentSpeed property to set
     */
    @Query("Update DownloadJobItem SET " +
            "djiStatus = :djiStatus, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE djiUid = :djiUid")
    abstract fun updateDownloadJobItemStatusIm(djiUid: Long, djiStatus: Int,
                                                         downloadedSoFar: Long, downloadLength: Long,
                                                         currentSpeed: Long)

    @Transaction
    open fun updateDownloadJobItemStatus(djiUid: Long, djiStatus: Int,
                                    downloadedSoFar: Long, downloadLength: Long,
                                    currentSpeed: Long) {
        println("updateDownloadJobItemStatus $djiUid -> $djiStatus")
        updateDownloadJobItemStatusIm(djiUid, djiStatus, downloadedSoFar, downloadLength,
                currentSpeed)
    }

    @Query("UPDATE DownloadJobItem SET downloadedSoFar = :downloadedSoFar, " +
            "currentSpeed = :currentSpeed " +
            "WHERE djiUid = :djiUid")
    abstract fun updateDownloadJobItemProgress(djiUid: Long, downloadedSoFar: Long,
                                               currentSpeed: Long)


    @Query("UPDATE DownloadJobItem SET djiStatus = :status WHERE djiUid = :djiUid")
    abstract fun updateItemStatusInt(djiUid: Int, status: Int)

    @Transaction
    open fun updateStatus(djiUid: Int, status: Int) {
        println("DownloadJob #$djiUid updating status to $status")
        updateItemStatusInt(djiUid, status)
    }


    @Query("UPDATE DownloadJobItem SET numAttempts = numAttempts + 1 WHERE djiUid = :djiUid")
    abstract fun incrementNumAttempts(djiUid: Int)

    @Query("SELECT DownloadJobItem.* FROM " +
            "DownloadJobItem " +
            "WHERE DownloadJobItem.djiUid = :djiUid")
    abstract fun findByUid(djiUid: Int): DownloadJobItem?

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

    @Query("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadJob ON DownloadJobItem.djiDjUid = DownloadJob.djUid " +
            "WHERE " +
            " DownloadJobItem.djiContainerUid != 0 " +
            " AND DownloadJobItem.djiStatus >= " + JobStatus.WAITING_MIN +
            " AND DownloadJobItem.djiStatus < " + JobStatus.RUNNING_MIN +
            " AND (((SELECT connectivityState FROM ConnectivityStatus) =  " + ConnectivityStatus.STATE_UNMETERED + ") " +
            " OR ((SELECT connectivityState FROM ConnectivityStatus) = " + ConnectivityStatus.STATE_METERED + ") " +
            " AND DownloadJob.meteredNetworkAllowed) " +
            "LIMIT 1")
    abstract fun findNextDownloadJobItems(): DoorLiveData<List<DownloadJobItem>>

    @Query("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
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
            "   AND Container.lastModified = " +
            "   (SELECT MAX(lastModified) FROM Container WHERE containerContentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid) " +
            "WHERE " +
            "ContentEntryParentChildJoin.cepcjParentContentEntryUid in (:parentContentEntryUids)")
    abstract fun findByParentContentEntryUuids(
            parentContentEntryUids: List<Long>): List<DownloadJobItemToBeCreated2>

    @Insert
    abstract fun insertDownloadJobItemParentChildJoin(dj: DownloadJobItemParentChildJoin)

    @Transaction
    open fun updateJobItemStatusList(statusList: List<DownloadJobItemStatus>) {
        for (status in statusList) {
            updateStatus(status.jobItemUid, status.status)
        }
    }

}
