package com.toughra.ustadmobile.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.v4.content.LocalBroadcastManager;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.android.network.NetworkManagerAndroid;
import com.ustadmobile.port.android.network.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.network.FileCheckResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.toughra.ustadmobile.network.ServiceBroadcastTest.TEST_SERVICE_NAME;
import static com.ustadmobile.port.android.network.BluetoothConnectionManager.ACTION_SINGLE_FILE_CHECKING_COMPLETED;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.ACTION_DOWNLOAD_COMPLETED;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.DOWNLOAD_SOURCE_PEER;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.EXTRA_ENTRY_ID;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.EXTRA_SERVICE_NAME;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.PREF_KEY_SUPERNODE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kileha3 on 02/05/2017.
 */

public class DiscoverAndDownloadTest{
    private Context mContext;
    private static final int mWaitingTimeToDiscoverANode =30000;
    private static final int mWaitingTimeForLocalFileAvailability =10000;
    private static final int mWaitingTimeForFileAcquisition =60000;
    private static List<String> fileIdsToProcess=null;


    private static final String FEED_SRC_URL ="opds:///com.ustadmobile.app.devicefeed";
    private static final String FEED_TITLE="The Little Chicks";
    private static final String FEED_ID="202b10fe-b028-4b84-9b84-852aa766607d-um-entry";
    private static final String FEED_ENTRY_ID="202b10fe-b028-4b84-9b84-852aa766607d";
    private static final String FEED_ENTRY_UPDATED="2016-11-04T13:38:49Z";

    private static final String FEED_LINK_MIME ="application/dir";
    private static final String FEED_LINK_HREF ="/storage/emulated/0/ustadmobileContent";

    private static final String ENTRY_LINK_REL="http://opds-spec.org/acquisition";
    private static final String ENTRY_LINK_MIME="application/epub+zip";
    private static final String ENTRY_LINK_HREF="/media/eXeUpload/d3288c3b-89b3-4541-a1f0-13ccf0b0eacc.um.TheLittleChicks.epub";

    private UstadMobileSystemImplAndroid implAndroid=null;
    private NetworkManagerAndroid managerAndroid;
    private BroadcastReceiver broadcastReceiver;

    private boolean notifiedEntryIdAvailable = false;
    private boolean notifiedFiledDownloaded=false;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();
    private final Object mLock=new Object();


    @Before
    public void setUp() throws TimeoutException {
        mContext= InstrumentationRegistry.getTargetContext();
        UstadMobileSystemImpl.getInstance().init(mContext);
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_KEY_SUPERNODE, "false", mContext);
        Intent serviceIntent = new Intent(mContext, NetworkServiceAndroid.class);
        serviceIntent.putExtra(EXTRA_SERVICE_NAME,TEST_SERVICE_NAME);
        mServiceRule.bindService(serviceIntent);
        fileIdsToProcess=new ArrayList<>();
        fileIdsToProcess.add(FEED_ENTRY_ID);
    }




    @Test
    public void testDiscoverAndDownload() throws InterruptedException {
        implAndroid = UstadMobileSystemImplAndroid.getInstanceAndroid();
        managerAndroid=((NetworkManagerAndroid)implAndroid.getP2PManager());
        notifiedEntryIdAvailable = false;

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String entry=null;

                switch (intent.getAction()){

                    case ACTION_SINGLE_FILE_CHECKING_COMPLETED:
                        entry=intent.getStringExtra(EXTRA_ENTRY_ID);
                        FileCheckResponse checkResponse=managerAndroid.fileCheckResponse(entry);

                        if(entry.equals(FEED_ENTRY_ID) && checkResponse!=null)
                            notifiedEntryIdAvailable=checkResponse.isFileAvailable();
                        break;
                    case ACTION_DOWNLOAD_COMPLETED:
                        entry=intent.getStringExtra(EXTRA_ENTRY_ID);
                        if(entry.equals(FEED_ENTRY_ID))
                            notifiedFiledDownloaded=true;
                        break;
                }


            }
        };

        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_SINGLE_FILE_CHECKING_COMPLETED);
        filter.addAction(ACTION_DOWNLOAD_COMPLETED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver,filter);

        synchronized (mLock){
            mLock.wait(mWaitingTimeToDiscoverANode);
        }

        assertThat("Was node discovered: ",managerAndroid.knownNodes.size()>0,is(true));

        managerAndroid.checkLocalFilesAvailability(mContext,fileIdsToProcess);
        synchronized (mLock){
            mLock.wait(mWaitingTimeForLocalFileAvailability);
        }

        assertThat("Was file processed?",managerAndroid.getAvailableFiles().size()>0,is(true));
        assertThat("Is file available locally?: ",notifiedEntryIdAvailable ,is(true));

        //Create a feed manually
        UstadJSOPDSFeed feed=new UstadJSOPDSFeed(FEED_SRC_URL,FEED_TITLE,FEED_ID);
        feed.addLink(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                FEED_LINK_MIME, FEED_LINK_HREF);
        feed.addLink(UstadJSOPDSItem.LINK_REL_SELF_ABSOLUTE, UstadJSOPDSItem.TYPE_ACQUISITIONFEED,
                "http://192.168.49.1/catalog/acquire.opds");


        UstadJSOPDSEntry entry=new UstadJSOPDSEntry(feed);
        entry.id=FEED_ENTRY_ID;
        entry.title=FEED_TITLE;
        entry.updated=FEED_ENTRY_UPDATED;
        entry.addLink(ENTRY_LINK_REL,ENTRY_LINK_MIME,ENTRY_LINK_HREF);
        feed.addEntry(entry);
        managerAndroid.createFileAcquisitionTask(feed,mContext);

        synchronized (mLock){
            mLock.wait(mWaitingTimeForFileAcquisition);
        }

        assertThat("Was file downloaded successfully?: ",notifiedFiledDownloaded ,is(true));
        assertThat("Was file downloaded locally?:",managerAndroid.getDownloadSource(),
                is(DOWNLOAD_SOURCE_PEER));


    }


}
