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
    private int downloadJobItemId;

    private int downloadSetItemId;

    private int downloadJobId;

    private long downloadedSoFar;

    private long downloadLength;

    private long currentSpeed;

    private long timeStarted;

    private long timeFinished;

    @UmIndexField
    private int status;

    private String destinationFile;

    private int numAttempts;

    /**
     * Constructor
     *
     * @param downloadSetItemId The related DownloadSetItem primary key
     * @param downloadJobId The related DownloadJob primary key
     */
    public DownloadJobItem(int downloadSetItemId, int downloadJobId) {
        this.downloadSetItemId = downloadSetItemId;
        this.downloadJobId = downloadJobId;
    }


    /**
     * Get the primary key
     *
     * @return The primary key
     */
    public int getDownloadJobItemId() {
        return downloadJobItemId;
    }

    /**
     * Set the primary key
     *
     * @param downloadJobItemId The primary key
     */
    public void setDownloadJobItemId(int downloadJobItemId) {
        this.downloadJobItemId = downloadJobItemId;
    }

    /**
     * Get the primary key of the related DownoadSetItem
     *
     * @return The primary key of the related DownloadSetItem
     */
    public int getDownloadSetItemId() {
        return downloadSetItemId;
    }

    /**
     * Set the primary key of the related DownloadSetItem
     *
     * @param downloadSetItemId The primary key of the related DownloadSetItem
     */
    public void setDownloadSetItemId(int downloadSetItemId) {
        this.downloadSetItemId = downloadSetItemId;
    }


    /**
     * Get the primary key of the related DownloadJob
     *
     * @return the primary key of the related DownloadJob
     */
    public int getDownloadJobId() {
        return downloadJobId;
    }

    /**
     * Set the primary key of the related DownloadJob
     *
     * @param downloadJobId the primary key of the related DownloadJob
     */
    public void setDownloadJobId(int downloadJobId) {
        this.downloadJobId = downloadJobId;
    }

    /**
     * Get the total downloaded so far (in bytes) for this DownloadJobItem
     *
     * @return the total downloaded so far (in bytes)
     */
    public long getDownloadedSoFar() {
        return downloadedSoFar;
    }

    /**
     * Set the total downloaded so far (in bytes) for this DownloadJobItem
     *
     * @param downloadedSoFar the total downloaded so far (in bytes)
     */
    public void setDownloadedSoFar(long downloadedSoFar) {
        this.downloadedSoFar = downloadedSoFar;
    }

    /**
     * Get the total download length (as per the Content-Length http header) in bytes
     *
     * @return The total download length (as per the Content-Length http header) in bytes
     */
    public long getDownloadLength() {
        return downloadLength;
    }

    /**
     * Set the total download length (as per the Content-Length http header) in bytes
     *
     * @param downloadLength the total download length (as per the Content-Length http header) in bytes
     */
    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    /**
     * Get the current speed of this download (in bytes per second). This is calculated by DownloadTask
     * and is normally a 5second moving average.
     *
     * @return the current speed of this download (in bytes per second)
     */
    public long getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Set the current speed of this download (in bytes per second). This is calculated by DownloadTask
     * and is normally a 5second moving average.
     *
     * @param currentSpeed the current speed of this download (in bytes per second)
     */
    public void setCurrentSpeed(long currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    /**
     * Get the current status of this DownloadJobItem (as per the NetworkTask.STATUS flags)
     * @return the current status of this DownloadJobItem (as per the NetworkTask.STATUS flags)
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the current status of this DownloadJobItem (as per the NetworkTask.STATUS flags)
     *
     * @param status the current status of this DownloadJobItem (as per the NetworkTask.STATUS flags)
     */
    public void setStatus(int status) {
        this.status = status;
    }


    /**
     * Get the time (in ms) this downloadJobItem started
     *
     * @return the time (in ms) this downloadJobItem started
     */
    public long getTimeStarted() {
        return timeStarted;
    }

    /**
     * Set the time (in ms) this downloadJobItem started
     *
     * @param timeStarted the time (in ms) this downloadJobItem started
     */
    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    /**
     * Get the time (in ms) this DownloadJobItem finished, or 0 if this item has not yet finished.
     *
     * @return the time (in ms) this DownloadJobItem finished, or 0 if this item has not yet finished.
     */
    public long getTimeFinished() {
        return timeFinished;
    }

    /**
     * Set the time (in ms) this DownloadJobItem finished, or 0 if this item has not yet finished.
     *
     * @param timeFinished the time (in ms) this DownloadJobItem finished, or 0 if this item has not yet finished.
     */
    public void setTimeFinished(long timeFinished) {
        this.timeFinished = timeFinished;
    }

    /**
     * Get the destination file to which this download job will be written
     *
     * @return the destination file to which this download job will be written
     */
    public String getDestinationFile() {
        return destinationFile;
    }

    /**
     * Set the destination file to which this download job will be written
     *
     * @param destinationFile the destination file to which this download job will be written
     */
    public void setDestinationFile(String destinationFile) {
        this.destinationFile = destinationFile;
    }

    /**
     * Get the number of attempts that have been completed to download this item. This is exclusive
     * of the current attempt running, if any.
     *
     * @return The number of attempts that have been completed to download this item
     */
    public int getNumAttempts() {
        return numAttempts;
    }

    /**
     Get the number of attempts that have been completed to download this item. This is exclusive
     * of the current attempt running, if any.
     *
     * @param numAttempts
     */
    public void setNumAttempts(int numAttempts) {
        this.numAttempts = numAttempts;
    }
}
