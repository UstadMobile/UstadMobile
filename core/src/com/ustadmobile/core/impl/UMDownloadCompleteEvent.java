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
    private long downloadID;
    
    public UMDownloadCompleteEvent(long downloadID) {
        this.downloadID = downloadID;
    }
    
    public long getDownloadID() {
        return downloadID;
    }
    
}
