package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class AcquisitionTask extends NetworkTask {

    private UstadJSOPDSFeed feed;

    protected NetworkManagerTaskListener listener;

    private long downloadID;

    private int downloadStatus;

    private int bytesDownloadedSoFar;

    private int downloadTotalBytes;

    private boolean testEnvironment=false;

    private final Object bytesDownloadedLock = new Object();


    public AcquisitionTask(UstadJSOPDSFeed feed){
        this.feed=feed;
    }

    public void fireAcquisitionTaskCompleted() {
        if(listener != null)
            listener.handleTaskCompleted(this);
    }
    /**
     * Start the download task
     */
    public synchronized void start() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public int getQueueId() {
        return this.queueId;
    }

    @Override
    public int getTaskId() {
        return this.taskId;
    }

    @Override
    public int getTaskType() {
        return this.taskType;
    }

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
        statusVal[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR]=getBytesDownloadedSoFar();
        statusVal[UstadMobileSystemImpl.IDX_BYTES_TOTAL]=getDownloadTotalBytes();
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
        return object instanceof AcquisitionTask && getFeed().equals(this.feed);
    }
}
