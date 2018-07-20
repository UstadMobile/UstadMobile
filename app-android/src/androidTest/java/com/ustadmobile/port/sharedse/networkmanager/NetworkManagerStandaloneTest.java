package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.network.TestDownloadTaskStandalone;
import com.ustadmobile.test.sharedse.network.TestWithNetworkService;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.ustadmobile.test.sharedse.network.TestDownloadTaskStandalone.CRAWL_JOB_TIMEOUT;
import static com.ustadmobile.test.sharedse.network.TestDownloadTaskStandalone.CRAWL_ROOT_ENTRY_ID_SLOW;
import static com.ustadmobile.test.sharedse.network.TestDownloadTaskStandalone.waitForDownloadStatus;

public class NetworkManagerStandaloneTest extends TestWithNetworkService {

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }


    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenWaitingDownload_whenConnectivityRestored_thenShouldStartAndComplete() {
        CrawlJob crawlJob = TestDownloadTaskStandalone.runCrawlJob(
                UMFileUtil.resolveLink(ResourcesHttpdTestServer.getHttpRoot(),
                        TestDownloadTaskStandalone.OPDS_PATH_SPEED_LIMITED),
                        CRAWL_ROOT_ENTRY_ID_SLOW, true, CRAWL_JOB_TIMEOUT,
                true);
        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), NetworkTask.STATUS_RUNNING,
            5*1000);
        try { Thread.sleep(500);}
        catch(InterruptedException e) {}
        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance()
                .getNetworkManager();
        UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Test: setting connection disconnect");
        networkManager.handleConnectivityChanged(NetworkManagerCore.CONNECTIVITY_STATE_DISCONNECTED);

        TestDownloadTaskStandalone.waitForDownloadStatus(crawlJob.getContainersDownloadJobId(),
            NetworkTask.STATUS_WAITING_FOR_CONNECTION, 10*1000);

        //Make sure the task has stopped
        DownloadJob dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertEquals("After connectivity is disconnected, task is stopped",
                NetworkTask.STATUS_WAITING_FOR_CONNECTION, dlJob.getStatus());


        networkManager.handleConnectivityChanged(NetworkManager.CONNECTIVITY_STATE_METERED);
        TestDownloadTaskStandalone.waitForDownloadStatus(crawlJob.getContainersDownloadJobId(),
                NetworkTask.STATUS_COMPLETE, 120*1000);

        dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertEquals("After connectivity is restored, download job is completed",
                NetworkTask.STATUS_COMPLETE, dlJob.getStatus());

        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true);
    }

    @Test
    @SuppressWarnings("EmptyCatchBlock")
    public void givenWaitingDownloadRequiringUnmetered_whenUnmeteredConnectivityAvailable_thenShouldStartAndComplete() {
        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance()
                .getNetworkManager();
        networkManager.handleConnectivityChanged(NetworkManagerCore.CONNECTIVITY_STATE_UNMETERED);
        CrawlJob crawlJob = TestDownloadTaskStandalone.runCrawlJob(
                UMFileUtil.resolveLink(ResourcesHttpdTestServer.getHttpRoot(),
                        TestDownloadTaskStandalone.OPDS_PATH_SPEED_LIMITED),
                CRAWL_ROOT_ENTRY_ID_SLOW, false, CRAWL_JOB_TIMEOUT,
                true);
        waitForDownloadStatus(crawlJob.getContainersDownloadJobId(), NetworkTask.STATUS_RUNNING,
                5*1000);

        try { Thread.sleep(500);}
        catch(InterruptedException e) {}

        networkManager.handleConnectivityChanged(NetworkManagerCore.CONNECTIVITY_STATE_METERED);

        TestDownloadTaskStandalone.waitForDownloadStatus(crawlJob.getContainersDownloadJobId(),
                NetworkTask.STATUS_WAITING_FOR_CONNECTION, 10*1000);

        DownloadJob dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertEquals("Setup: download job has stopped itself",
                NetworkTask.STATUS_WAITING_FOR_CONNECTION, dlJob.getStatus());

        //bring back unmetered connectivity
        networkManager.handleConnectivityChanged(NetworkManagerCore.CONNECTIVITY_STATE_UNMETERED);

        //the download should complete
        TestDownloadTaskStandalone.waitForDownloadStatus(crawlJob.getContainersDownloadJobId(),
                NetworkTask.STATUS_COMPLETE, 120000);

        dlJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findById(crawlJob.getContainersDownloadJobId());
        Assert.assertEquals("After restoring unmetered connectivity, download job is completed",
                NetworkTask.STATUS_COMPLETE, dlJob.getStatus());

        //now delete the entries
        UstadMobileSystemImpl.getInstance().deleteEntries(PlatformTestUtil.getTargetContext(),
                Arrays.asList(CRAWL_ROOT_ENTRY_ID_SLOW), true);
    }




}
