package com.ustadmobile.core.db.dao;


import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.CrawlJob;

/**
 * Created by mike on 3/6/18.
 */

public abstract class CrawlJobDao {

    @UmInsert
    public abstract long insert(CrawlJob job);

    @UmQuery("SELECT * FROM CrawlJob where crawlJobId = :crawlJobId")
    public abstract CrawlJob findById(int crawlJobId);

    @UmQuery("UPDATE CrawlJob set status = :status WHERE crawlJobId = :crawlJobId")
    public abstract void setStatusById(int crawlJobId, int status);

    @UmQuery("SELECT * From CrawlJob where crawlJobId = :crawlJobId")
    public abstract UmLiveData<CrawlJob> findByIdLive(int crawlJobId);

    @UmQuery("SELECT CrawlJob.*, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.id) AS numItems, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.id AND CrawlJobItem.status = " + NetworkTask.STATUS_COMPLETE +
            " FROM CrawlJob Where CrawlJob.id = :crawlJobId")
    public abstract UmLiveData<CrawlJobWithTotals> findWithTotalsByIdLive(int crawlJobId);




}
