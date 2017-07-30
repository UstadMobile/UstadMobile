/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.networkmanager;

/**
 *
 * @author mike
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
}
