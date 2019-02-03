package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadJobItemRunner implements Runnable{

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    private CountDownLatch localConnectLatch;

    private static final int MAX_STATUS_CHECK_DELAY_TIME = 500;

    private volatile ResumableHttpDownload httpDownload;

    private boolean isTaskFinished = false;

    private Timer downloadStatusTimer = new Timer();

    private AtomicBoolean stopped = new AtomicBoolean(false);



    private TimerTask downloadStatusTimerTask = new TimerTask() {
        @Override
        public void run() {

            int downloadStatus = httpDownload.getTotalSize() == httpDownload.getDownloadedSoFar()
                    ? JobStatus.COMPLETE:JobStatus.RUNNING;
            if(!isTaskFinished){
                updateStatus(downloadStatus);
            }
        }
    };


    public DownloadJobItemRunner(DownloadJobItemWithDownloadSetItem downloadItem,
                                 NetworkManagerBle networkManager, UmAppDatabase appDb,
                                 String endpointUrl) {
        this.networkManager = networkManager;
        this.downloadItem = downloadItem;
        this.appDb = appDb;
        this.endpointUrl = endpointUrl;
    }

    public void handleConnectivityStatusChanged(ConnectivityStatus newStatus) {
        if(newStatus != null){
            //if we are waiting for a connection, and the new connection matches latch.countDown
            DownloadSetItem setItem =  appDb.getDownloadSetItemDao().findById(downloadItem.getDjiDsiUid());
            DownloadSet downloadSet = appDb.getDownloadSetDao().findByUid(setItem.getDsiDsUid());

            if(!downloadSet.isMeteredNetworkAllowed() &&
                    newStatus.getConnectivityState() == ConnectivityStatus.STATE_METERED){
                stop(JobStatus.WAITING_FOR_CONNECTION);
            }else if(newStatus.getConnectivityState() == ConnectivityStatus.STATE_CONNECTED_LOCAL){
                localConnectLatch.countDown();
            }
        }
    }

    private void stop(int newStatus) {
        stopped.set(true);
        if(httpDownload != null){
            httpDownload.stop();
        }

        updateItemStatus(newStatus);
    }



    @Override
    public void run() {
        updateItemStatus(JobStatus.RUNNING);
        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        statusObserver = this::handleConnectivityStatusChanged;
        statusLiveData.observeForever(statusObserver);

        ContentEntryFileStatus entryFileStatus = appDb.getContentEntryFileStatusDao().
                findByContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());
        if(entryFileStatus == null){
            startDownload();
        }else{
            //request connection info from the peer node
            try {
                ConnectivityStatus conStatus = new ConnectivityStatus();
                conStatus.setConnectedOrConnecting(true);
                appDb.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTING_LOCAL);
                localConnectLatch.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startDownload();
        }
    }

    private void startDownload(){
        String fileHref = endpointUrl + "ContentEntryFileServer/" + downloadItem
                .getDjiContentEntryFileUid();

        int attemptsRemaining = 3;

        boolean completed = false;

        do {
            try {
                System.out.println("Starting new resumable download for " + fileHref);
                httpDownload = new ResumableHttpDownload(fileHref, downloadItem.getDestinationFile());
                completed = httpDownload.download();
            }catch(IOException e) {
                e.printStackTrace();
            }

            attemptsRemaining--;
        }while(!stopped.get() && !completed && attemptsRemaining > 0);

        if(!stopped.get()) {
            int downloadStatus = attemptsRemaining >= 0 && !completed ? JobStatus.FAILED :
                    (httpDownload.isStopped() ? JobStatus.STOPPED : JobStatus.COMPLETE);
            updateItemStatus(downloadStatus);
        }

    }

    private void updateItemStatus(int itemStatus) {
        appDb.getDownloadJobItemDao().updateStatus(downloadItem.getDjiUid(), itemStatus);
    }





    private void updateStatus(int downloadStatus){
//        if(downloadStatus > JobStatus.RUNNING_MAX){
//            isTaskFinished = true;
//            downloadStatusTimer.cancel();
//            //statusLiveData.removeObserver(statusObserver);
//        }
//
//        appDb.getDownloadJobItemDao().updateDownloadJobItemStatus(downloadItem.getDjiUid(),
//                downloadStatus, httpDownload.getDownloadedSoFar(),
//                httpDownload.getTotalSize(),httpDownload.getCurrentDownloadSpeed());
    }
}
