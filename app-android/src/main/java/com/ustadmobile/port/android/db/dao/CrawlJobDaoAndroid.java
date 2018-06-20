package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobDao;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.CrawlJob;

import java.util.concurrent.ExecutorService;

/**
 * Created by mike on 3/6/18.
 */
@Dao
public abstract class CrawlJobDaoAndroid extends CrawlJobDao {

    private ExecutorService executorService;

    public void setExecutorService(ExecutorService executorService){
        this.executorService = executorService;
    }

    @Override
    @Insert
    public abstract long insert(CrawlJob job);

    @Override
    @Query("SELECT * From CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract CrawlJob findById(int crawlJobId);

    @Override
    @Query("UPDATE CrawlJob SET status = :status WHERE crawlJobId = :crawlJobId")
    public abstract void setStatusById(int crawlJobId, int status);

    @Override
    public UmLiveData<CrawlJob> findByIdLive(int crawlJobId) {
        return new UmLiveDataAndroid<>(findByIdLive_Room(crawlJobId));
    }

    @Query("SELECT * FROM CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract LiveData<CrawlJob> findByIdLive_Room(int crawlJobId);

    @Override
    public UmLiveData<CrawlJobWithTotals> findWithTotalsByIdLive(int crawlJobId) {
        return new UmLiveDataAndroid<>(findWithTotalsByIdLive_Room(crawlJobId));
    }

    @Query("SELECT CrawlJob.*, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.crawlJobId) AS numItems, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.crawlJobId AND CrawlJobItem.status = " + NetworkTask.STATUS_COMPLETE + ") AS numItemsCompleted " +
            " FROM CrawlJob Where CrawlJob.crawlJobId = :crawlJobId")
    public abstract LiveData<CrawlJobWithTotals> findWithTotalsByIdLive_Room(int crawlJobId);



    @Query("UPDATE CrawlJob SET queueDownloadJobOnDone = 1 " +
            "WHERE crawlJobId = :crawlJobId " +
            "AND status < " + NetworkTask.STATUS_COMPLETE_MIN)
    public abstract int updateQueueDownloadOnDoneIfNotFinished(int crawlJobId);

    @Override
    public void updateQueueDownloadOnDoneIfNotFinished(int crawlJobId, UmCallback<Integer> callback) {
        executorService.execute(() -> callback.onSuccess(updateQueueDownloadOnDoneIfNotFinished(crawlJobId)));
    }

    @Override
    @Query("SELECT queueDownloadJobOnDone FROM CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract boolean findQueueOnDownloadJobDoneById(int crawlJobId);

}
