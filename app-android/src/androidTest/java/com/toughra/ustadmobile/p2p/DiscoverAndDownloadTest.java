package com.toughra.ustadmobile.p2p;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplFactoryAndroid;
import com.ustadmobile.port.android.network.NetworkManagerAndroid;
import com.ustadmobile.port.android.network.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.network.NetworkNode;
import com.ustadmobile.port.sharedse.network.NetworkNodeListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import static com.toughra.ustadmobile.p2p.ServiceBroadcastTest.TEST_SERVICE_NAME;
import static com.ustadmobile.port.android.network.DownloadTaskAndroid.DOWNLOAD_SOURCE_P2P;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.EXTRA_SERVICE_NAME;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.PREF_KEY_SUPERNODE;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kileha3 on 03/04/2017.
 */

public class DiscoverAndDownloadTest implements NetworkNodeListener,UMDownloadCompleteReceiver {

    private Context mContext;
    private static final int mWaitingTimeToDiscoverANode =40000;
    private static final int mWaitingTimeForLocalFileAvailability =2000;
    private static final int mWaitingTimeDownloadFileTask =8000;
    private static final int mSuperNodeIndex=0;
    private static final String TEST_FILE_ID = "202b10fe-b028-4b84-9b84-852aa766607d";
    private static final String TEST_FILE_URI = "http://www.ustadmobile.com/files/budget-savings-trainer.epub";
    private static int downloadedFromPeer=-1;

    private NetworkNode lastNodeDiscovered = null;
    private UstadMobileSystemImplAndroid implAndroid=null;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();
    private final Object mLock=new Object();

    @Before
    public void setUp() throws TimeoutException {
        UstadMobileSystemImpl.setSystemImplFactoryClass(UstadMobileSystemImplFactoryAndroid.class);
        mContext= InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(mContext);
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_KEY_SUPERNODE, "false", mContext);
        Intent serviceIntent = new Intent(mContext, NetworkServiceAndroid.class);
        serviceIntent.putExtra(EXTRA_SERVICE_NAME,TEST_SERVICE_NAME);
        mServiceRule.bindService(serviceIntent);
        lastNodeDiscovered = null;
        downloadedFromPeer = -1;
    }


    @Test
    public void testDiscoverAndDownload() throws InterruptedException {
        implAndroid = UstadMobileSystemImplAndroid.getInstanceAndroid();
        implAndroid.getP2PManager().addNodeListener(this);

        synchronized (mLock){
            mLock.wait(mWaitingTimeToDiscoverANode);
        }
        assertNotNull("Node was discovered: ",lastNodeDiscovered);

        File destinationFile = fileToDownload();
        if(destinationFile.exists())
            destinationFile.delete();


        synchronized (mLock){
            mLock.wait(mWaitingTimeForLocalFileAvailability);
        }
        assertThat("File is available locally: ",
                implAndroid.getP2PManager().isFileAvailable(mContext,TEST_FILE_ID),is(true));


        implAndroid.registerDownloadCompleteReceiver(this,mContext);
        implAndroid.queueFileDownload(TEST_FILE_URI,destinationFile.getAbsolutePath(),TEST_FILE_ID,null,mContext);
        synchronized (mLock){
            mLock.wait(mWaitingTimeDownloadFileTask);
        }

        assertThat("File was downloaded: ",destinationFile.exists(),is(true));
        assertThat("Was file Downloaded from peer: ",downloadedFromPeer,is(DOWNLOAD_SOURCE_P2P));

    }

    @Override
    public void nodeDiscovered(NetworkNode node) {
        lastNodeDiscovered = node;

    }

    @Override
    public void nodeGone(NetworkNode node) {

    }
    @Override
    public void downloadStatusUpdated(UMDownloadCompleteEvent evt) {
        downloadedFromPeer=evt.getDownloadedFromPeer();
    }

    private File fileToDownload(){
        String storageDir = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogController.SHARED_RESOURCE, mContext)[0].getDirURI();

        NetworkNode node=implAndroid.getP2PManager().knownNodes.get(mSuperNodeIndex);
        UstadJSOPDSFeed   fileFeed = ((NetworkManagerAndroid)implAndroid.getP2PManager())
                .getAvailableIndexes().get(node);
        UstadJSOPDSEntry  fileEntry = fileFeed.getEntryById(TEST_FILE_ID);
        Vector acquisitionLinks = fileEntry.getAcquisitionLinks();
        String[] acquisitionAttrs = (String[])acquisitionLinks.get(mSuperNodeIndex);
        String url = acquisitionAttrs[UstadJSOPDSItem.ATTR_HREF];
        return new File(storageDir, UMFileUtil.getFilename(url));
    }



}
