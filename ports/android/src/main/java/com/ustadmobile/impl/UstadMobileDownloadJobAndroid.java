package com.ustadmobile.impl;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 *
 * Hints from http://jhshi.me/2013/12/02/how-to-use-downloadmanager/
 *
 * Created by mike on 28/06/15.
 */
public class UstadMobileDownloadJobAndroid implements UMTransferJob{

    public UstadMobileDownloadJobAndroid(Context context) {
        this.context = context;
    }

    private Context context;

    private BroadcastReceiver downloadCompleteReceiver;

    private UMProgressListener[] listeners;

    private String url;


    @Override
    public void start() {
        IntentFilter downloadCompleteFilter = new IntentFilter(
            DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };


    }

    @Override
    public void addProgresListener(UMProgressListener umProgressListener) {

    }
}
