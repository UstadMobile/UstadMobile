package com.ustadmobile.test.sharedse.view;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Hashtable;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Created by mike on 3/5/18.
 */
public class TestStartDownloadPresenter {

    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private NetworkServiceAndroid sService;

    private ServiceConnection sServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sService = ((NetworkServiceAndroid.LocalServiceBinder)iBinder)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Before
    public void startNetworkService() throws Exception{
        final Context context = (Context)PlatformTestUtil.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(PlatformTestUtil.getTargetContext());
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent, sServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @After
    public void stopNetworkService() throws Exception {
        mServiceRule.unbindService();
    }

    @BeforeClass
    public static void startResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    @Test
    public void testCreation() {
        DownloadDialogView mockView = Mockito.mock(DownloadDialogView.class);
        Hashtable testArgs = new Hashtable();


        final Object lockObj = new Object();
        final String[] progressTextVal = new String[1];
        final String expectedTextStatus = "Indexed 3/3";
        final long startTime = System.currentTimeMillis();
        String opdsRootIndexUrl = UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                "com/ustadmobile/test/sharedse/crawlme/index.opds");
        testArgs.put(DownloadDialogPresenter.ARG_ROOT_URIS, new String[]{opdsRootIndexUrl});

        DownloadDialogPresenter presenter = new DownloadDialogPresenter(PlatformTestUtil.getTargetContext(),
                mockView, testArgs);

        doAnswer((invocationOnMock) -> {
            progressTextVal[0] = invocationOnMock.getArgument(0);
            if(invocationOnMock.getArgument(0).equals(expectedTextStatus)){
                synchronized (lockObj){
                    try {lockObj.notifyAll();}
                    catch(Exception e) {}
                }
            }

            return null;
        }).when(mockView).setProgressStatusText(anyString());

        presenter.onCreate(null);

        if(!expectedTextStatus.equals(progressTextVal[0])){
            synchronized (lockObj) {
                try {lockObj.wait(10000);}
                catch(InterruptedException e) {}
            }
        }

        verify(mockView, atLeastOnce()).setProgressStatusText(anyString());
        Assert.assertEquals("Status text is as expected", expectedTextStatus,
                progressTextVal[0]);


        //test when we click download a download job is started
        //TODO: Update this
        presenter.handleClickConfirm();

        DownloadJob createdJob = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                .getDownloadJobDao().findLastCreatedDownloadJob();
        Assert.assertTrue("Download job was created after start of this test",
                createdJob.getTimeCreated() > startTime);

    }

}
