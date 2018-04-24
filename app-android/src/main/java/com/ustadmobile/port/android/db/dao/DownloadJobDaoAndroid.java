package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;

@Dao
public abstract class DownloadJobDaoAndroid extends DownloadJobDao {

    @Insert
    public abstract long insert(DownloadJob jobRun);

    @Override
    @Query("Update DownloadJob SET status = :status, timeRequested = :timeRequested WHERE downloadJobId = :id")
    public abstract void queueDownload(int id, int status, long timeRequested);

    @Override
    @Query("SELECT * FROM DownloadJob " +
            "LEFT JOIN DownloadSet on DownloadJob.downloadSetId = DownloadSet.id " +
            "WHERE status > 0 AND status <= 10 ORDER BY timeRequested LIMIT 1")
    protected abstract DownloadJobWithDownloadSet findNextDownloadJob();

    @Override
    @Query("UPDATE DownloadJob  SET status = :status WHERE downloadJobId = :jobId")
    public abstract long updateJobStatus(int jobId, int status);

    @Override
    @Query("UPDATE DownloadJob SET status = :setTo WHERE status BETWEEN :rangeFrom AND :rangeTo")
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int setTo);

    @Transaction
    public DownloadJobWithDownloadSet findNextDownloadJobAndSetStartingStatus(){
        return super.findNextDownloadJobAndSetStartingStatus();
    }

    @Override
    public UmLiveData<DownloadJob> getByIdLive(int id) {
        return new UmLiveDataAndroid<>(getByIdLiveR(id));
    }

    @Query("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract LiveData<DownloadJob> getByIdLiveR(int id);


    @Override
    @Update
    public abstract void update(DownloadJob jobRun);

    @Override
    @Query("SELECT * From DownloadJob WHERE downloadJobId = :id")
    public abstract DownloadJob findById(int id);

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
}
