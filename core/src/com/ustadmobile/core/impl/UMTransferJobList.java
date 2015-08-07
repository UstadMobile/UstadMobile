/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.impl;

import java.util.Vector;

/**
 * Transfer job lists runs an array of transfer jobs; synchronously one 
 * after the other
 * 
 * @author mike
 */
public class UMTransferJobList implements UMTransferJob, UMProgressListener{

    private UMTransferJob[] jobList;
    
    private int currentItem;
    
    private int totalSizeCombined = -1;
    
    private Vector progressListeners;
    
    boolean sizeKnown = false;
    
    private boolean isFinished;
    
    /**
     * Runnable item that will be triggered before start
     */
    private Runnable runBeforeStartJob;
    
    
    /**
     * Runnable item that will run after job finish, before firing the finish event
     */
    private Runnable runAfterFinishJob;
    
    public UMTransferJobList(UMTransferJob[] jobList) {
        this.jobList = jobList;
        this.currentItem = -1;
        progressListeners = new Vector();
        isFinished = false;
    }
    
    @Override
    public void start() {
        if(currentItem != -1) {
            throw new RuntimeException("already started");
        }
        
        if(runBeforeStartJob != null) {
            runBeforeStartJob.run();
        }
        
        //Make sure that we know the size of the whole
        getTotalSize();
        if(this.jobList.length > 0) {
            currentItem = 0;
            this.jobList[0].addProgresListener(this);
            this.jobList[0].start();
        }
    }
    
    /**
     * Add something to run when the job is started.  The Runnable will be called
     * when start() is called before anything actually starts downloading
     * 
     * @param job The runnable to run in the start method before file downloads start
     */
    public void setRunBeforeStartJob(Runnable job) {
        this.runBeforeStartJob = job;
    }
    
    /**
     * Add something to run after all the downloads complete.  This runnable
     * will be called when all downloads have finished but before fireComplete
     * sends the event out.
     * 
     * @param job The runnable to run once all downloads have completed.
     */
    public void setRunAfterFinishJob(Runnable job) {
        this.runAfterFinishJob = job;
    }

    @Override
    public void addProgresListener(UMProgressListener listener) {
        progressListeners.add(listener);
    }

    @Override
    public int getBytesDownloadedCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getTotalSize() {
        // go over all the component downloads to find the total size
        int numUncertain = 0;
        if(totalSizeCombined == -1) {
            totalSizeCombined = 0;
            int thisJobSize = -1;
            for(int i = 0; i < jobList.length; i++) {
                thisJobSize = jobList[i].getTotalSize();
                if(thisJobSize > 0) {
                    totalSizeCombined += thisJobSize;
                }else {
                    numUncertain++;
                }
            }
        }
        
        this.sizeKnown = (numUncertain == 0);
        
        return totalSizeCombined;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void progressUpdated(UMProgressEvent evt) {
        if(evt.getEvtType() == UMProgressEvent.TYPE_COMPLETE) {
            // time to start the next download
            if(currentItem < this.jobList.length - 1) {
                currentItem++;
                this.jobList[currentItem].addProgresListener(this);
                this.jobList[currentItem].start();
            }else {
                isFinished = true;
                if(runAfterFinishJob != null) {
                    runAfterFinishJob.run();
                }
                
                fireComplete();
            }
        }
    }
    
    protected void fireComplete() {
        UMProgressEvent evt = new UMProgressEvent(UMProgressEvent.TYPE_COMPLETE, 
            totalSizeCombined, totalSizeCombined, 200);
        for(int i = 0; i < progressListeners.size(); i++) {
            ((UMProgressListener)progressListeners.get(i)).progressUpdated(evt);
        }
    }
    
}
