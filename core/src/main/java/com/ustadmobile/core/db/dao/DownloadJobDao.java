package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;

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

    /**
     * Mark the given DownloadJob entity as queued
     *
     * @param id The downloadJobId of the DownloadJob entity to update
     * @param status The status to mark on the DownloadJob
     * @param timeRequested The time the download job is to be marked as queued (in ms)
     */
    @UmQuery("Update DownloadJob SET status = :status, timeRequested = :timeRequested WHERE downloadJobId = :id")
    public abstract void queueDownload(int id, int status, long timeRequested);

    /**
     * Find the next pending DownloadJob (the oldest DownloadJob that is pending)
     *
     * @return The next DownloadJob to run
     */
    @UmQuery("SELECT * FROM DownloadJob " +
            "LEFT JOIN DownloadSet on DownloadJob.downloadSetId = DownloadSet.id " +
            "WHERE (status BETWEEN " + NetworkTask.STATUS_WAITING_MIN + " AND  " +
            NetworkTask.STATUS_WAITING_MAX + ") " +
            " AND (allowMeteredDataUsage = 1 OR allowMeteredDataUsage = :connectionMetered) " +
            " ORDER BY timeRequested LIMIT 1")
    protected abstract DownloadJobWithDownloadSet findNextDownloadJob(boolean connectionMetered);

    /**
     * Update the status of the given DownloadJob
     *
     * @param jobId The DownloadJobId of the download job to update the status for
     * @param status The status to set on the given DownloadJob
     *
     */
    @UmQuery("UPDATE DownloadJob  SET status = :status WHERE downloadJobId = :jobId")
    public abstract void updateJobStatus(int jobId, int status);

    /**
     * Mark the status in bulk of DownloadJob, useful for testing purposes to cancel other downloads
     *
     * @param rangeFrom The minimum existing status of a job
     * @param rangeTo The maximum existing status of a job
     * @param setTo The status to set on a job
     */
    @UmQuery("UPDATE DownloadJob SET status = :setTo WHERE status BETWEEN :rangeFrom AND :rangeTo")
    @Deprecated
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int setTo);


    /**
     * Update all fields on the given DownloadJob
     *
     * @param job The DownloadJob to update
     */
    @UmUpdate
    public abstract void update(DownloadJob job);


    /**
     * Find a DownloadJob by the downloadJobId (primary key)
     *
     * @param id downloadJobId to search for.
     *
     * @return The DownloadJob with the given id, or null if no such DownloadJob exists
     */
    @UmQuery("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract DownloadJob findById(int id);

    /**
     * Find a DownloadJobWithDownloadSet by the downloadJobId (primary key)
     *
     * @param id downloadJobId to search for.
     * @return The DownloadJobWithDownloadSet for the given id, or null if no such DownloadJob exists
     */
    @UmQuery("SELECT * FROM DownloadJob " +
            "LEFT JOIN DownloadSet on DownloadJob.downloadSetId = DownloadSet.id " +
            "WHERE downloadJobId = :id")
    public abstract DownloadJobWithDownloadSet findByIdWithDownloadSet(int id);



    /**
     * Find the corresponding downloadSetId for the given DownloadJob
     *
     * @param downloadJobId Primary key of the given DownloadJob
     *
     * @return The primary key of the related DownloadSet
     */
    @UmQuery("SELECT downloadSetId FROM DownloadJob WHERE downloadJobId = :downloadJobId")
    public abstract int findDownloadSetId(int downloadJobId);

    /**
     * Find the next eligible DownloadJob, and if a job is remaining, set it's status to
     * NetworkTask.STATUS_STARTING
     *
     * @return The DownloadJob that has been marked as started, if any was pending
     */
    @UmTransaction
    public DownloadJobWithDownloadSet findNextDownloadJobAndSetStartingStatus(boolean connectionMetered){
        DownloadJobWithDownloadSet nextJob = findNextDownloadJob(connectionMetered);
        if(nextJob != null){
            updateJobStatus(nextJob.getDownloadJobId(), NetworkTask.STATUS_STARTING);
        }

        return nextJob;
    }

    /**
     * Get a LiveData object for the given DownloadJob id.
     *
     * @param id The downloadJobId (prmiary key) of the DownloadJob to find
     *
     * @return LiveData for the given DownloadJob
     */
    @UmQuery("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract UmLiveData<DownloadJob> getByIdLive(int id);

    /**
     * Get a LiveData as DownloadJobWithTotals, which includes totals generated by SQL SUM functions
     * for the total number of container downloads and the total size.
     *
     * @param id DownloadJobId of the DownloadJob
     *
     * @return LiveData with DownloadJobWithTotals for the given DownloadJob
     */
    @UmQuery("SELECT DownloadJob.*, " +
            " (SELECT COUNT(*) FROM DownloadJobItem WHERE DownloadJobItem.downloadJobId = DownloadJob.downloadJobId) AS numJobItems, " +
            " (SELECT SUM(DownloadJobItem.downloadLength) FROM DownloadJobItem WHERE DownloadJobItem.downloadJobId = DownloadJob.downloadJobId) AS totalDownloadSize " +
            " FROM DownloadJob Where DownloadJob.downloadJobId = :id")
    public abstract UmLiveData<DownloadJobWithTotals> findByIdWithTotals(int id);

    /**
     * Find the most recently created DownloadJob
     *
     * @return the most recently created DownloadJob
     */
    @UmQuery("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    public abstract DownloadJob findLastCreatedDownloadJob();


    /**
     * Find the last download job that was requested with a given entryId as one of the entries
     *
     * @param entryId entryId to search for - can be the root entryId, or any child entry
     * @param callback callback to call when done
     */
    public void findLastDownloadJobId(String entryId, UmCallback<Integer> callback) {
        findLastDownloadJobIdByDownloadJobItem(entryId, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer jobItemJobId) {
                if(jobItemJobId != null && jobItemJobId > 0) {
                    callback.onSuccess(jobItemJobId);
                }else{
                    findLastDownloadJobIdByCrawlJobItem(entryId, callback);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @UmQuery("SELECT DownloadJob.downloadJobId " +
            "FROM DownloadSetItem " +
            "LEFT JOIN DownloadJobItem ON DownloadSetItem.id = DownloadJobItem.downloadJobItemId " +
            "LEFT JOIN DownloadJob ON DownloadJobItem.downloadJobId = DownloadJob.downloadJobId " +
            "WHERE DownloadSetItem.entryId = :entryId " +
            "ORDER BY DownloadJob.timeRequested DESC LIMIT 1")
    public abstract void findLastDownloadJobIdByDownloadJobItem(String entryId, UmCallback<Integer> callback);

    @UmQuery("SELECT DownloadJob.downloadJobId  " +
            "FROM CrawlJobItem " +
            "LEFT JOIN OpdsEntry ON CrawlJobItem.opdsEntryUuid = OpdsEntry.uuid " +
            "LEFT JOIN CrawlJob ON CrawlJobItem.crawlJobId = CrawlJob.crawlJobId " +
            "LEFT JOIN DownloadJob on CrawlJob.containersDownloadJobId = DownloadJob.downloadJobId " +
            "WHERE OpdsEntry.entryId = :entryId " +
            "ORDER BY DownloadJob.timeRequested DESC LIMIT 1")
    public abstract void findLastDownloadJobIdByCrawlJobItem(String entryId, UmCallback<Integer> callback);

    @UmQuery("SELECT allowMeteredDataUsage FROM DownloadJob WHERE downloadJobId = :downloadJobId")
    public abstract UmLiveData<Boolean> findAllowMeteredDataUsageLive(int downloadJobId);

    @UmQuery("UPDATE DownloadJob SET allowMeteredDataUsage = :allowMeteredDataUsage WHERE downloadJobId = :downloadJobId ")
    public abstract void updateAllowMeteredDataUsage(int downloadJobId, boolean allowMeteredDataUsage,
                                                     UmCallback<Void> callback);

    /**
     * Get a list of all DownloadJob items. Used for debugging purposes.
     *
     * @return A list of all DownloadJob entity objects
     */
    @UmQuery("SELECT * From DownloadJob")
    public abstract List<DownloadJob> findAll();

}
