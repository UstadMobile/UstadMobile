package com.ustadmobile.test.port.android;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerListener;
import com.ustadmobile.port.sharedse.networkmanager.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.NetworkTask;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.TestNetworkManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by kileha3 on 28/05/2017.
 */

public class AcquisitionTask {
    private static final int DEFAULT_WAIT_TIME =20000;
    private static final String FEED_LINK_MIME ="application/dir";
    private static final String FEED_LINK_HREF ="/storage/emulated/0/ustadmobileContent";

    private static RouterNanoHTTPD resourcesHttpd;

    /**
     * The resources server can be used as the "cloud"
     */
    private static String httpRoot;

    public static final ServiceTestRule mServiceRule = new ServiceTestRule();

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException, TimeoutException {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent serviceIntent = new Intent(context, NetworkServiceAndroid.class);
        mServiceRule.bindService(serviceIntent);

        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
            httpRoot = "http://localhost:" + resourcesHttpd.getListeningPort() + "/res/";
        }
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    @Test
    public void testAcquisition() throws IOException, InterruptedException, XmlPullParserException {
        final NetworkManager manager= UstadMobileSystemImplSE.getInstanceSE().getNetworkManager();
        Assume.assumeTrue("Network test wifi and bluetooth enabled",
                manager.isBluetoothEnabled() && manager.isWiFiEnabled());

        final Object acquisitionLock=new Object();

        //Create a feed manually
        String catalogUrl = UMFileUtil.joinPaths(new String[]{
                httpRoot, "com/ustadmobile/test/sharedse/test-acquisition-task-feed.opds"});
        UstadJSOPDSFeed feed = CatalogController.getCatalogByURL(catalogUrl,
                CatalogController.SHARED_RESOURCE, null, null, 0, PlatformTestUtil.getTargetContext());

        feed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                FEED_LINK_MIME, FEED_LINK_HREF);
        feed.addLink(UstadJSOPDSItem.LINK_REL_SELF_ABSOLUTE, UstadJSOPDSItem.TYPE_ACQUISITIONFEED,
                catalogUrl);

        manager.requestAcquisition(feed,manager.getContext());

        synchronized (acquisitionLock){
            acquisitionLock.wait(DEFAULT_WAIT_TIME*10);
        }
    }
}
