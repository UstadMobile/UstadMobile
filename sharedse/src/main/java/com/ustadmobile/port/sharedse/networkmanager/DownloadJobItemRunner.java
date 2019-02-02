package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class DownloadJobItemRunner implements Runnable{

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    private CountDownLatch localConnectLatch;

    private ResumableHttpDownload httpDownload;


    //TODO: add a timer that updates the progress of the download job

    public DownloadJobItemRunner(DownloadJobItemWithDownloadSetItem downloadItem,
                                 NetworkManagerBle networkManager, UmAppDatabase appDb,
                                 String endpointUrl) {
        this.networkManager = networkManager;
        this.downloadItem = downloadItem;
        this.appDb = appDb;
        this.endpointUrl = endpointUrl;
    }

    public void handleConnetivityStatusChanged(ConnectivityStatus newStatus) {
        //if we are waiting for a connection, and the new connection matches latch.countDown
    }

    @Override
    public void run() {
        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        statusObserver = this::handleConnetivityStatusChanged;
        statusLiveData.observeForever(statusObserver);

        //TODO: loop for retry

        //decide where to download this from - e.g. cloud or local

        String fileHref = endpointUrl + "/ContentEntryFileServer/" + downloadItem
                .getDjiContentEntryFileUid();
        httpDownload = new ResumableHttpDownload(fileHref, downloadItem.getDestinationFile());
        //httpDownload.download();



        //if we need a local connection, tell networkmanagerble to connect, then use the latch to wait


        //when done
        statusLiveData.removeObserver(statusObserver);
    }
}
