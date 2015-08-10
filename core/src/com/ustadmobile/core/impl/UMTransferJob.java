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

/**
 *
 * @author mike
 */
public interface UMTransferJob {
    
    /**
     * Start the transfer job.  The start method must be async and must
     * return immediately.  The job itself must run in another thread.
     */
    public void start();
    
    /**
     * Add a progress listener that will receive events as the job completes
     * 
     * @param listener 
     */
    public void addProgressListener(UMProgressListener listener);
    
    /**
     * Return the total number of bytes processed (e.g. downloaded) thus far
     * 
     * @return Number of bytes processed so far
     */
    public int getBytesDownloadedCount();
    
    /**
     * Get the total size of the job in bytes or -1 if unknown
     * 
     * @return Total size of job in bytes
     */
    public int getTotalSize();
    
    /**
     * Determine if the job is completed yet or not
     * 
     * @return True if finished, false otherwise
     */
    public boolean isFinished();
    
    /**
     * Get the source of the job (e.g. url being downloaded from) if applicable.
     * If a job is a list or does not have a direct source; this can return
     * null.  If a job has a direct source it should be returned
     * 
     * @return 
     */
    public String getSource();
    
    /**
     * Get the destination of the job (e.g. the file path being saved to) if
     * applicable.  If a job is a list or does not have a direct destination
     * this can return null.  If a job has a direct full path destination it
     * should be returned
     * 
     * @return 
     */
    public String getDestination();
    
}
