package com.ustadmobile.test.sharedse.network;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 3/6/18.
 *
 * Note: Sometimes the observer on Android is not calling the onChanged method of the observer as
 * it should when the status is updated.
 *
 */
@SmallTest
public class TestCrawlJob {

    public static final int CRAWL_JOB_TIMEOUT = 1500;//* 100 for testing purposes only

    private static RouterNanoHTTPD resourcesHttpd;

    public static final ServiceTestRule mServiceRule = new ServiceTestRule();


    @BeforeClass
    public static void startHttpResourcesServer() throws IOException, InterruptedException {
        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
        }
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    private static NetworkServiceAndroid sService;

    public static ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @BeforeClass
    public static void startNetworkService() throws Exception{
        Context context = InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(context);
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent, sServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @AfterClass
    public static void stopNetworkService() throws Exception {
        mServiceRule.unbindService();
    }




    @Test
    public void testCrawl() {
        String opdsRootIndexUrl = UMFileUtil.joinPaths(
                "http://localhost:" + resourcesHttpd.getListeningPort(),
                "res/com/ustadmobile/test/sharedse/crawlme/index.opds");

        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, PlatformTestUtil.getTargetContext())[0].getDirURI();
        NetworkManager networkManager = (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager();
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());

        DownloadSet set = new DownloadSet();
        set.setDestinationDir(storageDir);
        CrawlJob crawlJob = new CrawlJob();
        crawlJob.setRootEntryUri(opdsRootIndexUrl);
        crawlJob = UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownload(set, crawlJob, true);
        UstadMobileSystemImpl.l(UMLog.DEBUG, 0, "Created crawl job id# " +
                crawlJob.getCrawlJobId());
        UmLiveData<CrawlJobWithTotals> crawlJobLiveData = dbManager.getCrawlJobDao().findWithTotalsByIdLive(
                crawlJob.getCrawlJobId());
        CountDownLatch latch = new CountDownLatch(1);


        UmObserver<CrawlJobWithTotals> crawlJobUmObserver = (crawlJobValue) -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 0,
                    "crawlJobObserver: status " + crawlJobValue.getStatus());
            if(crawlJobValue.getStatus() == NetworkTask.STATUS_COMPLETE){
                latch.countDown();
            }
        };
        crawlJobLiveData.observeForever(crawlJobUmObserver);

        try { latch.await(CRAWL_JOB_TIMEOUT, TimeUnit.MILLISECONDS); }
        catch(InterruptedException e) {}

        CrawlJobWithTotals crawlJobValue = dbManager.getCrawlJobDao().findWithTotalsById(
                crawlJob.getCrawlJobId());

        Assert.assertEquals("Crawl job status complete", NetworkTask.STATUS_COMPLETE,
                crawlJobValue.getStatus());

        Assert.assertEquals("Crawl job has three items", 3,
                crawlJobValue.getNumItems());

        List<DownloadJobItem> downloadJobItems = dbManager.getDownloadJobItemDao().
                findAllByDownloadJobId(crawlJob.getContainersDownloadJobId());
        for(DownloadJobItem item : downloadJobItems) {
            Assert.assertTrue("Job Item #" + item.getDownloadJobItemId() + " has size found > 0",
                    item.getDownloadLength() > 0);
        }

        Assert.assertEquals("All three crawl job items are completed", 3,
                crawlJobValue.getNumItemsCompleted());

        crawlJobLiveData.removeObserver(crawlJobUmObserver);
    }



}
