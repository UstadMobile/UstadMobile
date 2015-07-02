/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.impl;

/**
 *
 * @author mike
 */
public interface UMTransferJob {
    
    public void start();
    
    public void addProgresListener(UMProgressListener listener);
    
    public int getBytesDownloadedCount();
    
    public int getTotalSize();
    
    public boolean isFinished();
    
}
