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
package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSItem;

/**
 *
 * @author varuna
 */
public interface CatalogView extends UstadView{
    
    public int CMD_DOWNLOADALL = 0;
    
    public int CMD_DELETEITEM = 1;
    
    public int CMD_DOWNLOADENTRY = 2;
    
    /**
     * Set the controller linked to this view: the feed is referenced
     * via controller.
     * @param controller 
     */
    public void setController(CatalogController controller);
    
    /**
     * Get the controller linked to this view: the feed is reference via the
     * controller
     * 
     * @return Controller connected with this view
     */
    public CatalogController getController();
    
    /**
     * Show a confirm/cancel dialog to the user (e.g. delete item? download item?)
     * 
     * @param title
     * @param text 
     * @param commandId The type of event (the view should then know what to trigger next)
     */
    public void showConfirmDialog(String title, String message, String positiveChoice, String negativeChoice, final int commandId);
    
    
    /**
     * Set the status of a given entry (e.g. acquired, not acquired, in progress) 
     * 
     * @param entryId Id of the entry to update
     * @param status Entry status flag ( CatalogController.STATUS_ACQUIRED, STATUS_ACQUISITION_IN_PROGRESS, or STATUS_NOT_ACQUIRED)
     */
    public void setEntryStatus(String entryId, int status);
    
    /**
     * Set the thumbnail for the given entry.  Loading the thumbnails is threaded
     * and this method should handle putting calls onto the UI thread as required
     * 
     * @param entryId the entry id for the entry to s
     * @param iconFileURI A file with an icon image
     */
    public void setEntrythumbnail(String entryId, String iconFileURI);
    
    /**
     * Update the download all progress bar at the bottom
     * 
     * @param loaded amount loaded (e.g. in total bytes downloaded) 
     * @param total total length total length of download, or -1 if not yet known
     */
    public void updateDownloadAllProgress(int loaded, int total);
    
    /**
     * Set whether a progress bar is visible on a given catalog (acquisition) entry
     * @param entryId Entry to update
     * @param visible true for visible, false otherwise
     */
    public void setDownloadEntryProgressVisible(String entryId, boolean visible);
    
    /**
     * Update the progress bar showing the progress on a given download entry
     * 
     * @param entryId The entry id to be updated
     * @param loaded the amount loaded 
     * @param total total amount to load (or -1 if not yet known)
     */
    public void updateDownloadEntryProgress(String entryId, int loaded, int total);
    
    /**
     * Get the entries that have been selected by the user (e.g. by long press)
     * 
     * @return Array of entries selected by the user
     */
    public UstadJSOPDSEntry[] getSelectedEntries();
    
    /**
     * Set the entries that are to be marked as selected
     * 
     * @param entries Array of entries to be marked as selected
     */
    public void setSelectedEntries(UstadJSOPDSEntry[] entries);
    
}
