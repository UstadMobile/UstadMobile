package com.ustadmobile.test.sharedse.network;

/**
 * Created by mike on 3/11/18.
 */

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.annotation.UmMediumTest;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests for the download task that run without requiring a network peer to download entries from
 *
 * Test naming as per https://www.youtube.com/watch?v=wYMIadv9iF8
 */
@UmMediumTest
public class TestDownloadTaskStandalone extends TestWithNetworkService {

    protected static final String CRAWL_ROOT_ENTRY_ID = "http://umcloud1.ustadmobile.com/opds/test-crawl";

    protected static final String CRAWL_ROOT_ENTRY_ID_SLOW = "http://umcloud1.ustadmobile.com/opds/test-crawl-slow";

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException{
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }


    public CrawlJob startDownloadJob(String opdsPath, String rootEntryId, boolean autoStart) {
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<String> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                .findAllChildEntryIdsRecursive(rootEntryId);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }

        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setRootEntryUri(opdsPath);
        crawlJob.setQueueDownloadJobOnDone(autoStart);
        DownloadSet downloadJob = new DownloadSet();
        downloadJob.setDestinationDir(storageDir);

        crawlJob = UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownload(downloadJob,
                crawlJob);

        return crawlJob;
    }

    @Test
    public void givenDownloadStarted_whenCancelled_thenShouldBeDeleted() {
        CrawlJob crawlJob = startDownloadJob(UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme-slow/index.opds"),
                CRAWL_ROOT_ENTRY_ID_SLOW, true);

        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        UmLiveData<OpdsEntryStatusCache> statusCacheLive = DbManager
                .getInstance(PlatformTestUtil.getTargetContext())
                .getOpdsEntryStatusCacheDao().findByEntryIdLive(CRAWL_ROOT_ENTRY_ID_SLOW);
        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<OpdsEntryStatusCache> observer = (statusCache) -> {
            if(statusCache.getContainersDownloadedIncDescendants() > 0) {
                latch.countDown();
            }
        };
        statusCacheLive.observeForever(observer);
        try { latch.await(10, TimeUnit.SECONDS); }
        catch(InterruptedException e) {}
        statusCacheLive.removeObserver(observer);

        //now cancel the download
        UstadMobileSystemImpl.getInstance().getNetworkManager().cancelDownloadJob(
                crawlJob.getContainersDownloadJobId());

        OpdsEntryStatusCache entryStatus = DbManager.getInstance(
                PlatformTestUtil.getTargetContext()).getOpdsEntryStatusCacheDao().findByEntryId(
                        CRAWL_ROOT_ENTRY_ID_SLOW);
        Assert.assertEquals("After cancel, no entries are downloaded", 0,
                entryStatus.getContainersDownloadedIncDescendants());
        Assert.assertEquals("After cancel, no downloads are pending", 0,
                entryStatus.getContainersDownloadPendingIncAncestors());
        Assert.assertEquals("After cancel, no pending download bytes", 0,
                entryStatus.getPendingDownloadBytesSoFarIncDescendants());
        Assert.assertEquals("After cancel, no entries are active downloads", 0,
                entryStatus.getActiveDownloadsIncAncestors());
    }

    /**
     * Test downloading entries in a recursive manner
     */
    @Test
    public void givenEntriesNotDownloaded_whenDownloaded_thenShouldBeDownloaded(){
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme/index.opds");
        CrawlJob crawlJob = startDownloadJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID, true);

        CountDownLatch completeLatch = new CountDownLatch(1);
        UmLiveData<DownloadJob> downloadJobLiveData = dbManager.getDownloadJobDao()
                .getByIdLive(crawlJob.getContainersDownloadJobId());
        UmObserver<DownloadJob> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == NetworkTask.STATUS_COMPLETE) {
                completeLatch.countDown();
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);

        try { completeLatch.await(120, TimeUnit.SECONDS); }
        catch(InterruptedException e) {}

        DownloadJob completedJob =dbManager.getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());

        OpdsEntryStatusCache rootStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID);
        Assert.assertEquals("Download job status reported as completed", NetworkTask.STATUS_COMPLETE,
                completedJob.getStatus());
        Assert.assertEquals("Status shows all child entries downloaded",
                rootStatusCache.getSizeIncDescendants(), rootStatusCache.getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("4 containers downloaded in total",4,
                rootStatusCache.getContainersDownloadedIncDescendants());

        //now delete them all. We need to rerun the find query, if these entries were unknown before they
        // would not have been discovered
        final boolean[] complete = new boolean[1];
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID), true);
    }


    @Test
    public void givenDownloadStarted_whenPausedAndResumed_shouldComplete() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme-slow/index.opds");
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        CrawlJob crawlJob = startDownloadJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID, true);
        int downloadJobId = crawlJob.getContainersDownloadJobId();


        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}


        CountDownLatch latch = new CountDownLatch(1);
        UstadMobileSystemImpl.getInstance().getNetworkManager().pauseDownloadJobAsync(downloadJobId,
                (pausedOK) -> { latch.countDown(); });

        try { latch.await(12, TimeUnit.SECONDS); }
        catch(InterruptedException e) {}


        DownloadJob pausedJob = dbManager.getDownloadJobDao().findById(downloadJobId);
        Assert.assertEquals("Task status is paused",
                NetworkTask.STATUS_PAUSED, pausedJob.getStatus());

        UstadMobileSystemImpl.l(UMLog.INFO, 0,
            "TestDownloadTaskStandalone: checked task was paused, now queueing download job again");
        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);

        CountDownLatch downloadJobLatch = new CountDownLatch(1);
        UmLiveData<DownloadJob> downloadJobLiveData = dbManager.getDownloadJobDao()
                .getByIdLive(crawlJob.getContainersDownloadJobId());
        UmObserver<DownloadJob> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == NetworkTask.STATUS_COMPLETE) {
                downloadJobLatch.countDown();
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);

        try { downloadJobLatch.await(2, TimeUnit.MINUTES); }
        catch(InterruptedException e) {}

        downloadJobLiveData.removeObserver(downloadJobObserver);


        pausedJob = dbManager.getDownloadJobDao().findById(downloadJobId);
        OpdsEntryStatusCache rootStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID_SLOW);
        Assert.assertEquals("Download job status reported as completed", NetworkTask.STATUS_COMPLETE,
                pausedJob.getStatus());
        Assert.assertEquals("Status shows all child entries downloaded",
                rootStatusCache.getSizeIncDescendants(), rootStatusCache.getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("4 containers downloaded in total",4,
                rootStatusCache.getContainersDownloadedIncDescendants());

        //now delete it
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true);
    }
}
