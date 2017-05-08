package com.ustadmobile.port.sharedse.network;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kileha3 on 22/04/2017.
 */

public class DownloadTask {

    public static final int DOWNLOAD_STATUS_FAILED=-1;
    public static final int DOWNLOAD_STATUS_COMPLETED=2;
    public static final int DOWNLOAD_STATUS_RUNNING=1;
    public static final int DOWNLOAD_STATUS_CANCELLED=3;
    public static final int DOWNLOAD_STATUS_QUEUED =0;


    private UstadJSOPDSFeed feed;
    protected NetworkTaskListener listener;

    private long downloadID;
    private int downloadStatus;

    private int bytesDownloadedSoFar;

    private int downloadTotalBytes;

    private boolean testEnvironment=false;



    private final Object bytesDownloadedLock = new Object();

    public DownloadTask(UstadJSOPDSFeed feed){
        this.feed=feed;
    }


    public void setDownloadTaskListener(NetworkTaskListener listener) {
        this.listener = listener;
    }

    public void fireDownloadTaskEnded() {
        if(listener != null)
            listener.downloadTaskEnded(this);
    }
    /**
     * Start the download task
     */
    public synchronized void start() {

    }

    /**
     * Stop the download task
     * @return
     */
    public synchronized boolean stop(){
        return false;
    }

    public UstadJSOPDSFeed getFeed() {
        return feed;
    }

    public void setFeed(UstadJSOPDSFeed feed) {
        this.feed = feed;
    }

    public int getBytesDownloadedSoFar() {
        synchronized (bytesDownloadedLock) {
            return bytesDownloadedSoFar;
        }
    }

    protected void setBytesDownloadedSoFar(int bytesDownloadedSoFar) {
        synchronized (bytesDownloadedLock) {
            this.bytesDownloadedSoFar = bytesDownloadedSoFar;
        }
    }


    public int getDownloadTotalBytes() {
        return downloadTotalBytes;
    }

    public void setDownloadTotalBytes(int downloadTotalBytes) {
        this.downloadTotalBytes = downloadTotalBytes;
    }

    public boolean isTestEnvironment() {
        return testEnvironment;
    }

    public void setTestEnvironment(boolean testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    public int []  getDownloadStatus() {
        int [] statusVal=new int[3];
        statusVal[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR]=bytesDownloadedSoFar;
        statusVal[UstadMobileSystemImpl.IDX_BYTES_TOTAL]=downloadTotalBytes;
        statusVal[UstadMobileSystemImpl.IDX_STATUS]=downloadStatus;
        return statusVal;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public void setDownloadID(long downloadID){
        this.downloadID=downloadID;
    }


    public long getDownloadID(){
       return downloadID;
    }





    @Override
    public boolean equals(Object object) {
       return object instanceof DownloadTask && getFeed().equals(this.feed);
    }
}
