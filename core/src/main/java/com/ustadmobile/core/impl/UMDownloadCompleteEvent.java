/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.impl;

/**
 *
 * @author mike
 */
public class UMDownloadCompleteEvent {
    private String downloadID;
    
    private int[] status;
    
    public UMDownloadCompleteEvent(String downloadID, int[] status) {
        this.downloadID = downloadID;
        this.status = status;
    }
    
    /**
     * The system download ID
     * 
     * @return The system download ID
     */
    public String getDownloadID() {
        return downloadID;
    }
    
    /**
     * The status of the download upon completion
     */
    public int[] getStatus() {
        return status;
    }
    
}
