package com.ustadmobile.test.sharedse.network;

/**
 * Created by mike on 3/11/18.
 */

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.fs.db.ContainerFileHelper;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.port.sharedse.networkmanager.CrawlTask;
import com.ustadmobile.port.sharedse.networkmanager.DownloadTask;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.annotation.UmMediumTest;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the download task that run without requiring a network peer to download entries from
 */
@UmMediumTest
public class TestDownloadTaskStandalone extends TestWithNetworkService {

    protected static final String CRAWL_ROOT_ENTRY_ID = "http://umcloud1.ustadmobile.com/opds/test-crawl";

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException{
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    /**
     * Test downloading entries in a recursive manner
     */
    @Test
    public void testRecursiveDownload(){
        ArrayList<OpdsEntryWithRelations> entryList = new ArrayList<>();
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager();
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<String> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                .findAllChildEntryIdsRecursive(CRAWL_ROOT_ENTRY_ID);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }

        DownloadJob job = UstadMobileSystemImpl.getInstance().getNetworkManager().buildDownloadJob(
                entryList, storageDir, false, true, true);
        job.setMobileDataEnabled(true);
        dbManager.getDownloadJobDao().update(job);


        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/sharedse/crawlme/index.opds");
        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setContainersDownloadJobId(job.getId());
        crawlJob.setCrawlJobId((int)dbManager.getCrawlJobDao().insert(crawlJob));
        CrawlJobItem rootCrawlItem = new CrawlJobItem(crawlJob.getCrawlJobId(), opdsRootIndexUrl,
                NetworkTask.STATUS_QUEUED, 0);
        dbManager.getDownloadJobCrawlItemDao().insert(rootCrawlItem);


        CrawlTask crawlTask = new CrawlTask(crawlJob, dbManager, networkManager);
        UmLiveData<CrawlJobWithTotals> crawlJobLiveData = dbManager.getCrawlJobDao().findWithTotalsByIdLive(
                crawlJob.getCrawlJobId());
        final Object lock = new Object();
        UmObserver<CrawlJobWithTotals> crawlJobUmObserver = (crawlJobValue) -> {
            if(crawlJobValue.getStatus() == NetworkTask.STATUS_COMPLETE){
                synchronized (lock){
                    try {lock.notifyAll(); }
                    catch(Exception e){}
                }
            }
        };
        crawlTask.start();
        crawlJobLiveData.observeForever(crawlJobUmObserver);
        if(crawlJobLiveData.getValue() == null || crawlJobLiveData.getValue().getStatus() != NetworkTask.STATUS_COMPLETE) {
            synchronized (lock){
                try { lock.wait(10000);}
                catch(InterruptedException e) {}
            }
        }

        crawlJobLiveData.removeObserver(crawlJobUmObserver);

        //now download the entries in the job
        DownloadJobWithRelations jobWithRelations = dbManager.getDownloadJobDao().findById(job.getId());
        DownloadTask downloadTask = new DownloadTask(jobWithRelations, networkManager);

        UmLiveData<OpdsEntryStatusCache> dlStatus = dbManager.getOpdsEntryStatusCacheDao().
                findByEntryIdLive(CRAWL_ROOT_ENTRY_ID);


        final boolean[] receivedProgressUpdate = new boolean[1];
        UmObserver<OpdsEntryStatusCache> statusUmObserver = (downloadStatus) -> {
            if(downloadStatus.getContainersDownloadPendingIncAncestors() < downloadStatus.getSizeIncDescendants()) {
                receivedProgressUpdate[0] = true;
            }
        };

        UmLiveData<DownloadJobWithRelations> downloadJobLiveData = dbManager.getDownloadJobDao()
                .getByIdLive(job.getId());
        UmObserver<DownloadJobWithRelations> downloadJobObserver = (downloadJobLiveDataUpdate) -> {
            if (downloadJobLiveDataUpdate.getStatus() == NetworkTask.STATUS_COMPLETE) {
                synchronized (lock) {
                    try { lock.notifyAll();}
                    catch(Exception e) {}
                }
            }
        };
        downloadJobLiveData.observeForever(downloadJobObserver);

        dlStatus.observeForever(statusUmObserver);
        downloadTask.start();

        synchronized (lock){
            try {lock.wait(120000);}
            catch(InterruptedException e) {}
        }

        DownloadJob completedJob =dbManager.getDownloadJobDao().findById(job.getId());

        Assert.assertEquals("Download job status reported as completed", NetworkTask.STATUS_COMPLETE,
                completedJob.getStatus());
        Assert.assertEquals("Status shows all child entries downloaded",
                dlStatus.getValue().getSizeIncDescendants(), dlStatus.getValue().getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("4 containers downloaded in total",4,
                dlStatus.getValue().getContainersDownloadedIncDescendants());

        //now delete them all. We need to rerun the find query, if these entries were unknown before they
        // would not have been discovered
        childEntries = dbManager.getOpdsEntryWithRelationsDao().findAllChildEntryIdsRecursive(CRAWL_ROOT_ENTRY_ID);
        for(String entryId : childEntries) {
            ContainerFileHelper.getInstance().deleteAllContainerFilesByEntryId(PlatformTestUtil.getTargetContext(),
                    entryId);
        }

        //now make sure that the entries have been marked as deleted
        OpdsEntryStatusCache rootStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(CRAWL_ROOT_ENTRY_ID);
        Assert.assertEquals("After entries are deleted, 0 entries are marked as being downloaded",
                0, rootStatusCache.getContainersDownloadedIncDescendants());
        Assert.assertEquals("After entries are deleted, total downloaded file size is 0",
                0, rootStatusCache.getContainersDownloadedSizeIncDescendants());
    }
}
