package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobSizeInfo

/**
 * DAO for the DownloadJob class
 */
@Dao
abstract class DownloadJobDao {

    @Query("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    abstract fun lastJobLive(): DoorLiveData<DownloadJob?>

    @Query("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    abstract fun lastJob(): DownloadJob?

    @Query("SELECT * From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    abstract fun activeDownloadJobs(): DoorLiveData<List<DownloadJob>>

    @Query("SELECT count(*) > 0 From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    @QueryLiveTables(["DownloadJob"])
    abstract fun anyActiveDownloadJob(): DoorLiveData<Boolean>

    /**
     * IInsert a new DownloadJob
     *
     * @param job DownloadJob entity to insert
     *
     * @return The Primary Key value assigned to the inserted object
     */
    @Insert
    abstract fun insert(job: DownloadJob): Long

    @Query("DELETE FROM DownloadJob")
    abstract suspend fun deleteAllAsync()

    /**
     * Mark the status in bulk of DownloadJob, useful for testing purposes to cancel other downloads
     *
     * @param rangeFrom The minimum existing status of a job
     * @param rangeTo The maximum existing status of a job
     * @param djStatus The status to set on a job
     */
    @Query("UPDATE DownloadJob SET djStatus = :djStatus WHERE djStatus BETWEEN :rangeFrom AND :rangeTo")
    @Deprecated("")
    abstract fun updateJobStatusByRange(rangeFrom: Int, rangeTo: Int, djStatus: Int)


    /**
     * Update all fields on the given DownloadJob
     *
     * @param job The DownloadJob to updateStateAsync
     */
    @Update
    abstract fun update(job: DownloadJob)


    /**
     * Find a DownloadJob by the downloadJobId (primary key)
     *
     * @param djUid downloadJobId to search for.
     *
     * @return The DownloadJob with the given id, or null if no such DownloadJob exists
     */
    @Query("SELECT * From DownloadJob WHERE djUid = :djUid")
    abstract fun findByUid(djUid: Int): DownloadJob?

    /**
     * Get a list of all DownloadJob items. Used for debugging purposes.
     *
     * @return A list of all DownloadJob entity objects
     */

    @Query("SELECT * FROM DownloadJob WHERE djUid = :djUid")
    abstract fun getJobLive(djUid: Int): DoorLiveData<DownloadJob?>

    @Query("SELECT * FROM DownloadJob WHERE djStatus = :jobStatus")
    abstract fun getJobsLive(jobStatus: Int): DoorLiveData<List<DownloadJob>>

    @Query("SELECT djUid FROM DownloadJob WHERE djDsUid = :djDsUid LIMIT 1")
    abstract fun getLatestDownloadJobUidForDownloadSet(djDsUid: Long): Int

    @Query("""SELECT djiDjUid FROM DownloadJobItem 
        WHERE djiContentEntryUid = :contentEntryUid AND djiStatus < ${JobStatus.CANCELED} 
        ORDER BY timeStarted DESC LIMIT 1""")
    abstract fun getLatestDownloadJobUidForContentEntryUid(contentEntryUid: Long): Int

    @Query("UPDATE DownloadJob SET djStatus = :djStatus WHERE djUid = :djUid " +
            "AND djStatus BETWEEN :statusFrom AND :statusTo")
    abstract suspend fun updateAsync(djUid: Int, djStatus: Int, statusFrom: Int, statusTo: Int)


    @Query("UPDATE DownloadJobItem SET djiStatus = :djiStatus WHERE djiDjUid = :djUid " + "AND djiStatus BETWEEN :jobStatusFrom AND :jobStatusTo")
    abstract suspend fun updateJobItems(djUid: Int, djiStatus: Int, jobStatusFrom: Int,
                                        jobStatusTo: Int)

    @Transaction
    open suspend fun updateJobAndItems(djUid: Int, djStatus: Int, activeJobItemsStatus: Int,
                                       completeJobItemStatus: Int = -1, jobStatusFrom: Int = 0,
                                       jobStatusTo: Int = JobStatus.COMPLETE_MAX) {
        updateJobItems(djUid, djStatus, 0, JobStatus.WAITING_MAX)

        if (activeJobItemsStatus != -1)
            updateJobItems(djUid, activeJobItemsStatus, JobStatus.RUNNING_MIN, JobStatus.RUNNING_MAX)

        if (completeJobItemStatus != -1)
            updateJobItems(djUid, completeJobItemStatus, JobStatus.COMPLETE_MIN, JobStatus.COMPLETE_MAX)

        updateAsync(djUid, djStatus, jobStatusFrom, jobStatusTo)
    }

    @Query("UPDATE DownloadJob SET bytesDownloadedSoFar = " +
            "(SELECT SUM(downloadedSoFar) FROM DownloadJobItem WHERE djiDjUid = :downloadJobId) " +
            "WHERE djUid = :downloadJobId")
    abstract suspend fun updateBytesDownloadedSoFarAsync(downloadJobId: Int): Int


    @Query("SELECT ContentEntry.title FROM DownloadJob " +
            "LEFT JOIN ContentEntry ON DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE DownloadJob.djUid = :downloadJobId")
    abstract suspend fun getEntryTitleByJobUidAsync(downloadJobId: Int): String?

    @Query("UPDATE DownloadJob SET djStatus = :djStatus WHERE djUid = :downloadJobId")
    abstract fun updateStatus(downloadJobId: Int, djStatus: Int)

    @Query("SELECT djUid FROM DownloadJob WHERE djRootContentEntryUid = :rootContentEntryUid")
    abstract fun findDownloadJobUidByRootContentEntryUid(rootContentEntryUid: Long): Long

    @Query("SELECT * FROM DownloadJob WHERE djRootContentEntryUid = :rootContentEntryUid")
    abstract fun findDownloadJobByRootContentEntryUid(rootContentEntryUid: Long): DownloadJob?

    @Query("UPDATE DownloadJob SET djDestinationDir = :destinationDir WHERE djUid = :djUid")
    abstract suspend fun updateDestinationDirectoryAsync(djUid: Int, destinationDir: String): Int

    @Query("SELECT djDestinationDir FROM DownloadJob WHERE djUid = :djUid")
    abstract fun getDestinationDir(djUid: Int): String?

    @Query("UPDATE DownloadJob SET meteredNetworkAllowed = :meteredNetworkAllowed WHERE djUid = :djUid")
    abstract suspend fun setMeteredConnectionAllowedByJobUidAsync(djUid: Int, meteredNetworkAllowed: Boolean): Int

    @Query("UPDATE DownloadJob SET meteredNetworkAllowed = :meteredNetworkAllowed WHERE djUid = :djUid")
    abstract fun setMeteredConnectionAllowedByJobUidSync(djUid: Int, meteredNetworkAllowed: Boolean)

    @Query("SELECT meteredNetworkAllowed FROM DownloadJob WHERE djUid = :djUid")
    abstract fun getLiveMeteredNetworkAllowed(djUid: Int): DoorLiveData<Boolean>

    @Query("SELECT COALESCE((SELECT meteredNetworkAllowed FROM DownloadJob WHERE djUid = :djUid), 1)")
    abstract suspend fun getMeteredNetworkAllowed(djUid: Int): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid = :downloadJobId) AS numEntries, " +
            "(SELECT downloadLength FROM DownloadJobItem WHERE djiDjUid = :downloadJobId AND " +
            " djiContentEntryUid = (SELECT djRootContentEntryUid FROM DownloadJob WHERE djUid = :downloadJobId)) AS totalSize" )
    abstract suspend fun getDownloadSizeInfo(downloadJobId: Int): DownloadJobSizeInfo?

    @Query("DELETE FROM DownloadJob WHERE djRootContentEntryUid = :rootContentEntryUid")
    abstract fun deleteByContentEntryUid(rootContentEntryUid: Long)

    @Query("UPDATE DownloadJob SET djStatus = :status WHERE djUid = :djiDjUid")
    abstract fun changeStatus(status: Int, djiDjUid: Int)

    @Query("""UPDATE DownloadJob SET djStatus = :status, 
            bytesDownloadedSoFar = :bytesDownloadedSoFar, 
            totalBytesToDownload = :totalBytesToDownload
            WHERE djUid = :djUid""")
    abstract fun updateStatusAndProgress(djUid: Int, status: Int, bytesDownloadedSoFar: Long,
                                         totalBytesToDownload: Long)

    fun updateStatusAndProgressList(downloadJobs: Iterable<DownloadJob>) {
        downloadJobs.forEach {
            updateStatusAndProgress(it.djUid, it.djStatus, it.bytesDownloadedSoFar,
                    it.totalBytesToDownload)
        }
    }


}
