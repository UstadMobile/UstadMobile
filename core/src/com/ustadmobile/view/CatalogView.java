/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

//import com.ustadmobile.controller.LoginController;
import com.ustadmobile.controller.CatalogController;
import com.ustadmobile.opds.UstadJSOPDSItem;

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
     * Show a confirm/cancel dialog to the user (e.g. delete item? download item?)
     * 
     * @param title
     * @param text 
     * @param commandId The type of event (the view should then know what to trigger next)
     */
    public void showDialog(String title, String text, int commandId);
    
    /**
     * Show the context menu for a container (e.g. more info, delete this item, etc)
     * 
     * @param item 
     */
    public void showContainerContextMenu(UstadJSOPDSItem item);
    
    /**
     * Hide the container context menu if one is showing
     */
    public void hideContainerContextMenu();
    
    /**
     * Set the status of a given entry (e.g. acquired, not acquired, in progress) 
     * 
     * @param entryId Id of the entry to update
     * @param status Entry status flag ( CatalogController.STATUS_ACQUIRED, STATUS_ACQUISITION_IN_PROGRESS, or STATUS_NOT_ACQUIRED)
     */
    public void setEntryStatus(String entryId, int status);
    
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
    
    
        
}
