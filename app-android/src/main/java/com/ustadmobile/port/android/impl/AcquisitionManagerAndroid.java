package com.ustadmobile.port.android.impl;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.AcquisitionManager;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.AcquisitionStatusListener;
import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by mike on 4/19/17.
 */

public class AcquisitionManagerAndroid extends AcquisitionManager {

    private HashMap<String, Long> entryIdToDownloadIdHashmap;

    private HashMap<Long, String> downloadIdToEntryIdHashmap;

    private HashMap<AcquisitionStatusListener, BroadcastReceiver> downloadCompleteReceivers;

    private HashMap<AcquisitionStatusListener, Context> downloadCompleteContexts;

    private Timer updateTimer;

    private TimerTask updateTimerTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (downloadCompleteReceivers) {
                Iterator<AcquisitionStatusListener> listeners = downloadCompleteReceivers.keySet().iterator();

                while(listeners.hasNext()) {
                    AcquisitionStatusListener listener = listeners.next();
                    Iterator<String> activeEntryIds = entryIdToDownloadIdHashmap.keySet().iterator();
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
        entryIdToDownloadIdHashmap = new HashMap<>();
        downloadIdToEntryIdHashmap = new HashMap<>();
        downloadCompleteReceivers = new HashMap<>();
        downloadCompleteContexts = new HashMap<>();
    }




    @Override
    public void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context) {
        Vector downloadDestVector = acquireFeed.getLinks(AcquisitionManager.LINK_REL_DOWNLOAD_DESTINATION,
                null);
        if(downloadDestVector.isEmpty()) {
            throw new IllegalArgumentException("No download destination in acquisition feed for acquireCatalogEntries");
        }
        File downloadDestDir = new File(((String[])downloadDestVector.get(0))[UstadJSOPDSEntry.LINK_HREF]);

        String[] selfLink = acquireFeed.getAbsoluteSelfLink();
        if(selfLink == null)
            throw new IllegalArgumentException("No absolute self link on feed - required to resolve links");

        String feedHref = selfLink[UstadJSOPDSEntry.LINK_HREF];

        Context aContext = (Context)context;
        DownloadManager manager = (DownloadManager)aContext.getSystemService(Context.DOWNLOAD_SERVICE);

        for(int i = 0; i < acquireFeed.entries.length; i++) {
            String downloadUrl = UMFileUtil.resolveLink(feedHref,
                acquireFeed.entries[i].getFirstAcquisitionLink(null)[UstadJSOPDSEntry.LINK_HREF]);
            String fileName = UMFileUtil.getFilename(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            File destFile = new File(downloadDestDir, fileName);
            request.setDestinationUri(Uri.fromFile(destFile));
            long downloadId = manager.enqueue(request);
            entryIdToDownloadIdHashmap.put(acquireFeed.entries[i].id, downloadId);
            downloadIdToEntryIdHashmap.put(downloadId, acquireFeed.entries[i].id);

            CatalogEntryInfo info = new CatalogEntryInfo();
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.downloadID = String.valueOf(downloadId);
            info.downloadTotalSize = -1;
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.fileURI = destFile.getAbsolutePath();
            info.srcURLs = new String[]{downloadUrl};
            CatalogController.setEntryInfo(acquireFeed.entries[i].id, info,
                    CatalogController.SHARED_RESOURCE, context);
        }
    }

    @Override
    public int[] getEntryStatusById(String entryId, Object context) {
        Context ctx = (Context)context;
        Long downloadId = entryIdToDownloadIdHashmap.get(entryId);
        if(downloadId == null)
            return null;

        DownloadManager mgr = (DownloadManager)ctx.getSystemService(
                Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = mgr.query(query);
        cursor.moveToFirst();

        int[] retVal = new int[3];
        retVal[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        retVal[UstadMobileSystemImpl.IDX_BYTES_TOTAL] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        retVal[UstadMobileSystemImpl.IDX_STATUS] = cursor.getInt(cursor.getColumnIndex(
                DownloadManager.COLUMN_STATUS));
        return retVal;
    }

    @Override
    public void registerEntryAquisitionStatusListener(final AcquisitionStatusListener listener, Object context) {
        final Context aContext = (Context)context;
        IntentFilter downloadCompleteIntentFilter =
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                String entryId = downloadIdToEntryIdHashmap.get(downloadId);
                if(entryId  != null) {
                    //listener.statusUpdated(new AcquisitionStatusEvent(UstadMobileSystemImpl.DLSTATUS_COM));
                    int[] entryStatus = getEntryStatusById(entryId, aContext);
                    listener.statusUpdated(new AcquisitionStatusEvent(
                        UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL,
                        entryStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL],
                        entryStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], entryId));
                    downloadIdToEntryIdHashmap.remove(downloadId);
                    entryIdToDownloadIdHashmap.remove(entryId);
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
                updateTimer.scheduleAtFixedRate(updateTimerTask, 500, 500);
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
