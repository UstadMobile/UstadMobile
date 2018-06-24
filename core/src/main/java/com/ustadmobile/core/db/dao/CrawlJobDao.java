package com.ustadmobile.core.db.dao;


import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.CrawlJob;

/**
 * DAO for the CrawlJob class
 *
 */
@UmDao
public abstract class CrawlJobDao {

    @UmInsert
    public abstract long insert(CrawlJob job);

    @UmQuery("SELECT * From CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract CrawlJob findById(int crawlJobId);

    @UmQuery("UPDATE CrawlJob SET status = :status WHERE crawlJobId = :crawlJobId")
    public abstract int setStatusById(int crawlJobId, int status);

    @UmQuery("SELECT * FROM CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract UmLiveData<CrawlJob> findByIdLive(int crawlJobId);

    private static final String SQL_CRAWL_JOB_WITH_TOTALS = "SELECT CrawlJob.*, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.crawlJobId) AS numItems, " +
            " (SELECT COUNT(*) FROM CrawlJobItem WHERE CrawlJobItem.crawlJobId = CrawlJob.crawlJobId AND CrawlJobItem.status = " + NetworkTask.STATUS_COMPLETE + ") AS numItemsCompleted " +
            " FROM CrawlJob Where CrawlJob.crawlJobId = :crawlJobId";

    @UmQuery(SQL_CRAWL_JOB_WITH_TOTALS)
    public abstract UmLiveData<CrawlJobWithTotals> findWithTotalsByIdLive(int crawlJobId);

    @UmQuery(SQL_CRAWL_JOB_WITH_TOTALS)
    public abstract CrawlJobWithTotals findWithTotalsById(int crawlJobId);


    /**
     * Update the given CrawlJob to indicate that the CrawlTask should automatically queue the
     * associated DownloadJob when the CrawlTask is complete, if the crawl job is not yet finished.
     *
     * @param crawlJobId Crawl Job ID
     * @return 1 if the CrawlJob was in progress, and has now been set to queue the download on completion, 0 otherwise (e.g. the CrawlTask is already complete)
     */
    @UmQuery("UPDATE CrawlJob SET queueDownloadJobOnDone = 1 " +
            "WHERE crawlJobId = :crawlJobId " +
            "AND status < " + NetworkTask.STATUS_COMPLETE_MIN)
    public abstract void updateQueueDownloadOnDoneIfNotFinished(int crawlJobId, UmCallback<Integer> callback);

    /**
     * Check if the given Crawl Job should automatically queue the associated DownloadJob
     *
     * @param crawlJobId Crawl Job ID of the CrawlJob
     * @return true if the Crawl Task should autoqueue the associated download, false otherwise
     */
    @UmQuery("SELECT queueDownloadJobOnDone FROM CrawlJob WHERE crawlJobId = :crawlJobId")
    public abstract boolean findQueueOnDownloadJobDoneById(int crawlJobId);



}
