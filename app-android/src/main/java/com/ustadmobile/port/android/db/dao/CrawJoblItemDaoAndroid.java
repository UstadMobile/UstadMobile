package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.ustadmobile.core.db.dao.CrawJoblItemDao;
import com.ustadmobile.lib.db.entities.CrawlJobItem;

import java.util.List;

/**
 * Created by mike on 3/4/18.
 */
@Dao
public abstract class CrawJoblItemDaoAndroid extends CrawJoblItemDao {

    @Override
    @Query("SELECT * FROM CrawlJobItem WHERE crawlJobId = :crawlJobId")
    public abstract List<CrawlJobItem> findByCrawlJob(int crawlJobId);

    @Override
    @Query("SELECT * FROM CrawlJobItem WHERE crawlJobId = :crawlJobId AND status < 10")
    public abstract CrawlJobItem findNextItemForJob(int crawlJobId);

    @Override
    @Query("UPDATE CrawlJobItem SET status = :status WHERE id = :id")
    public abstract void updateStatus(int id, int status);

    @Override
    @Insert
    public abstract void insert(CrawlJobItem item);

    @Override
    @Transaction
    public CrawlJobItem findNextItemAndUpdateStatus(int downloadJobId, int status) {
        return super.findNextItemAndUpdateStatus(downloadJobId, status);
    }

    @Override
    @Insert
    public abstract void insertAll(List<CrawlJobItem> item);
}
