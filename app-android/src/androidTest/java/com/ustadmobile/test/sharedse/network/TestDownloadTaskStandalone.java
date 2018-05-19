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
        OpdsEntryStatusCache startStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID_SLOW);
        System.out.println(startStatusCache);

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
    public void testRecursiveDownload(){
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<String> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                .findAllChildEntryIdsRecursive(CRAWL_ROOT_ENTRY_ID);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }


        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme/index.opds");
        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setRootEntryUri(opdsRootIndexUrl);
        crawlJob.setQueueDownloadJobOnDone(true);
        DownloadSet downloadJob = new DownloadSet();
        downloadJob.setDestinationDir(storageDir);
        final Object lock = new Object();

        crawlJob = UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownload(downloadJob,
                crawlJob);

        UmLiveData<DownloadJob> downloadJobLiveData = dbManager.getDownloadJobDao()
                .getByIdLive(crawlJob.getContainersDownloadJobId());
        UmObserver<DownloadJob> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == NetworkTask.STATUS_COMPLETE) {
                synchronized (lock) {
                    try { lock.notifyAll();}
                    catch(Exception e) {}
                }
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);

        if(downloadJobLiveData.getValue() == null
                || downloadJobLiveData.getValue().getStatus() != NetworkTask.STATUS_COMPLETE) {
            synchronized (lock) {
                try { lock.wait(120000); }
                catch (InterruptedException e) { }
            }
        }

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
                Arrays.asList(CRAWL_ROOT_ENTRY_ID), true,
                new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        synchronized (complete) {
                            complete[0] = true;
                            complete.notifyAll();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        complete.notifyAll();
                    }
                });

        synchronized (complete) {
            if(!complete[0]){
                try { complete.wait(10000); }
                catch(InterruptedException e) {}
            }
        }

        //now make sure that the entries have been marked as deleted
        rootStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID);
        Assert.assertEquals("After entries are deleted, 0 entries are marked as being downloaded",
                0, rootStatusCache.getContainersDownloadedIncDescendants());
        Assert.assertEquals("After entries are deleted, total downloaded file size is 0",
                0, rootStatusCache.getContainersDownloadedSizeIncDescendants());
    }


    @Test
    public void givenDownloadStarted_whenPausedAndResumed_shouldComplete() {
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<String> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                .findAllChildEntryIdsRecursive(CRAWL_ROOT_ENTRY_ID);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }


        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme-slow/index.opds");
        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setRootEntryUri(opdsRootIndexUrl);
        crawlJob.setQueueDownloadJobOnDone(true);
        DownloadSet downloadJob = new DownloadSet();
        downloadJob.setDestinationDir(storageDir);
        final Object lock = new Object();

        crawlJob = UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownload(downloadJob,
                crawlJob);
        int downloadJobId = crawlJob.getContainersDownloadJobId();
        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}


        final boolean[] completed = new boolean[1];
        UstadMobileSystemImpl.getInstance().getNetworkManager().pauseDownloadJobAsync(downloadJobId,
                (pausedOK) -> {
                    synchronized (completed) {
                        completed[0] = true;
                        try { completed.notifyAll(); }
                        catch(Exception e) {}
                    }
                });

        synchronized (completed){
            if(!completed[0]){
                try { completed.wait(12000);}
                catch(InterruptedException e) {}
            }
        }


        DownloadJob pausedJob = dbManager.getDownloadJobDao().findById(downloadJobId);
        Assert.assertEquals("Task status is paused",
                NetworkTask.STATUS_PAUSED, pausedJob.getStatus());

        UstadMobileSystemImpl.l(UMLog.INFO, 0,
            "TestDownloadTaskStandalone: checked task was paused, now queueing download job again");
        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);

        UmLiveData<DownloadJob> downloadJobLiveData = dbManager.getDownloadJobDao()
                .getByIdLive(crawlJob.getContainersDownloadJobId());
        UmObserver<DownloadJob> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == NetworkTask.STATUS_COMPLETE) {
                synchronized (lock) {
                    try { lock.notifyAll();}
                    catch(Exception e) {}
                }
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);

        if(downloadJobLiveData.getValue() == null
                || downloadJobLiveData.getValue().getStatus() != NetworkTask.STATUS_COMPLETE) {
            synchronized (lock) {
                try { lock.wait(120000 * 5); }
                catch (InterruptedException e) { }
            }
        }

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
        final boolean[] complete = new boolean[1];
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true,
                new UmCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        synchronized (complete) {
                            complete[0] = true;
                            complete.notifyAll();
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        complete.notifyAll();
                    }
                });

        synchronized (complete) {
            if(!complete[0]){
                try { complete.wait(10000); }
                catch(InterruptedException e) {}
            }
        }
    }
}
