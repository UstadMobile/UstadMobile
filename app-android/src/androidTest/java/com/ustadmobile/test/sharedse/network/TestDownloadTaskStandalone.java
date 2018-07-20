 package com.ustadmobile.test.sharedse.network;


import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadTaskListener;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.port.sharedse.networkmanager.DownloadTask;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.annotation.UmMediumTest;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.core.networkmanager.NetworkTask.STATUS_COMPLETE;
import static com.ustadmobile.core.networkmanager.NetworkTask.STATUS_WAITING_FOR_CONNECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;

/**
 * Tests for the download task that run without requiring a network peer to download entries from
 *
 * Test naming as per https://www.youtube.com/watch?v=wYMIadv9iF8
 */
@UmMediumTest
public class TestDownloadTaskStandalone extends TestWithNetworkService {

    @SuppressWarnings("WeakerAccess")
    public static final String CRAWL_ROOT_ENTRY_ID = "http://umcloud1.ustadmobile.com/opds/test-crawl";

    @SuppressWarnings("WeakerAccess")
    public static final String CRAWL_ROOT_ENTRY_ID_SLOW = "http://umcloud1.ustadmobile.com/opds/test-crawl-slow";

    @SuppressWarnings("WeakerAccess")
    public static final String OPDS_PATH_SPEED_LIMITED = "com/ustadmobile/test/sharedse/crawlme-slow/index.opds";

    @SuppressWarnings("WeakerAccess")
    public static final String OPDS_PATH_404 =
            "com/ustadmobile/test/sharedse/testfeeds/entry-not-found/index.opds";

    @SuppressWarnings("WeakerAccess")
    public static final String CRAWL_ROOT_ENTRY_ID_404 =
            "http://umcloud1.ustadmobile.com/opds/test-crawl/entry-not-found/doesnotexist";

    @SuppressWarnings("WeakerAccess")
    public static final int JOB_CHANGE_WAIT_TIME = 1000;

    @SuppressWarnings("WeakerAccess")
    public static final int CRAWL_JOB_TIMEOUT = 5000;

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException{
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }


    @SuppressWarnings("EmptyCatchBlock")
    public static CrawlJob runCrawlJob(String opdsPath, String rootEntryId,
                                       boolean allowMeteredNetworks, long crawlTimeout,
                                       boolean queueDownloadJobOnDone) {
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());

        List<String> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                .findAllChildEntryIdsRecursive(rootEntryId);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }

        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setRootEntryUri(opdsPath);
        crawlJob.setQueueDownloadJobOnDone(queueDownloadJobOnDone);
        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setDestinationDir(storageDir);

        crawlJob = UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownload(downloadSet,
                crawlJob, allowMeteredNetworks);
        UmLiveData<CrawlJob> crawlJobLiveData = dbManager.getCrawlJobDao().findByIdLive(
                crawlJob.getCrawlJobId());
        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<CrawlJob> observer = (crawlJobValue) -> {
            if(crawlJobValue != null && crawlJobValue.getStatus() == STATUS_COMPLETE)
                latch.countDown();
        };
        crawlJobLiveData.observeForever(observer);
        try {latch.await(crawlTimeout, TimeUnit.MILLISECONDS); }
        catch(InterruptedException e) {}

        crawlJobLiveData.removeObserver(observer);

        return crawlJob;
    }

    public static CrawlJob runCrawlJob(String opdsPath, String rootEntryId,
                                       boolean allowMeteredNetworks, long crawlTimeout){
        return runCrawlJob(opdsPath, rootEntryId, allowMeteredNetworks, crawlTimeout, false);
    }

    @SuppressWarnings("WeakerAccess")
    public static DownloadTask startDownload(int downloadJobId, DownloadTaskListener listener) {
        DownloadJobWithDownloadSet job = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findByIdWithDownloadSet(downloadJobId);
        DownloadTask task = new DownloadTask(job,
                (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager(), listener,
                Executors.newCachedThreadPool());
        task.start();
        return task;
    }

    @SuppressWarnings("EmptyCatchBlock")
    public static void waitForDownloadStatus(int downloadJobId, int status, int timeout) {
        CountDownLatch downloadJobLatch = new CountDownLatch(1);
        UmLiveData<DownloadJob> downloadJobLiveData = UmAppDatabase.getInstance(PlatformTestUtil
                .getTargetContext()).getDownloadJobDao()
                .getByIdLive(downloadJobId);

        UmObserver<DownloadJob> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == status) {
                downloadJobLatch.countDown();
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);


        try { downloadJobLatch.await(timeout, TimeUnit.MILLISECONDS); }
        catch(InterruptedException e) {}

        downloadJobLiveData.removeObserver(downloadJobObserver);
    }

    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenDownloadStarted_whenCancelled_thenShouldBeDeleted() {
        String opdsUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme-slow/index.opds");
        CrawlJob crawlJob = runCrawlJob(opdsUrl,
                CRAWL_ROOT_ENTRY_ID_SLOW, true, CRAWL_JOB_TIMEOUT,
                true);
        UstadMobileSystemImpl.l(UMLog.INFO, 0,
                "whenCancelled_thenShouldBeDeleted: created download job # " + crawlJob.getContainersDownloadJobId());

        UmLiveData<OpdsEntryStatusCache> statusCacheLive = UmAppDatabase
                .getInstance(PlatformTestUtil.getTargetContext())
                .getOpdsEntryStatusCacheDao().findByEntryIdLive(CRAWL_ROOT_ENTRY_ID_SLOW);
        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<OpdsEntryStatusCache> observer = (statusCache) -> {
            if(statusCache != null && statusCache.getContainersDownloadedIncDescendants() > 0) {
                latch.countDown();
            }
        };
        statusCacheLive.observeForever(observer);
        try { latch.await(10, TimeUnit.SECONDS); }
        catch(InterruptedException e) {}
        statusCacheLive.removeObserver(observer);

        //try letting it download a little bit
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}

        //now cancel the download
        UstadMobileSystemImpl.getInstance().getNetworkManager().cancelDownloadJob(
                crawlJob.getContainersDownloadJobId());

        OpdsEntryStatusCache entryStatus = UmAppDatabase.getInstance(
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
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme/index.opds");
        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID, true,
                CRAWL_JOB_TIMEOUT);
        DownloadTaskListener mockDownloadlistener = Mockito.mock(DownloadTaskListener.class);

        startDownload(crawlJob.getContainersDownloadJobId(), mockDownloadlistener);

        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), STATUS_COMPLETE,
                120000);
        DownloadJob completedJob =dbManager.getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());

        OpdsEntryStatusCache rootStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID);
        Assert.assertEquals("Download job status reported as completed", STATUS_COMPLETE,
                completedJob.getStatus());
        Assert.assertEquals("Status shows all child entries downloaded",
                rootStatusCache.getSizeIncDescendants(), rootStatusCache.getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("4 containers downloaded in total",4,
                rootStatusCache.getContainersDownloadedIncDescendants());
        Mockito.verify(mockDownloadlistener, timeout(1000))
                .handleDownloadTaskStatusChanged(any(), eq(STATUS_COMPLETE));

        //now delete them all. We need to rerun the find query, if these entries were unknown before they
        // would not have been discovered

        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID), true);
    }


    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenDownloadStarted_whenPausedAndResumed_shouldComplete() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme-slow/index.opds");
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());

        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID, true,
                CRAWL_JOB_TIMEOUT, true);
        int downloadJobId = crawlJob.getContainersDownloadJobId();

        waitForDownloadStatus(downloadJobId, NetworkTask.STATUS_RUNNING, 5*1000);

        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}


        UstadMobileSystemImpl.getInstance().getNetworkManager().pauseDownloadJob(downloadJobId);

        waitForDownloadStatus(downloadJobId, NetworkTask.STATUS_PAUSED, 2*1000);

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
            if (downloadJobLiveDataUpdate.getStatus() == STATUS_COMPLETE) {
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
        Assert.assertEquals("Download job status reported as completed", STATUS_COMPLETE,
                pausedJob.getStatus());
        Assert.assertEquals("Status shows all child entries downloaded",
                rootStatusCache.getSizeIncDescendants(), rootStatusCache.getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("4 containers downloaded in total",4,
                rootStatusCache.getContainersDownloadedIncDescendants());

        //now delete it
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true);
    }

    @Test
    public void givenDownloadStarted_whenConnectivityDisconnected_shouldStopAndQueue() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), OPDS_PATH_SPEED_LIMITED );
        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID_SLOW, true,
                CRAWL_JOB_TIMEOUT);
        DownloadTaskListener taskListener = Mockito.mock(DownloadTaskListener.class);
        DownloadTask task = startDownload(crawlJob.getContainersDownloadJobId(), taskListener);

        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance()
                .getNetworkManager();
        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), NetworkTask.STATUS_RUNNING,
                10000);
        try { Thread.sleep(JOB_CHANGE_WAIT_TIME); }
        catch(InterruptedException e) {}

        //fire the connectivity disconnected event
        long connectivityChangeTime = System.currentTimeMillis();
        task.onConnectivityChanged(NetworkManager.CONNECTIVITY_STATE_DISCONNECTED);
        Mockito.verify(taskListener, timeout(5000)).handleDownloadTaskStatusChanged(any(),
                eq(STATUS_WAITING_FOR_CONNECTION));
        long timeToStop = System.currentTimeMillis() - connectivityChangeTime;
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Task stopped " + timeToStop +
                "ms after connectivity change");

        DownloadJob downloadJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertTrue("Task is stopped", task.isStopped());
        Assert.assertEquals("Job status is waiting for connection",
                NetworkTask.STATUS_WAITING_FOR_CONNECTION, downloadJob.getStatus());

        //now delete it
        networkManager.cancelDownloadJob(downloadJob.getDownloadJobId());
    }

//    TODO: make start download work independently of the crawljob
//    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenDownloadStartedUnmeteredOnly_whenConnectivityChangesToMetered_shouldStopAndQueue() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                OPDS_PATH_SPEED_LIMITED);
        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID_SLOW, true, CRAWL_JOB_TIMEOUT);
        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance()
                .getNetworkManager();

        try { Thread.sleep(JOB_CHANGE_WAIT_TIME); }
        catch(InterruptedException e) {}
        DownloadTask createdTask = networkManager.getActiveTask(
                crawlJob.getContainersDownloadJobId(), DownloadTask.class);
        createdTask.onConnectivityChanged(NetworkManager.CONNECTIVITY_STATE_METERED);

        DownloadJob downloadJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertTrue("Task is stopped", createdTask.isStopped());
        Assert.assertEquals("Job status is waiting for connection",
                NetworkTask.STATUS_WAITING_FOR_CONNECTION, downloadJob.getStatus());

        //now delete it
        networkManager.cancelDownloadJob(downloadJob.getDownloadJobId());
    }

    @Test
    public void givenDownloadStartedAnyNetwork_whenConnectivityChangesToMetered_shouldComplete() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                OPDS_PATH_SPEED_LIMITED);
        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID_SLOW, true, CRAWL_JOB_TIMEOUT);
        DownloadTaskListener taskListener = Mockito.mock(DownloadTaskListener.class);
        UstadMobileSystemImpl.l(UMLog.INFO, 0,
                "givenDownloadStartedAnyNetwork_whenConnectivityChangesToMetered_shouldComplete"
                        + " created download job #" + crawlJob.getContainersDownloadJobId());
        DownloadTask task = startDownload(crawlJob.getContainersDownloadJobId(), taskListener);

        try { Thread.sleep(JOB_CHANGE_WAIT_TIME); }
        catch(InterruptedException e) {}

        task.onConnectivityChanged(NetworkManager.CONNECTIVITY_STATE_METERED);

        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), STATUS_COMPLETE,
                120*1000);

        DownloadJob dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getCrawlJobId());
        Assert.assertEquals("Download job status = completed",
                STATUS_COMPLETE, dlJob.getStatus());
        OpdsEntryStatusCache statusCache = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getOpdsEntryStatusCacheDao().findByEntryId(CRAWL_ROOT_ENTRY_ID_SLOW);
        Assert.assertTrue("Containers have been downloaded",
                statusCache.getContainersDownloadedIncDescendants() > 0);

        //now delete it
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true);
    }

    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenDownloadStarted_whenFileIs404NotFound_shouldGiveUpAndSetFailStatus() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                OPDS_PATH_404);
        CrawlJob crawlJob = runCrawlJob(opdsRootIndexUrl, CRAWL_ROOT_ENTRY_ID_404,
                true, CRAWL_JOB_TIMEOUT);
        UstadMobileSystemImpl.l(UMLog.INFO, 0,
                "whenFileIs404NotFound_shouldGiveUpAndSetFailStatus: created download job # " +
                        crawlJob.getContainersDownloadJobId());
        startDownload(crawlJob.getContainersDownloadJobId(),
                (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager());

        try { Thread.sleep(JOB_CHANGE_WAIT_TIME); }
        catch(InterruptedException e) {}

        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), NetworkTask.STATUS_COMPLETE,
                150000);
        DownloadJob dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        List<DownloadJobItem> downloadJobItemList = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobItemDao().findAllByDownloadJobId(dlJob.getDownloadJobId());

        Assert.assertEquals("Download Job List has 1 item", 1,
                downloadJobItemList.size());
        Assert.assertEquals("Download Job Item status is failed", NetworkTask.STATUS_FAILED,
                downloadJobItemList.get(0).getStatus());
        Assert.assertEquals("When 404 hard failure response code sent, there was no retry",
                1, downloadJobItemList.get(0).getNumAttempts());
        Assert.assertEquals("DownloadJob status with failed item is complete",
                NetworkTask.STATUS_COMPLETE, dlJob.getStatus());
    }

}
