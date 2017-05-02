package com.ustadmobile.port.android.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.port.android.network.DownloadManagerAndroid;
import com.ustadmobile.port.android.network.NetworkManagerAndroid;

import java.util.HashMap;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerAndroid extends AcquisitionManager {


    private HashMap<AcquisitionStatusListener, BroadcastReceiver> downloadCompleteReceivers;

    private NetworkManagerAndroid managerAndroid;
    private DownloadManagerAndroid downloadManager;

    public AcquisitionManagerAndroid() {
        downloadCompleteReceivers = new HashMap<>();
        managerAndroid= (NetworkManagerAndroid) UstadMobileSystemImplAndroid.getInstance().getP2PManager();
    }


    @Override
    public void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context) {
        managerAndroid.downloadFile(acquireFeed, context);
    }

    @Override
    public int[] getEntryStatusById(String entryId, Object context) {
        downloadManager=managerAndroid.getDownloadManager();
        long downloadID=downloadManager.getEntryDownloadLog().get(entryId);
        return downloadManager.getFeedDownloadStatus().get(downloadID).get(entryId);
    }

    @Override
    public void registerEntryAquisitionStatusListener(final AcquisitionStatusListener listener, Object context) {
        final Context aContext = (Context)context;
        IntentFilter downloadCompleteIntentFilter =
            new IntentFilter(DownloadManagerAndroid.ACTION_DOWNLOAD_COMPLETED);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String entryId = intent.getStringExtra(DownloadManagerAndroid.EXTRA_DOWNLOAD_ENTRY_ID);
                if(entryId  != null) {
                    //listener.statusUpdated(new AcquisitionStatusEvent(UstadMobileSystemImpl.DLSTATUS_COM));

                    int[] entryStatus = getEntryStatusById(entryId, aContext);
                    listener.statusUpdated(new AcquisitionStatusEvent(
                        UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL,
                        entryStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL],
                        entryStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], entryId));
                }
            }
        };

        downloadCompleteReceivers.put(listener, completeReceiver);
        aContext.registerReceiver(completeReceiver, downloadCompleteIntentFilter);
    }

    @Override
    public void unregisterEntryAquisitionStatusListener(AcquisitionStatusListener listener, Object context) {
        Context aContext = (Context)context;
        BroadcastReceiver receiver = downloadCompleteReceivers.get(listener);
        aContext.unregisterReceiver(receiver);
        downloadCompleteReceivers.remove(listener);
    }
}
