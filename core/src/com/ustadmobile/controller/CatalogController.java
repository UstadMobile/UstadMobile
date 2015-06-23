/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.controller;

import com.ustadmobile.impl.UMTransferJob;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.model.CatalogModel;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import com.ustadmobile.opds.UstadJSOPDSItem;
import com.ustadmobile.view.CatalogView;

/**
 *
 * @author varuna
 */
public class CatalogController implements UstadController{
    
    public static final int STATUS_ACQUIRED = 0;
    
    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;
    
    public static final int STATUS_NOT_ACQUIRED = 2;
    
    
    private CatalogView view;
    
    private CatalogModel model;
    
    public CatalogController() {
        
    }
    
    //methods go here..
    
    public void handleClickRefresh() {
        
    }
    
    public void show() {
        this.view = (CatalogView)
                UstadMobileSystemImpl.getInstance().makeView("Catalog");
    }
    
    public void hide() {
        
    }
    
    /**
     * Construct a CatalogController for the OPDS feed at the given URL
     * 
     * @param url
     * @param impl
     * @return 
     */
    public static CatalogController makeControllerByURL(String url, UstadMobileSystemImpl impl) {
        
        return null;
    }
    
    /**
     * Make a CatalogController for the user's default OPDS catalog
     * 
     * @param impl system implementation to be used
     * 
     * @return CatalogController representing the default catalog for the active user
     */
    public static CatalogController makeUserCatalog(UstadMobileSystemImpl impl) {
        
        return null;
    }
    
    /**
     * Make a catalog representing the files that are now in the shared and user
     * directories
     * 
     * @return CatalogController representing files on the device
     */
    public static CatalogController makeDeviceCatalog() {
        
        return null;
    }
    
    /**
     * Triggered by the view when the user has selected the download all button
     * for this feed
     * 
     */
    public void handleClickDownloadAll() {
        
    }
    
    /**
     * Triggered when the user confirms that they wish to download all entries
     * for this feed
     * 
     */
    public void handleConfirmDownloadAll() {
        
    }
    
    /**
     * Triggered when the user requests the context menu for a container in the feed
     * (applies only to acquisition feeds)
     * 
     * @param item 
     */
    public void handleRequestContainerContextMenu(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Triggered when the user selects to delete a container in the feed
     * This triggers deleting the locally downloaded file (if it exists)
     * 
     * @param item OPDS item selected
     */
    public void handleClickDeleteEntry(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Triggered when the user selects a given container from an acquisition feed
     * 
     * @param item 
     */
    public void handleClickContainerEntry(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Triggered when the user confirms that they wish to download a given container
     * 
     * @param item 
     */
    public void handleConfirmDownloadContainer(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Triggered when the user selects a sub-feed from a navigation feed
     * 
     * @param item 
     */
    public void handleClickFeedEntry(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Get an OPDS catalog by URL
     * 
     * @param url
     * @param httpUsername
     * @param httpPassword
     * @param cacheEnabled
     * @return 
     */
    public static CatalogController getCatalogByURL(String url, String httpUsername, String httpPassword, boolean cacheEnabled) {
        
        return null;
    }
    
    /**
     * Get the Feed ID of a catalog according to a URL it is known to have been
     * downloaded from
     * 
     * @param url The URL of where the catalog was downloaded from
     * @return The OPDS ID of the entry from that location
     */
    public String getCatalogIDByURL(String url) {
        return null;
    }
    
    /**
     * Get a list of URLs from which the given OPDS Feed ID has been downloaded
     * @param itemID The ID of the item to check for download locations
     * 
     * @return Array of strings representing where the id could have been downloaded from
     */
    public String[] getCatalogURLSByID(String itemID) {
        
        return null;
    }
    
    /**
     * Make a transfer job that can download the entire contents of the acquisition feed
     * 
     * @param feed
     * @param httpUsername
     * @param httpPassword
     * @return 
     */
    public static UMTransferJob downloadEntireAcquisitionFeed(UstadJSOPDSFeed feed, String httpUsername, String httpPassword) {
        return null;
    }
    
    protected static String getFileNameForOPDSFeedId(String feedId, String user) {
        
        return null;
    }
    
    /**
     * Cache the given catalog so it can be retrieved when offline
     * 
     * @param catalog 
     * @param ownerUser the user that owns the download, or null if this is for the shared directory
     */
    public static void cacheCatalog(UstadJSOPDSFeed catalog, String ownerUser) {
        
    }
    
    /**
     * For any acquisition feed that we have - make a catalog of what we have locally
     * that points at the actual contain entries on the disk
     */
    public static CatalogController generateLocalCatalog(UstadJSOPDSFeed catalog) {
        return null;
    }
    
    /**
      * Get a cached copy of a given catalog according to it's ID
      */
    public static CatalogController getCachedCatalogByID(String catalogID, String username) {
        return null;
    }
    
    public static CatalogController getCachedCatalogByURL(String url, String username) {
        return null;
    }
    
    /**
     * 
     * CatalogController.scanDir logic:
     *
     *  1. Go through all .opds files - load them and make a dictionary in the form of 
     *     catalogid -> opds object.  These are acquisition feeds (courses)
     *  
     * 2. Make another new empty OPDS navigation feed - looseContainers
     * 
     * 3. Go through all .epub files - are they present in any of the catalogs (check using ID)?
     *   No: Add them to the looseContainers object
     *   Yes: Do nothing
     *
     * 4. Make a new OPDS navigation feed with an entry for each acquisition feed
     * 
     * @param dirname URI to the directory to scan
     * @return Feed representing the contents of the directory
     */ 
    public UstadJSOPDSFeed scanDir(String dirname) {
        return null;
    }
    
    /**
     * Register that a download has started
     * @param itemDownloaded
     * @param destURI 
     */
    public static void registerEntryDownload(UstadJSOPDSItem itemDownloaded, String destURI) {
        
    }
    
    /**
     * Register that a download is finished
     * 
     * @param item
     */
    public static void unregisterEntryDownload(UstadJSOPDSItem item) {
        
    }
    
    /**
     * Fetch and download the given container, save required information about it to
     * disk
     * 
     * 1. Download the given srcURL to disk (see options.destdir and destname) to 
     * override destination
     * 
     * 2. Generate a .<filename>.container file that will contains an OPDS acquisition feed
     * with one entry in it.
     * 
     * 3. Update the localstorage map of EntryID -> containerURI 
     */
    public static UMTransferJob acquireCatalogEntries(UstadJSOPDSItem[] entries, String httpUsername, String httpPassword) {
        return null;
    }
    
    /**
     * Delete the given entry
     * 
     * @param entryID
     * @param user 
     */
    public static void removeEntry(String entryID, String user) {
        
    }
    
    public static int getAcquisitionSTatusByEntryID(String entryID, String user) {
        return -1;
    }
    
}
