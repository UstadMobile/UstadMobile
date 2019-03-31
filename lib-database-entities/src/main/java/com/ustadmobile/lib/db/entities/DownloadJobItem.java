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

    private long djiContainerUid;

    private long djiContentEntryUid;

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

    @Deprecated
    private long djiParentDjiUid;


    public DownloadJobItem() {

    }

    public DownloadJobItem(DownloadJobItem src) {
        this.djiUid = src.djiUid;
        this.downloadLength = src.downloadLength;
        this.downloadedSoFar = src.downloadedSoFar;
        this.djiContentEntryUid = src.djiContentEntryUid;
        this.djiDjUid = src.djiDjUid;
        this.djiStatus = src.djiStatus;
        this.djiContainerUid = src.djiContainerUid;
        this.currentSpeed = src.currentSpeed;
        this.destinationFile = src.destinationFile;
        this.numAttempts = src.numAttempts;
    }

    public DownloadJobItem(DownloadJob downloadJob, long djiContentEntryUid, long downloadLength) {
        this(downloadJob.getDjUid(), djiContentEntryUid, downloadLength);
    }

    public DownloadJobItem(long djiDjUid, long djiContentEntryUid, long downloadLength) {
        this.djiDjUid = djiDjUid;
        this.djiContentEntryUid = djiContentEntryUid;
        this.downloadLength = downloadLength;
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
    public long getDjiParentDjiUid() {
        return djiParentDjiUid;
    }

    @Deprecated
    public void setDjiParentDjiUid(long djiParentDjiUid) {
        this.djiParentDjiUid = djiParentDjiUid;
    }


    public long getDjiContentEntryUid() {
        return djiContentEntryUid;
    }

    public void setDjiContentEntryUid(long djiContentEntryUid) {
        this.djiContentEntryUid = djiContentEntryUid;
    }
}
