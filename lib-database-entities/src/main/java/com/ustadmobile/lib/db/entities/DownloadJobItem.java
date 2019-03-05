package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * A DownloadJobItem is a specific DownloadRun of a specific item. It corresponds with a given
 * DownloadSetItem representing the item to download, and a DownloadJob representing a specific
 * download run. Each DownloadSetItem can be downloaded multiple times (e.g. it can be downloaded,
 * updated, re-downloaded after the user deletes it, etc)
 */
@UmEntity
public class DownloadJobItem {

    @UmPrimaryKey(autoIncrement = true)
    private long djiUid;

    private long djiDsiUid;

    private long djiDjUid;

    private long djiContentEntryFileUid;

    private long djiContainerUid;

    private long downloadedSoFar;

    private long downloadLength;

    private long currentSpeed;

    @UmIndexField
    private long timeStarted;

    private long timeFinished;

    @UmIndexField
    private int djiStatus;

    private String destinationFile;

    private int numAttempts;


    public DownloadJobItem() {

    }

    @Deprecated
    public DownloadJobItem(DownloadJob downloadJob, DownloadSetItem downloadSetItem,
                           ContentEntryFile contentEntryFile) {
        this.djiDjUid = downloadJob.getDjUid();
        this.djiDsiUid = downloadSetItem.getDsiUid();
        this.djiContentEntryFileUid = contentEntryFile.getContentEntryFileUid();
    }

    public DownloadJobItem(DownloadJob downloadJob,DownloadSetItem downloadSetItem,
                           Container container){
        this.djiDjUid = downloadJob.getDjUid();
        this.djiDsiUid = downloadSetItem.getDsiUid();
        this.djiContainerUid = container.getContainerUid();
    }

    public long getDjiContainerUid() {
        return djiContainerUid;
    }

    public void setDjiContainerUid(long djiContainerUid) {
        this.djiContainerUid = djiContainerUid;
    }

    public long getDjiUid() {
        return djiUid;
    }

    public void setDjiUid(long djiUid) {
        this.djiUid = djiUid;
    }

    public long getDjiDsiUid() {
        return djiDsiUid;
    }

    public void setDjiDsiUid(long djiDsiUid) {
        this.djiDsiUid = djiDsiUid;
    }

    public long getDjiDjUid() {
        return djiDjUid;
    }

    public void setDjiDjUid(long djiDjUid) {
        this.djiDjUid = djiDjUid;
    }

    public long getDownloadedSoFar() {
        return downloadedSoFar;
    }

    public void setDownloadedSoFar(long downloadedSoFar) {
        this.downloadedSoFar = downloadedSoFar;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public long getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(long currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeFinished() {
        return timeFinished;
    }

    public void setTimeFinished(long timeFinished) {
        this.timeFinished = timeFinished;
    }

    public int getDjiStatus() {
        return djiStatus;
    }

    public void setDjiStatus(int djiStatus) {
        this.djiStatus = djiStatus;
    }

    public String getDestinationFile() {
        return destinationFile;
    }

    public void setDestinationFile(String destinationFile) {
        this.destinationFile = destinationFile;
    }

    public int getNumAttempts() {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts) {
        this.numAttempts = numAttempts;
    }


    @Deprecated

    public long getDjiContentEntryFileUid() {
        return djiContentEntryFileUid;
    }

    @Deprecated
    public void setDjiContentEntryFileUid(long djiContentEntryFileUid) {
        this.djiContentEntryFileUid = djiContentEntryFileUid;
    }
}
