package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ScrapeQueueItemDao implements BaseDao<ScrapeQueueItem> {

    public static final int STATUS_PENDING = 1;

    public static final int STATUS_RUNNING = 2;

    public static final int STATUS_DONE = 3;

    public static final int STATUS_FAILED = 4;

    public ScrapeQueueItem getNextItemAndSetStatus(int runId, int itemType) {
        ScrapeQueueItem nextItem = findNextItem(STATUS_PENDING, runId, itemType);
        if (nextItem != null) {
            updateSetStatusById(nextItem.getSqiUid(), STATUS_RUNNING);
        }

        return nextItem;
    }

    @UmQuery("SELECT * FROM ScrapeQueueItem")
    public abstract List<ScrapeQueueItem> findAll();


    @UmQuery("SELECT * FROM ScrapeQueueItem WHERE status = :status AND runId = :runId AND itemType = :itemType LIMIT 1")
    public abstract ScrapeQueueItem findNextItem(int status, int runId, int itemType);

    @UmQuery("UPDATE ScrapeQueueItem SET status = :status WHERE sqiUid = :uid")
    public abstract void updateSetStatusById(int uid, int status);

    @UmQuery("SELECT * from ScrapeQueueItem WHERE runId = :runId AND scrapeUrl = :indexUrl LIMIT 1")
    public abstract ScrapeQueueItem getExistingQueueItem(int runId, String indexUrl);

    @UmQuery("UPDATE ScrapeQueueItem SET timeStarted = :timeStarted WHERE sqiUid = :uid")
    public abstract void setTimeStarted(int uid, long timeStarted);

    @UmQuery("UPDATE ScrapeQueueItem SET timeFinished = :timeFinished WHERE sqiUid = :uid")
    public abstract void setTimeFinished(int uid, long timeFinished);

}
