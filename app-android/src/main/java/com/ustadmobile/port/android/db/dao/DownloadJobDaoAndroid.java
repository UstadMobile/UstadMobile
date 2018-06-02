package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;

import java.util.concurrent.ExecutorService;

@Dao
public abstract class DownloadJobDaoAndroid extends DownloadJobDao {

    private ExecutorService executorService;

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Insert
    public abstract long insert(DownloadJob jobRun);

    @Override
    @Query("Update DownloadJob SET status = :status, timeRequested = :timeRequested WHERE downloadJobId = :id")
    public abstract void queueDownload(int id, int status, long timeRequested);

    @Override
    @Query("SELECT * FROM DownloadJob " +
            "LEFT JOIN DownloadSet on DownloadJob.downloadSetId = DownloadSet.id " +
            "WHERE (status BETWEEN " + NetworkTask.STATUS_WAITING_MIN + " AND  " +
                NetworkTask.STATUS_WAITING_MAX + ") " +
            " AND (allowMeteredDataUsage = 1 OR allowMeteredDataUsage = :connectionMetered) " +
            " ORDER BY timeRequested LIMIT 1")
    protected abstract DownloadJobWithDownloadSet findNextDownloadJob(boolean connectionMetered);

    @Override
    public long updateJobStatus(int jobId, int status) {
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, "DownloadJobDao: updateJobStatus #"
            + jobId + " status " + status);
        return updateJobStatus_Room(jobId, status);
    }

    @Query("UPDATE DownloadJob  SET status = :status WHERE downloadJobId = :jobId")
    public abstract long updateJobStatus_Room(int jobId, int status);


    @Override
    @Query("UPDATE DownloadJob SET status = :setTo WHERE status BETWEEN :rangeFrom AND :rangeTo")
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int setTo);

    @Transaction
    public DownloadJobWithDownloadSet findNextDownloadJobAndSetStartingStatus(boolean connectionMetered){
        return super.findNextDownloadJobAndSetStartingStatus(connectionMetered);
    }

    @Override
    public UmLiveData<DownloadJob> getByIdLive(int id) {
        return new UmLiveDataAndroid<>(getByIdLiveR(id));
    }

    @Query("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract LiveData<DownloadJob> getByIdLiveR(int id);


    @Override
    public void update(DownloadJob jobRun) {
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, "DownloadJobDao: update job#"
            + jobRun.getDownloadJobId() + " status " + jobRun.getStatus());
        update_Room(jobRun);
    }



    @Update
    public abstract void update_Room(DownloadJob jobRun);


    @Override
    @Query("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract DownloadJob findById(int id);

    @Override
    @Query("SELECT * FROM DownloadJob " +
            "LEFT JOIN DownloadSet on DownloadJob.downloadSetId = DownloadSet.id " +
            "WHERE downloadJobId = :id")
    public abstract DownloadJobWithDownloadSet findByIdWithDownloadSet(int id);

    @Override
    @Query("SELECT downloadSetId FROM DownloadJob WHERE downloadJobId = :downloadJobId")
    public abstract int findDownloadSetId(int downloadJobId);

    @Override
    public UmLiveData<DownloadJobWithTotals> findByIdWithTotals(int id) {
        return new UmLiveDataAndroid<>(findByIdWithTotals_Room(id));
    }

    @Query("SELECT DownloadJob.*, " +
            " (SELECT COUNT(*) FROM DownloadJobItem WHERE DownloadJobItem.downloadJobId = DownloadJob.downloadJobId) AS numJobItems, " +
            " (SELECT SUM(DownloadJobItem.downloadLength) FROM DownloadJobItem WHERE DownloadJobItem.downloadJobId = DownloadJob.downloadJobId) AS totalDownloadSize " +
            " FROM DownloadJob Where DownloadJob.downloadJobId = :id")
    public abstract LiveData<DownloadJobWithTotals> findByIdWithTotals_Room(int id);

    @Override
    @Query("SELECT * FROM DownloadJob ORDER BY timeCreated DESC LIMIT 1")
    public abstract DownloadJob findLastCreatedDownloadJob();

    public void findLastDownloadJobIdByDownloadJobItem(String entryId, UmResultCallback<Integer> callback) {
        executorService.execute(() -> callback.onDone(findLastDownloadJobIdByDownloadJobItem_Room(entryId)));
    }

    @Query("SELECT DownloadJob.downloadJobId " +
            "FROM DownloadSetItem " +
            "LEFT JOIN DownloadJobItem ON DownloadSetItem.id = DownloadJobItem.downloadJobItemId " +
            "LEFT JOIN DownloadJob ON DownloadJobItem.downloadJobId = DownloadJob.downloadJobId " +
            "WHERE DownloadSetItem.entryId = :entryId " +
            "ORDER BY DownloadJob.timeRequested DESC LIMIT 1")
    public abstract Integer findLastDownloadJobIdByDownloadJobItem_Room(String entryId);

    @Override
    public void findLastDownloadJobIdByCrawlJobItem(String entryId, UmResultCallback<Integer> callback) {
        executorService.execute(() -> callback.onDone(findLastDownloadJobIdByCrawlJobItem_Room(entryId)));
    }

    @Query("SELECT DownloadJob.downloadJobId  " +
            "FROM CrawlJobItem " +
            "LEFT JOIN OpdsEntry ON CrawlJobItem.opdsEntryUuid = OpdsEntry.uuid " +
            "LEFT JOIN CrawlJob ON CrawlJobItem.crawlJobId = CrawlJob.crawlJobId " +
            "LEFT JOIN DownloadJob on CrawlJob.containersDownloadJobId = DownloadJob.downloadJobId " +
            "WHERE OpdsEntry.entryId = :entryId " +
            "ORDER BY DownloadJob.timeRequested DESC LIMIT 1")
    public abstract Integer findLastDownloadJobIdByCrawlJobItem_Room(String entryId);

    @Override
    public UmLiveData<Boolean> findAllowMeteredDataUsageLive(int downloadJobId) {
        return new UmLiveDataAndroid<>(findAllowMeteredDataUsageLive_Room(downloadJobId));
    }

    @Query("SELECT allowMeteredDataUsage FROM DownloadJob WHERE downloadJobId = :downloadJobId")
    public abstract LiveData<Boolean> findAllowMeteredDataUsageLive_Room(int downloadJobId);

    @Override
    public void updateAllowMeteredDataUsage(int downloadJobId, boolean allowMeteredDataUsage, UmResultCallback<Void> callback) {
        executorService.execute(() -> {
            updateAllowMeteredDataUsage_Room(downloadJobId, allowMeteredDataUsage);
            if(callback != null)
                callback.onDone(null);
        });
    }

    @Query("UPDATE DownloadJob SET allowMeteredDataUsage = :allowMeteredDataUsage WHERE downloadJobId = :downloadJobId ")
    public abstract void updateAllowMeteredDataUsage_Room(int downloadJobId, boolean allowMeteredDataUsage);
}
