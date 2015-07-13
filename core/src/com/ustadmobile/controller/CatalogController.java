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
package com.ustadmobile.controller;

import com.ustadmobile.app.Base64;
import com.ustadmobile.impl.HTTPResult;
import com.ustadmobile.impl.UMTransferJob;
import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.model.CatalogModel;
import com.ustadmobile.opds.UstadJSOPDSFeed;
import com.ustadmobile.opds.UstadJSOPDSItem;
import com.ustadmobile.view.CatalogView;
import com.ustadmobile.view.ViewFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.pki.UserCredentialManager;
import org.kxml2.io.KXmlParser;

/**
 *
 * @author varuna
 */
public class CatalogController implements UstadController{
    
    public static final int STATUS_ACQUIRED = 0;
    
    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;
    
    public static final int STATUS_NOT_ACQUIRED = 2;
    
    //The View (J2ME or Android)
    private CatalogView view;
    
    //this is where the feed (and its entries) live.
    private CatalogModel model;
    
    public CatalogController() {
        
    }
    public CatalogController(CatalogModel model){
        this.model=model;
    }
    
    /**
     * Get the catalog model that corresponds to this controller.  The model
     * contains the OPDS feed
     * 
     * @return CatalogModel corresponding to this feed
     */
    public CatalogModel getModel() {
        return this.model;
    }
    
    //methods go here..
    
    public void handleClickRefresh() {
        
    }
    
    //shows the view
    public void show() {
        this.view = ViewFactory.makeCatalogView();
        this.view.setController(this);
        this.view.show();
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
        
        //Create ODPSn feed
        String opdsEndpoint = 
                UstadMobileSystemImpl.getInstance().getAppPref("opds");
        if (url == null | url.equals("")){
            url = opdsEndpoint;
        }
        
        Hashtable requestHeaders = new Hashtable();
        String username = 
                UstadMobileSystemImpl.getInstance().getUserPref("username", "");
        String password = 
                UstadMobileSystemImpl.getInstance().getUserPref("password", "");
        if (username == null || username.equals("") || username == ""){
            return null;
        }
        String encodedUserAndPass="Basic "+ Base64.encode(username,
                    password);
        requestHeaders.put("Authorization", encodedUserAndPass);
        HTTPResult httpResult = null;
        httpResult = UstadMobileSystemImpl.getInstance().makeRequest(url,
                requestHeaders, null, "GET");
        
        InputStream is = null;
        
        try{
            KXmlParser parser = new KXmlParser();
            String response = new String(httpResult.getResponse());
            is = new ByteArrayInputStream(httpResult.getResponse());
            parser.setInput(is, "utf-8");

            UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(parser);
 
            CatalogModel feedModel = new CatalogModel(feed);
            
            CatalogController catalogController = new CatalogController(feedModel);
            return catalogController;
                    
        }catch(Exception e){
            e.printStackTrace();
        } finally{
            if (is != null){
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
