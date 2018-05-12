package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.CrawlJobItem;

import java.util.List;

/**
 * Created by mike on 3/3/18.
 */
public abstract class CrawJoblItemDao {

    @UmQuery("SELECT * FROM DownloadJobCrawlItem WHERE downloadJobId = :downloadJobId AND status < 10")
    public abstract CrawlJobItem findNextItemForJob(int downloadJobId);

    @UmQuery("UPDATE DownloadJobCrawlItem SET status = :status WHERE id = :id")
    public abstract void updateStatus(int id, int status);

    @UmQuery("UPDATE CrawlJobItem SET opdsEntryUuid = :opdsEntryUuid WHERE id = :id")
    public abstract void updateOpdsEntryUuid(int id, String opdsEntryUuid);

    @UmInsert
    public abstract void insert(CrawlJobItem item);

    public abstract void insertAll(List<CrawlJobItem> item);

    public CrawlJobItem findNextItemAndUpdateStatus(int downloadJobId, int status){
        CrawlJobItem item = findNextItemForJob(downloadJobId);
        if(item != null) {
            updateStatus(item.getId(), status);
            item.setStatus(status);
        }

        return item;
    }

    public abstract List<CrawlJobItem> findByCrawlJob(int crawlJobId);
}
