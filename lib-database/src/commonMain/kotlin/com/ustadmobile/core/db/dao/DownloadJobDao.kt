package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.lib.db.entities.DownloadJob

/**
 * DAO for the DownloadJob class
 */
@Dao
abstract class DownloadJobDao {

    @Query("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    abstract fun lastJobLive(): UmLiveData<DownloadJob?>

    @Query("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    abstract fun lastJob(): DownloadJob?

    @Query("SELECT * From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    abstract fun activeDownloadJobs(): UmLiveData<List<DownloadJob>>

    @Query("SELECT count(*) > 0 From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    abstract fun anyActiveDownloadJob(): UmLiveData<Boolean>

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
    abstract fun getJobLive(djUid: Int): UmLiveData<DownloadJob?>

    @Query("SELECT * FROM DownloadJob WHERE djStatus = :jobStatus")
    abstract fun getJobsLive(jobStatus: Int): UmLiveData<List<DownloadJob>>

    @Query("SELECT djUid FROM DownloadJob WHERE djDsUid = :djDsUid LIMIT 1")
    abstract fun getLatestDownloadJobUidForDownloadSet(djDsUid: Long): Int?

    @Query("SELECT djiDjUid FROM DownloadJobItem WHERE djiContentEntryUid = :contentEntryUid " + "ORDER BY timeStarted DESC LIMIT 1")
    abstract fun getLatestDownloadJobUidForContentEntryUid(contentEntryUid: Long): Int?


    @Query("UPDATE DownloadJob SET djStatus =:djStatus WHERE djUid = :djUid")
    abstract fun update(djUid: Int, djStatus: Int)


    @Query("UPDATE DownloadJobItem SET djiStatus = :djiStatus WHERE djiDjUid = :djUid " + "AND djiStatus BETWEEN :jobStatusFrom AND :jobStatusTo")
    abstract fun updateJobItems(djUid: Int, djiStatus: Int, jobStatusFrom: Int,
                                jobStatusTo: Int)

    @Transaction
    fun updateJobAndItems(djUid: Int, djStatus: Int, activeJobItemsStatus: Int,
                          completeJobItemStatus: Int) {
        updateJobItems(djUid, djStatus, 0, JobStatus.WAITING_MAX)

        if (activeJobItemsStatus != -1)
            updateJobItems(djUid, activeJobItemsStatus, JobStatus.RUNNING_MIN, JobStatus.RUNNING_MAX)

        if (completeJobItemStatus != -1)
            updateJobItems(djUid, completeJobItemStatus, JobStatus.COMPLETE_MIN, JobStatus.COMPLETE_MAX)

        update(djUid, djStatus)
    }

    fun updateJobAndItems(djUid: Int, djStatus: Int, activeJobItemsStatus: Int) {
        updateJobAndItems(djUid, djStatus, activeJobItemsStatus, -1)
    }

    @Query("UPDATE DownloadJob SET bytesDownloadedSoFar = " +
            "(SELECT SUM(downloadedSoFar) FROM DownloadJobItem WHERE djiDjUid = :downloadJobId) " +
            "WHERE djUid = :downloadJobId")
    abstract suspend fun updateBytesDownloadedSoFarAsync(downloadJobId: Int): Int


    @Query("SELECT ContentEntry.title FROM DownloadJob " +
            "LEFT JOIN ContentEntry ON DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE DownloadJob.djUid = :downloadJobId")
    abstract suspend fun getEntryTitleByJobUidAsync(downloadJobId: Long): String?

    @Query("UPDATE DownloadJob SET djStatus = :djStatus WHERE djUid = :downloadJobId")
    abstract fun updateStatus(downloadJobId: Int, djStatus: Int)

    @Query("SELECT djUid FROM DownloadJob WHERE djRootContentEntryUid = :rootContentEntryUid")
    abstract fun findDownloadJobUidByRootContentEntryUid(rootContentEntryUid: Long): Long

    @Query("UPDATE DownloadJob SET djDestinationDir = :destinationDir WHERE djUid = :djUid")
    abstract suspend fun updateDestinationDirectoryAsync(djUid: Int, destinationDir: String): Int

    @Query("SELECT djDestinationDir FROM DownloadJob WHERE djUid = :djUid")
    abstract fun getDestinationDir(djUid: Int): String?


    @Query("UPDATE DownloadJob SET meteredNetworkAllowed = :meteredNetworkAllowed WHERE djUid = :djUid")
    abstract suspend fun setMeteredConnectionAllowedByJobUidAsync(djUid: Int, meteredNetworkAllowed: Boolean): Int

    @Query("UPDATE DownloadJob SET meteredNetworkAllowed = :meteredNetworkAllowed WHERE djUid = :djUid")
    abstract fun setMeteredConnectionAllowedByJobUidSync(djUid: Int, meteredNetworkAllowed: Boolean)

    @Query("SELECT meteredNetworkAllowed FROM DownloadJob WHERE djUid = :djUid")
    abstract fun getLiveMeteredNetworkAllowed(djUid: Int): UmLiveData<Boolean>

    @Transaction
    fun cleanupUnused(downloadJobUid: Int) {
        deleteUnusedDownloadJobItems(downloadJobUid)
        deleteUnusedDownloadJob(downloadJobUid)
    }

    @Query("DELETE FROM DownloadJobItem " +
            "WHERE djiDjUid = :downloadJobUid " +
            "AND (djiStatus = " + JobStatus.NOT_QUEUED +
            " OR djiStatus = " + JobStatus.CANCELED + ")")
    abstract fun deleteUnusedDownloadJobItems(downloadJobUid: Int)

    @Query("DELETE FROM DownloadJob WHERE djUid = :downloadJobUid AND djStatus = "
            + JobStatus.NOT_QUEUED + " OR djStatus = " + JobStatus.CANCELED)
    abstract fun deleteUnusedDownloadJob(downloadJobUid: Int)

}
