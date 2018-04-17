package com.ustadmobile.core.db.dao;


import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.CrawlJob;

/**
 * DAO for the CrawlJob class
 *
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


    /**
     * Update the given CrawlJob to indicate that the CrawlTask should automatically queue the
     * associated DownloadJob when the CrawlTask is complete, if the crawl job is not yet finished.
     *
     * @param crawlJobId Crawl Job ID
     * @return 1 if the CrawlJob was in progress, and has now been set to queue the download on completion, 0 otherwise (e.g. the CrawlTask is already complete)
     */
    @UmQuery("UPDATE CrawlJob SET queueDownloadJobOnDone = 1 " +
            "WHERE crawlJobId = :crawlJobId " +
            "AND queueDownloadJobOnDone = 0 " +
            "AND status < " + NetworkTask.STATUS_COMPLETE_MIN)
    public abstract void updateQueueDownloadOnDoneIfNotFinished(int crawlJobId, UmResultCallback<Integer> callback);

    /**
     * Check if the given Crawl Job should automatically queue the associated DownloadJob
     *
     * @param crawlJobId Crawl Job ID of the CrawlJob
     * @return true if the Crawl Task should autoqueue the associated download, false otherwise
     */
    @UmQuery("SELECT queueDownloadJobOnDone FROM CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract boolean findQueueOnDownloadJobDoneById(int crawlJobId);



}
