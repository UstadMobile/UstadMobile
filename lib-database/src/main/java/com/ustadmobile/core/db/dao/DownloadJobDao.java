package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.DownloadJob;

import java.util.List;

/**
 * DAO for the DownloadJob class
 */
@UmDao
public abstract class DownloadJobDao {

    /**
     * IInsert a new DownloadJob
     *
     * @param job DownloadJob entity to insert
     *
     * @return The Primary Key value assigned to the inserted object
     */
    @UmInsert
    public abstract long insert(DownloadJob job);

    @UmQuery("DELETE FROM DownloadJob")
    public abstract void deleteAll(UmCallback<Void> callback);

    /**
     * Mark the status in bulk of DownloadJob, useful for testing purposes to cancel other downloads
     *
     * @param rangeFrom The minimum existing status of a job
     * @param rangeTo The maximum existing status of a job
     * @param djStatus The status to set on a job
     */
    @UmQuery("UPDATE DownloadJob SET djStatus = :djStatus WHERE djStatus BETWEEN :rangeFrom AND :rangeTo")
    @Deprecated
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int djStatus);


    /**
     * Update all fields on the given DownloadJob
     *
     * @param job The DownloadJob to updateState
     */
    @UmUpdate
    public abstract void update(DownloadJob job);


    /**
     * Find a DownloadJob by the downloadJobId (primary key)
     *
     * @param djUid downloadJobId to search for.
     *
     * @return The DownloadJob with the given id, or null if no such DownloadJob exists
     */
    @UmQuery("SELECT * From DownloadJob WHERE djUid = :djUid")
    public abstract DownloadJob findByUid(long djUid);

    @UmQuery("SELECT * FROM DownloadJob WHERE djDsUid = :downloadSetUid")
    public abstract List<DownloadJob> findBySetUid(long downloadSetUid);


    /**
     * Get a list of all DownloadJob items. Used for debugging purposes.
     *
     * @return A list of all DownloadJob entity objects
     */
    @UmQuery("SELECT * From DownloadJob")
    public abstract List<DownloadJob> findAll();

    @UmQuery("SELECT * FROM DownloadJob WHERE djUid = :djUid")
    public abstract UmLiveData<DownloadJob>  getJobLive(long djUid);

    @UmQuery("SELECT * FROM DownloadJob WHERE djStatus = :jobStatus")
    public abstract UmLiveData<List<DownloadJob>>  getJobsLive(int jobStatus);

    @UmQuery("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    public abstract UmLiveData<DownloadJob> getLastJobLive();

    @UmQuery("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    public abstract DownloadJob  getLastJob();

    @UmQuery("SELECT djUid FROM DownloadJob WHERE djDsUid = :djDsUid LIMIT 1")
    public abstract long getLatestDownloadJobUidForDownloadSet(long djDsUid);


    @UmQuery("UPDATE DownloadJob SET djStatus =:djStatus WHERE djUid = :djUid")
    public abstract void update(long djUid, int djStatus);


    @UmQuery("UPDATE DownloadJobItem SET djiStatus = :djiStatus WHERE djiDjUid = :djUid " +
            "AND djiStatus BETWEEN :jobStatusFrom AND :jobStatusTo")
    public abstract void updateJobItems(long djUid, int djiStatus, int jobStatusFrom,
                                        int jobStatusTo);

    @UmTransaction
    public void updateJobAndItems(long djUid, int djStatus, int activeJobItemsStatus,
                                  int completeJobItemStatus) {
        updateJobItems(djUid, djStatus, 0, JobStatus.WAITING_MAX);

        if(activeJobItemsStatus != -1)
            updateJobItems(djUid, activeJobItemsStatus, JobStatus.RUNNING_MIN, JobStatus.RUNNING_MAX);

        if(completeJobItemStatus != -1)
            updateJobItems(djUid, completeJobItemStatus, JobStatus.COMPLETE_MIN, JobStatus.COMPLETE_MAX);

        update(djUid, djStatus);
    }

    public void updateJobAndItems(long djUid, int djStatus, int activeJobItemsStatus) {
        updateJobAndItems(djUid, djStatus, activeJobItemsStatus, -1);
    }

    @UmQuery("SELECT * From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    public abstract UmLiveData<List<DownloadJob>> getActiveDownloadJobs();

    @UmQuery("SELECT count(*) > 0 From DownloadJob WHERE djStatus BETWEEN " + (JobStatus.PAUSED + 1) + " AND " +
            JobStatus.RUNNING_MAX + " ORDER BY timeCreated")
    public abstract UmLiveData<Boolean> getAnyActiveDownloadJob();

    @UmQuery("UPDATE DownloadJob SET totalBytesToDownload = " +
            "(SELECT SUM(downloadLength) FROM DownloadJobItem WHERE djiDjUid = :downloadJobId) " +
            "WHERE djUid = :downloadJobId")
    public abstract void updateTotalBytesToDownload(long downloadJobId);

    @UmQuery("UPDATE DownloadJob SET bytesDownloadedSoFar = " +
            "(SELECT SUM(downloadedSoFar) FROM DownloadJobItem WHERE djiDjUid = :downloadJobId) " +
            "WHERE djUid = :downloadJobId")
    public abstract void updateBytesDownloadedSoFar(long downloadJobId, UmCallback<Integer> callback);


    @UmQuery("SELECT ContentEntry.title FROM DownloadJob " +
            "LEFT JOIN DownloadSet ON DownloadJob.djDsUid = DownloadSet.dsUid " +
            "LEFT JOIN ContentEntry ON DownloadSet.dsRootContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE DownloadJob.djUid = :downloadJobId")
    public abstract void getEntryTitleByJobUid(long downloadJobId, UmCallback<String> callback);

    @UmQuery("UPDATE DownloadJob " +
            "SET djStatus = " + JobStatus.COMPLETE + " WHERE " +
            "djUid = :downloadJobUid AND " +
            "(SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid = :downloadJobUid " +
            "AND djiContentEntryFileUid != 0) = " +
            "(SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid = :downloadJobUid " +
            "AND djiContentEntryFileUid != 0 " +
            "AND djiStatus >= " + JobStatus.COMPLETE_MIN + ") ")
    public abstract void updateJobStatusToCompleteIfAllItemsAreCompleted(long downloadJobUid);



}
