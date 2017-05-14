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
import com.ustadmobile.port.android.netwokmanager.DownloadManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerAndroid extends AcquisitionManager {
    private NetworkManagerAndroid managerAndroid;
    private final HashMap<AcquisitionStatusListener, BroadcastReceiver> downloadCompleteReceivers;

    private HashMap<AcquisitionStatusListener, Context> downloadCompleteContexts;

    private Timer updateTimer;
    public static final int DEFAULT_TIMER_UPDATE_INTERVAL = 500;

    private TimerTask updateTimerTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (downloadCompleteReceivers) {
                Iterator<AcquisitionStatusListener> listeners = downloadCompleteReceivers.keySet().iterator();

                while(listeners.hasNext()) {

                    AcquisitionStatusListener listener = listeners.next();
                    Iterator<String> activeEntryIds = managerAndroid.getEntryIdToDownloadIdMap().keySet().iterator();
                    while(activeEntryIds.hasNext()) {
                        String entryId = activeEntryIds.next();
                        int[] downloadStatus = AcquisitionManagerAndroid.getInstance().getEntryStatusById(
                                entryId, downloadCompleteContexts.get(listener));
                        AcquisitionStatusEvent event = new AcquisitionStatusEvent(
                                downloadStatus[UstadMobileSystemImpl.IDX_STATUS],
                                downloadStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL],
                                downloadStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], entryId);
                        listener.statusUpdated(event);
                    }
                }
            }
        }
    };

    public AcquisitionManagerAndroid() {
        downloadCompleteReceivers = new HashMap<>();
        downloadCompleteContexts = new HashMap<>();
        managerAndroid=(NetworkManagerAndroid)UstadMobileSystemImplAndroid.getInstance()
                .getNetworkManager();
    }




    @Override
    public void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context) {
        //managerAndroid.createAcquisitionTask(acquireFeed,context);
    }

    @Override
    public int[] getEntryStatusById(String entryId, Object context) {
        Long downloadId = managerAndroid.getEntryIdToDownloadIdMap().
                get(entryId);
        if(downloadId == null)
            return null;
        HashMap<Long,int[]> valStatus=managerAndroid.getDownloadIdToDownloadStatusMap();
        return valStatus.get(downloadId);
    }

    @Override
    public void registerEntryAquisitionStatusListener(final AcquisitionStatusListener listener, Object context) {
        final Context aContext = (Context)context;
        IntentFilter downloadCompleteIntentFilter =
                new IntentFilter(DownloadManagerAndroid.ACTION_DOWNLOAD_COMPLETED);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Long downloadId = intent.getLongExtra(DownloadManagerAndroid.EXTRA_DOWNLOAD_ID, 0L);
                String entryId = managerAndroid.getDownloadIdToEntryIdMap().get(downloadId);
                if(entryId  != null) {
                    //managerTaskListener.statusUpdated(new AcquisitionStatusEvent(UstadMobileSystemImpl.DLSTATUS_COM));
                    int[] entryStatus = getEntryStatusById(entryId, aContext);
                    listener.statusUpdated(new AcquisitionStatusEvent(
                            UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL,
                            entryStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL],
                            entryStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], entryId));
                    managerAndroid.getDownloadIdToEntryIdMap().remove(downloadId);
                    managerAndroid.getEntryIdToDownloadIdMap().remove(entryId);
                }

            }
        };



        synchronized (downloadCompleteReceivers) {
            downloadCompleteReceivers.put(listener, completeReceiver);
            downloadCompleteContexts.put(listener, aContext);
            aContext.registerReceiver(completeReceiver, downloadCompleteIntentFilter);

        }


        if (updateTimer == null) {
            synchronized (this) {
                updateTimer = new Timer();
                updateTimer.scheduleAtFixedRate(updateTimerTask,
                        DEFAULT_TIMER_UPDATE_INTERVAL, DEFAULT_TIMER_UPDATE_INTERVAL);
            }
        }
    }

    @Override
    public void unregisterEntryAquisitionStatusListener(AcquisitionStatusListener listener, Object context) {
        Context aContext = (Context)context;
        BroadcastReceiver receiver = downloadCompleteReceivers.get(listener);
        aContext.unregisterReceiver(receiver);

        synchronized (downloadCompleteReceivers) {
            downloadCompleteReceivers.remove(listener);
            downloadCompleteContexts.remove(listener);

        }

        if(downloadCompleteReceivers.isEmpty()) {
            synchronized (this) {
                updateTimer.cancel();
                updateTimer = null;
            }
        }
    }
}
