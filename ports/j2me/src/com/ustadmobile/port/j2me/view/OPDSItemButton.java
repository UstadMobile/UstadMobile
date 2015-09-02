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

 */package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 *
 * @author varuna
 */
public class OPDSItemButton extends Button {

    private final int progressHeight = 5;
    private final byte progressTransparency = (byte)80;
    private int progressPercentage;
    
    private int acquisitionStatus = -1;
    
    /**
     * Share a static reference to the image; but let go when not needed
     */
    private static WeakReference checkImgRef;
    
    /**
     * Check image shown when entry is acquired
     */
    private Image checkImg;        
    
    private UstadJSOPDSEntry entry;
    
    /**
     * Whether or not the progress bar is visible
     */
    private boolean progressBarVisible;
    
    
    public OPDSItemButton(UstadJSOPDSEntry entry){
        this(new Command(entry.title), entry);
    }
    
    public OPDSItemButton(String title) {
        this(new Command(title), null);
    }
    
    public OPDSItemButton(Command cmd, UstadJSOPDSEntry entry){
        this.setCommand(cmd);
        this.entry = entry;
        
        progressBarVisible = false;
        
        Object checkVal = null;
        if(checkImgRef != null) {
            checkVal = checkImgRef.get();
        }
        
        if(checkVal != null) {
            checkImg = (Image)checkVal;
        }else {
            try {
                checkImg = Image.createImage("/res/img/check.png");
                checkImgRef = new WeakReference(checkImg);
            }catch(IOException e) {
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.CRITICAL, 330, null, e);
            }
        }
    }

    public UstadJSOPDSEntry getEntry() {
        return entry;
    }

    public void setEntry(UstadJSOPDSEntry entry) {
        this.entry = entry;
    }
    
    
    
    /**
     * Get the acquisition status of the item - returns a CatalogEntryInfo status flag
     * @see CatalogEntryInfo
     * 
     * @return Flag representing the acquisition status of this item
     */
    public int getAcquisitionStatus() {
        return acquisitionStatus;
    }

    /**
     * Set the acquisition status of this item - takes a CatalogEntryInfo status flag
     * 
     * Will display a checkbox on the item for the user if it's been acquired.
     * 
     * @see CatalogEntryInfo
     * 
     * @param acquisitionStatus The acquisition status this should have
     */
    public void setAcquisitionStatus(int acquisitionStatus) {
        if(this.acquisitionStatus != acquisitionStatus) {
            this.acquisitionStatus = acquisitionStatus;
            repaint();
        }
    }

    /**
     * Whether or not the progress bar is visible for this entry
     * 
     * @return true if visible; false otherwise
     */
    public synchronized boolean isProgressBarVisible() {
        return progressBarVisible;
    }

    /**
     * Set whether or not the progress bar is visible for this entry
     * 
     * @param progressBarVisible true for visible; false otherwise
     */
    public synchronized void setProgressBarVisible(boolean progressBarVisible) {
        if(progressBarVisible != this.progressBarVisible) {
            this.progressBarVisible = progressBarVisible;
            this.repaint();
        }
    }
    
    

    public void paint(Graphics g) {
        super.paint(g); 

        if(isProgressBarVisible()) {
            int x = this.getWidth();
            int y = this.getHeight();
            int yAxis = y - progressHeight;

            String ourText = getText();

            float percentageWidth = ((float)x/100) * progressPercentage;

            g.setColor(0);
            g.fillRect(getX(), getY() + (y - progressHeight), (int)percentageWidth, 
                    progressHeight, progressTransparency);
        }
        
        
        if(getAcquisitionStatus()== CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
            g.drawImage(checkImg, getX(), getY() + (getHeight()- checkImg.getHeight()));
        }
    }
    
    
    public void updateProgress(int progressPercentage){
        this.progressPercentage = progressPercentage;
        this.repaint();
    }
    
    
}
