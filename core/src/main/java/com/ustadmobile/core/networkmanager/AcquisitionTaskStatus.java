package com.ustadmobile.core.networkmanager;

/**
 * Created by mike on 5/30/17.
 */
public interface AcquisitionTaskStatus {

    /**
     * Get the total downloaded so far in bytes
     *
     * @return the total downloaded so far in bytes
     */
    long getDownloadedSoFar();

    /**
     * The total size of the download (if known) in bytes
     *
     * @return The total size of the download (if known) in bytes: -1 if unknown
     */
    long getTotalSize();

    /**
     * Gets the current status of the acquisition (e.g. waiting, retrying, in progress etc). As per
     * UstadMobileSystemImpl.DLSTATUS_ flags
     *
     * @return the status of the acquisition as per UstadMobileSystemImpl.DLSTATUS_ flags
     */
    int getStatus();

    /**
     * Gets the current download speed, kept as a moving average, in bytes per second
     *
     * @return Current download speed in bytes per second
     */
    long getCurrentSpeed();
}
