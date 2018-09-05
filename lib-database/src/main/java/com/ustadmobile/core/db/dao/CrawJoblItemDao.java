package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.db.entities.CrawlJobItem;

import java.util.List;

/**
 * Created by mike on 3/3/18.
 */
@UmDao
public abstract class CrawJoblItemDao {

    @UmQuery("SELECT * FROM CrawlJobItem WHERE crawlJobId = :crawlJobId AND status < 10")
    public abstract CrawlJobItem findNextItemForJob(int crawlJobId);

    @UmQuery("UPDATE CrawlJobItem SET status = :status WHERE id = :id")
    public abstract void updateStatus(int id, int status);

    @UmQuery("UPDATE CrawlJobItem SET opdsEntryUuid = :opdsEntryUuid WHERE id = :id")
    public abstract void updateOpdsEntryUuid(int id, String opdsEntryUuid);

    @UmInsert
    public abstract void insert(CrawlJobItem item);

    @UmInsert
    public abstract void insertAll(List<CrawlJobItem> item);

    @UmTransaction
    public CrawlJobItem findNextItemAndUpdateStatus(int downloadJobId, int status){
        CrawlJobItem item = findNextItemForJob(downloadJobId);
        if(item != null) {
            updateStatus(item.getId(), status);
            item.setStatus(status);
        }

        return item;
    }

    @UmQuery("SELECT * FROM CrawlJobItem WHERE crawlJobId = :crawlJobId")
    public abstract List<CrawlJobItem> findByCrawlJob(int crawlJobId);
}
