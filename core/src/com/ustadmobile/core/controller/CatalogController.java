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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.app.Base64;
import com.ustadmobile.core.impl.UMProgressEvent;
import com.ustadmobile.core.impl.UMProgressListener;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UMTransferJobList;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ViewFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * CatalogController manages OPDS Feeds and controls the view component
 * showing catalogs to the user
 * 
 * Resources (Containers and OPDS Feeds themselves) can be either in a user
 * specific area or in a public shared area.  
 * 
 * If USER_RESOURCE bit is set look for the resource in the user specific area
 * If SHARED_RESOURCE bit is set - look in shared resources if not already 
 * found in users' resources.  USER_RESOURCE takes precedence; i.e. if the
 * resource is found in the user area it is served from there; even if there
 * is also the same resource in the shared area.
 * 
 * To ask a method to look in both locations - use the bitwise OR e.g.
 * int mode =  USER_RESOURCE | SHARED_RESOURCE
 * 
 * Most methods have a resourceMode (which can be included in general flags)
 * 
 * @author Varuna Singh <varuna@ustadmobile.com>
 * @author Mike Dawson <mike@ustadmobile.com>
 */
public class CatalogController implements UstadController, UMProgressListener {
    
    public static final int STATUS_ACQUIRED = 0;
    
    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;
    
    public static final int STATUS_NOT_ACQUIRED = 2;
    
    
    /**
     * Enable retrieving resource from cache
     */
    public static final int CACHE_ENABLED= 1;
    
    /**
     * Save/retrieve resource from user specific directory
     */
    public static final int USER_RESOURCE = 2;
    
    
    /**
     * Save/retrieve resource from shared directory
     */
    public static final int SHARED_RESOURCE = 4;
    
    
    
    public static final int ENTRY_ACQUISITION_STATUS = 0;
    
    public static final int ENTRY_URLs = 1;
    
    public static final int ENTRY_FILEURI = 2;
    
    public static final int ENTRY_MIMETYPE = 3;
    
    
    /**
     * Command ID representing a user wishing to download a single entry 
     * (e.g. after just tapping it)
     */
    public static final int CMD_DOWNLOADENTRY = 0;
    
    /**
     * Command ID representing a user wishing to delete an entry or set
     * of entries
     */
    public static final int CMD_DELETEENTRY = 1;
    
    
    private UstadJSOPDSEntry[] selectedEntries;
    
    private Vector activeTransferJobs;
    
    //The View (J2ME or Android)
    private CatalogView view;
    
    //this is where the feed (and its entries) live.
    private CatalogModel model;
    
    public static String[] catalogMenuOpts = new String[]{"My Catalogs",
        "My Courses", "Logout"};
    
    
    public CatalogController() {
        
    }
    public CatalogController(CatalogModel model){
        this.model=model;
    }
    
    /**
     * Set the items currently selected now by the user (this is called by the
     * corresponding view object in response to user interaction)
     * 
     * @param entries Entry objects currently selected by the user
     */
    public void setSelectedEntries(UstadJSOPDSEntry[] entries) {
        this.selectedEntries = entries;
    }
    
    /**
     * Get the list of items that are currently marked as selected by the user
     * from this view
     * 
     * @return Array of UstadJSOPDSEntry of those selected by the user
     */
    public UstadJSOPDSEntry[] getSelectedEntries() {
        return this.selectedEntries;
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
        this.view.setMenuOptions(catalogMenuOpts);
        
        UstadJSOPDSFeed feed = this.getModel().opdsFeed;
        if(feed.isAcquisitionFeed()) {
            //go through and set the acquisition status
            for(int i = 0; i < feed.entries.length; i++) {
                CatalogEntryInfo info = getEntryInfo(feed.entries[i].id, 
                    USER_RESOURCE | SHARED_RESOURCE);
                if(info != null) {
                    this.view.setEntryStatus(feed.entries[i].id, 
                        info.acquisitionStatus);
                }
                
            }
        }
        
        this.view.show();
    }
    
    public CatalogView getView() {
        return this.view;
    }
    
    public void hide() {
        
    }
    
    /**
     * Construct a CatalogController for the OPDS feed at the given URL
     * 
     * @param url the URL of the OPDS feed
     * @param resourceMode: SHARED_RESOURCE to keep in shared cache: USER_RESOURCE to keep in user cache
     * @param httpUser: the HTTP username to use for authentication
     * @param httpPassword:or the HTTP password to use for authentication
     * @param impl System implementation to use
     * @return 
     */
    public static CatalogController makeControllerByURL(String url, UstadMobileSystemImpl impl, int resourceMode, String httpUser, String httpPassword, int flags) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = CatalogController.getCatalogByURL(url, resourceMode, 
            httpUser, httpPassword, flags);
        
        CatalogController result = new CatalogController(
            new CatalogModel(opdsFeed));
        
        return result;
    }
    
    /**
     * Make a CatalogController for the user's default OPDS catalog
     * 
     * @param impl system implementation to be used
     * 
     * @return CatalogController representing the default catalog for the active user
     */
    public static CatalogController makeUserCatalog(UstadMobileSystemImpl impl) throws IOException, XmlPullParserException{
        String opdsServerURL = impl.getUserPref("opds_server_primary", 
            UstadMobileDefaults.DEFAULT_OPDS_SERVER);
        
        String activeUser = impl.getActiveUser();
        String activeUserAuth = impl.getActiveUserAuth();
        return CatalogController.makeControllerByURL(opdsServerURL, impl, 
            USER_RESOURCE, activeUser, activeUserAuth, CACHE_ENABLED);
        
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
        selectedEntries = getModel().opdsFeed.entries;
        view.showConfirmDialog("Download?", "Download all " 
            + selectedEntries.length + " entries ?", "OK", "Cancel", 
            CMD_DOWNLOADENTRY);
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
    public void handleClickDeleteEntries(UstadJSOPDSEntry[] entries) {
        selectedEntries = entries;
        this.view.showConfirmDialog("Delete?", "Delete " + entries.length + 
                " entries from device?", "Delete", "Cancel", CMD_DELETEENTRY);
    }
    
    public void handleConfirmDeleteEntries() {
        int numDeleted = 0;
        for(int i = 0; i < selectedEntries.length; i++) {
            CatalogController.removeEntry(selectedEntries[i].id, 
                USER_RESOURCE | SHARED_RESOURCE);
            this.view.setEntryStatus(selectedEntries[i].id, STATUS_NOT_ACQUIRED);
        }
        this.view.setSelectedEntries(new UstadJSOPDSEntry[0]);
    }
    
    /**
     * Triggered when the user selects an entry from the catalog.  This could
     * be another OPDS catalog Feed to display or it could be a container
     * entry.
     * 
     * @param item 
     */
    public void handleClickEntry(final UstadJSOPDSEntry entry) {
        if(!entry.parentFeed.isAcquisitionFeed()) {
            //we are loading another opds catalog
            Vector entryLinks = entry.getLinks(null, UstadJSOPDSItem.TYPE_ATOMFEED, 
                true, true);
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            if(entryLinks.size() > 0) {
                String[] firstLink = (String[])entryLinks.elementAt(0);
                final String url = UMFileUtil.resolveLink(entry.parentFeed.href, 
                    firstLink[UstadJSOPDSItem.LINK_HREF]);
                
                Thread bgThread = new Thread() {
                    public void run() {
                        int resourceMode = USER_RESOURCE;
                        int fetchFlags = CACHE_ENABLED;
                        String httpUsername = impl.getActiveUser();
                        String httpPassword = impl.getActiveUserAuth();
                        try {
                            CatalogController newController = CatalogController.makeControllerByURL(url, impl, resourceMode, 
                                httpUsername, httpPassword, fetchFlags);
                            newController.show();
                            impl.getAppView().dismissProgressDialog();
                        }catch(Exception e) {
                            e.printStackTrace();
                            impl.getAppView().dismissProgressDialog();
                        }
                    }
                };
                impl.getAppView().showProgressDialog("Loading");
                bgThread.start();
            }
        }else {
            CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id, 
                    SHARED_RESOURCE | USER_RESOURCE);
            if(entryInfo != null && entryInfo.acquisitionStatus == STATUS_ACQUIRED) {
                String openPath = UstadMobileSystemImpl.getInstance().openContainer(
                    entry, entryInfo.fileURI, entryInfo.mimeType);
                
                ContainerController catalogCtrl = 
                    ContainerController.makeFromEntry(entry, openPath, 
                        entryInfo.fileURI, entryInfo.mimeType);
                catalogCtrl.show();
                
                System.out.println("Opened to : " + openPath);
            }else {
                String title = entry.title;
                selectedEntries = new UstadJSOPDSEntry[]{entry};
                view.showConfirmDialog("Download?", "Download " + title + "?", "OK", 
                    "Cancel", CMD_DOWNLOADENTRY);
            }
        }
    }
    
    /**
     * This should be called when the user has selected entries and requested
     * them to be downloaded
     * 
     * @param entries The entries selected by the user to download
     */
    public void handleClickDownloadEntries(final UstadJSOPDSEntry[] entries) {
        selectedEntries = entries;
        view.showConfirmDialog("Download?", "Download " + entries.length +
                " entries ?", "OK", "Cancel", CMD_DOWNLOADENTRY);
    }
    
    /**
     * Called when the user makes a choice on the confirm dialog
     * 
     * @param userResponse true if the user selected the positive option (e.g. OK)
     * , false otherwise (e.g. Cancel)
     * @param commandId The commandId that was provided when the dialog was shown
     */
    public void handleConfirmDialogClick(boolean userResponse, int commandId) {
        if(!userResponse) {
            return;
        }
        
        switch(commandId) {
            case CMD_DOWNLOADENTRY:
                this.handleConfirmDownloadEntries(selectedEntries);
                break;
                
            case CMD_DELETEENTRY:
                this.handleConfirmDeleteEntries();
                break;
        }
    }
    
    
    /**
     * Triggered when the user confirms that they wish to download a given set 
     * of entries
     * 
     * @param item 
     */
    public void handleConfirmDownloadEntries(UstadJSOPDSEntry[] entries) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UMTransferJobList transferJob;
        transferJob = CatalogController.acquireCatalogEntries(entries, impl.getActiveUser(), 
                impl.getActiveUserAuth(), SHARED_RESOURCE, CACHE_ENABLED);
        //TODO: Add event listeners to update progress etc.
        if(activeTransferJobs == null) {
            activeTransferJobs = new Vector();
        }
        transferJob.addProgressListener(this);
        setViewEntryProgressVisible(entries, true);
        transferJob.start();
        activeTransferJobs.addElement(transferJob);
        this.view.setSelectedEntries(new UstadJSOPDSEntry[0]);
    }
    
    /**
     * Called when the user selects an item from the menu (e.g. Drawer, J2ME options
     * menu etc)
     * 
     * @param index Index of the item clicked corresponding with the menuitem string array
     */
    public void handleClickMenuItem(int index) {
        System.out.println("You click: " + index);
    }
    
    /**
     * If the view is available (e.g. not null) set all the given entries 
     * progress visible to be visible/not visible
     * 
     * @param entries Array of entries this should effect
     * @param visible Whether or not the progress bar for these entries should be visible
     */
    private void setViewEntryProgressVisible(UstadJSOPDSEntry[] entries, boolean visible) {
        if(this.view == null) {
            return;
        }
        
        for(int i = 0; i < entries.length; i++){ 
                this.view.setDownloadEntryProgressVisible(entries[i].id, visible);
        }
    }
    
    /**
     * Make a Hashtable with http authorization headers if username and password
     * are not null.  Otherwise return empty hashtable.
     * 
     * TODO: If empty; let's use null instead
     * 
     * @param username HTTP username
     * @param password HTTP password
     * @return Hashtable with Authorization and Base64 encoded username/password
     */
    public static Hashtable makeAuthHeaders(String username, String password) {
        Hashtable headers = new Hashtable();
        if(username != null && password != null) {
            headers.put("Authorization", "Basic "+ Base64.encode(username,password));
        }
        return headers;
    }
    
    /**
     * Get an OPDS catalog by URL
     * 
     * @param url
     * @param resourceMode USER_RESOURCE or SHARED_RESOURCE - where it will be saved
     * @param httpUsername
     * @param httpPassword
     * @param flags boolean flags inc. for cache retrieval 
     *  - set USER_RESOURCE to retrieve catalogs from active user cache.
     *  - set SHARED_RESOURCE to retrieve catalogs from shared cache as well.
     * @return 
     */
    public static UstadJSOPDSFeed getCatalogByURL(String url, int resourceMode, String httpUsername, String httpPassword, int flags) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = null;
                
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable headers = makeAuthHeaders(httpUsername, httpPassword);
        
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        byte[] opdsContents = impl.readURLToString(url, headers).getResponse();
        parser.setInput(
            new ByteArrayInputStream(opdsContents), 
            "UTF-8");
        opdsFeed = UstadJSOPDSFeed.loadFromXML(parser);
        opdsFeed.href = url;
        CatalogController.cacheCatalog(opdsFeed, resourceMode, new String(opdsContents, 
            "UTF-8"));
        
        return opdsFeed;
    }
    
    /**
     * Get the Feed ID of a catalog according to a URL it is known to have been
     * downloaded from
     * 
     * @param url The URL of where the catalog was downloaded from
     * @param resourceMode 
     * 
     * @return The OPDS ID of the entry from that location
     */
    public String getCatalogIDByURL(String url, int resourceMode) {
        String catalogID = null;
        String prefKey = getPrefKeyNameForOPDSURLToIDMap(url);
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            catalogID = UstadMobileSystemImpl.getInstance().getUserPref(prefKey);
        }
        
        if(catalogID == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            catalogID = UstadMobileSystemImpl.getInstance().getAppPref(prefKey);
        }
        
        return catalogID;
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
    
    protected static String getFileNameForOPDSFeedId(String feedId) {
        return ".cache-" + sanitizeIDForFilename(feedId) + ".opds";
    }
    
    protected static String getPrefKeyNameForOPDSURLToIDMap(String opdsId) {
        return "opds-id-to-url-" + sanitizeIDForFilename(opdsId);
    }
    
    
    
    public static String sanitizeIDForFilename(String id) {
        char c;
        int len = id.length();
        StringBuffer retVal = new StringBuffer();
        for(int i = 0; i < len; i++) {
            c = id.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '*' || c == '_') {
                retVal.append(c);
            }else if(c == ' ' || c == '\t' || c == '\n'){
                retVal.append('_');
            }else {
                retVal.append("_").append(Integer.toHexString((int)c));
            }
        }
        return retVal.toString();
    }
    
    /**
     * Cache the given catalog so it can be retrieved when offline
     * 
     * @param catalog 
     * @param ownerUser the user that owns the download, or null if this is for the shared directory
     * @param serializedCatalog String contents of the catalog (in XML) : optional : if they are 'handy', otherwise null
     */
    public static void cacheCatalog(UstadJSOPDSFeed catalog, int resourceMode, String serializedCatalog) throws IOException{
        String destPath = null;
        boolean isUserMode = (resourceMode & USER_RESOURCE) == USER_RESOURCE;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!isUserMode) {
            destPath = impl.getSharedContentDir();
        }else {
            destPath = impl.getUserContentDirectory(impl.getActiveUser());
        }
        
        destPath += "/" + getFileNameForOPDSFeedId(catalog.id);
        if(serializedCatalog == null) {
            serializedCatalog = catalog.toString();
        }
        impl.writeStringToFile(serializedCatalog, destPath, "UTF-8");
        String keyName = "opds-cache-" + catalog.id;
        impl.setPref(isUserMode, keyName, destPath);
        impl.setPref(isUserMode, getPrefKeyNameForOPDSURLToIDMap(catalog.id), 
            catalog.href);
    }
    
    /**
     * For any acquisition feed that we know about - make a catalog of what we have locally
     * that points at the actual contain entries on the disk.
     * 
     * This will go through all the entries in the catalog (retrieved from cache)
     * and check the acquisition status of each entry.  If the entry is present 
     * on the system it will be included in the returned feed.
     * 
     * This can then be saved to a file ending .local.opds
     * 
     * @param catalog the Remote catalog we want a local feed for
     * @return A feed containing an entry for each entry in the remote catalog
     * already acquired; with the links pointing to the local file container
     * 
     */
    public static UstadJSOPDSFeed generateLocalCatalog(UstadJSOPDSFeed catalog) {
        return null;
    }
    
    /**
      * Get a cached copy of a given catalog according to it's ID
      * 
      * @param catalogID - The catalogID to retrieve the cached entry for (if available)
      * @param resourceMode - 
      * 
      * 
      */
    public static UstadJSOPDSFeed getCachedCatalogByID(String catalogID, int resourceMode) throws IOException, XmlPullParserException{
        String filename = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String key = "opds-cache-" + catalogID;
        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            filename = impl.getUserPref(key);
        }
        
        if(filename == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            filename = impl.getAppPref(key);
        }
        
        if(filename != null) {
            String contentsXML = impl.readFileAsText(filename, "UTF-8");
            UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(contentsXML);
            return feed;
        }
        
        return null;
    }
    
    /**
     * 
     * @param url
     * @param username
     * @return 
     */
    public static UstadJSOPDSFeed getCachedCatalogByURL(String url, int resourceMode) {
        UstadJSOPDSFeed retVal = null;
        
        
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
     * Generates a String preference key for the given entryID.  Used to map
     * in the form of entryID -> EntryInfo (serialized)
     * 
     * @param entryID
     * @return 
     */
    private static String getEntryInfoKey(String entryID) {
        return "e2ei-" + entryID;
    }
    
    /**
     * Save the info we need to know about a given entry using CatalogEntryInfo
     * object which can be encoded as a String then saved as an app or user 
     * preference
     * 
     * @param entryID the OPDS ID of the entry in question
     * @param entryInfo CatalogEntryInfo object with required info about entry
     * @param resourceMode  USER_RESOURCE or SHARED_RESOURCE to be set as a user or shared preference
     * Use USER_RESOURCE when the file is in the users own directory, SHARED_RESOURCE otherwise
     */
    public static void setEntryInfo(String entryID, CatalogEntryInfo info, int resourceMode) {
        UstadMobileSystemImpl.getInstance().setPref(resourceMode == USER_RESOURCE, 
            getEntryInfoKey(entryID), info != null? info.toString(): null);
    }
    
    /**
     * Get info about a given entryID; if known by the device.  Will return a 
     * CatalogEntryInfo that was serialized as a String.  
     * 
     * @param entryID The OPDS ID in question
     * @param resourceMode BitMask - valid values are USER_RESOURCE and SHARED_RESOURCE
     * eg. to get both - use USER_RESOURCE | SHARED_RESOURCE
     * @return CatalogEntryInfo for the given ID, or null if not found
     */
    public static CatalogEntryInfo getEntryInfo(String entryID, int resourceMode) {
        String prefKey = getEntryInfoKey(entryID);
        String entryInfoStr = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            entryInfoStr = impl.getUserPref(prefKey);
        }
        
        if(entryInfoStr == null && (resourceMode & SHARED_RESOURCE) ==SHARED_RESOURCE) {
            entryInfoStr = impl.getAppPref(prefKey);
        }
        
        if(entryInfoStr != null) {
            return CatalogEntryInfo.fromString(entryInfoStr);
        }else {
            return null;
        }
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
     * 
     * @param entries The OPDS Entries that should be acquired.  Must be OPDS 
     * Entry items with acquire links.  For now the first acquisition link will
     * be used
     * TODO: Enable user specification of preferred acuiqsition types
     * 
     * @param httpUsername optional HTTP authentication username - can be null
     * @param httpPassword optional HTTP authentication password - can be null
     * @param flags bitmask flags to use (unused currently)
     * @param resourceMode SHARED_RESOURCE or USER_RESOURCE to save to shared area or user specific area.
     * 
     * @return a Transfer job, that when it's start method is called will acquire the given entries
     */
    public static UMTransferJobList acquireCatalogEntries(UstadJSOPDSEntry[] entries, String httpUsername, String httpPassword, int resourceMode, int flags) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UMTransferJob[] transferJobs = new UMTransferJob[entries.length];
        String[] mimeTypes = new String[entries.length];
        
        String destDirPath = null;
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            destDirPath = impl.getUserContentDirectory(impl.getActiveUser());
        }else {
            destDirPath = impl.getSharedContentDir();
        }
        
        Hashtable authHeaders = makeAuthHeaders(httpUsername, httpPassword);
        
        for(int i = 0; i < entries.length; i++) {
            Vector itemLinks = entries[i].getAcquisitionLinks();
            if(itemLinks.size() <= 0) {
                continue;
            }
            
            String[] firstLink = ((String[])itemLinks.elementAt(0));
            String itemHref = firstLink[UstadJSOPDSItem.LINK_HREF];
            mimeTypes[i] = firstLink[UstadJSOPDSItem.LINK_MIMETYPE];
            
            String itemURL = UMFileUtil.resolveLink(entries[i].parentFeed.href, 
                    itemHref);
            itemLinks = null;
            
            String destFilename = UMFileUtil.joinPaths(new String[] {
                destDirPath,
                UMFileUtil.getFilename(itemHref)
            });
            
            transferJobs[i] = impl.downloadURLToFile(itemURL, destFilename, 
                authHeaders);
        }
        
        UMTransferJobList transferJob = new UMTransferJobList(transferJobs, 
            entries);
        transferJob.setRunAfterFinishJob(new AcquirePostDownloadRunnable(entries, 
            transferJobs, mimeTypes, resourceMode));
        return transferJob;
    }

    //@Override
    public void progressUpdated(UMProgressEvent evt) {
        if(this.view != null) {
            UMTransferJobList jobList = (UMTransferJobList)evt.getSrc();
            int currentJobItem = jobList.getCurrentItem();
            UstadJSOPDSEntry entry = (UstadJSOPDSEntry)jobList.getJobValue(
                currentJobItem);
            boolean isComplete = 
                jobList.getCurrentJobProgress() == jobList.getCurrentJobTotalSize();
            this.view.updateDownloadEntryProgress(entry.id, jobList.getCurrentJobProgress(), 
                jobList.getCurrentJobTotalSize());
            if(isComplete) {
                this.view.setEntryStatus(entry.id, STATUS_ACQUIRED);
            }
        }
    }
    
    /**
     * Runs before an acquisition run goes: marks items as in progress
     */
    private static class AcquirePreDownloadRunnable implements Runnable{
        
        private UstadJSOPDSEntry[] entries;
        
        private UMTransferJob[] srcJobs;
        
        private String[] mimeTypes;
        
        public AcquirePreDownloadRunnable(UstadJSOPDSEntry[] entries, UMTransferJob[] srcJobs, String[] mimeTypes) {
            this.entries = entries;
            this.mimeTypes = mimeTypes;
        }
        
        public void run() {
            
        }
    }
    
    private static class AcquirePostDownloadRunnable implements Runnable {
        private UstadJSOPDSEntry[] entries;
        
        private UMTransferJob[] srcJobs;
        
        private String[] mimeTypes;
        
        private int resourceMode;
        
        public AcquirePostDownloadRunnable(UstadJSOPDSEntry[] entries, UMTransferJob[] srcJobs, String[] mimeTypes, int resourceMode) {
            this.entries = entries;
            this.srcJobs = srcJobs;
            this.mimeTypes = mimeTypes;
            this.resourceMode = resourceMode;
        }
        
        public void run() {
            for(int i = 0; i < entries.length; i++) {
                CatalogEntryInfo info = new CatalogEntryInfo();
                info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
                info.srcURLs = new String[]{srcJobs[i].getSource()};
                info.fileURI = srcJobs[i].getDestination();
                info.mimeType = mimeTypes[i];
                CatalogController.setEntryInfo(entries[i].id, info, resourceMode);
            }
        }
    }
    
    
    /**
     * Delete the given entry
     * 
     * @param entryID
     * @param resourceMode
     */
    public static void removeEntry(String entryID, int resourceMode) {
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            actionRemoveEntry(entryID, USER_RESOURCE);
        }
        
        if((resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            actionRemoveEntry(entryID, SHARED_RESOURCE);
        }
    }
    
    private static void actionRemoveEntry(String entryID, int resourceMode) {
        CatalogEntryInfo entry = getEntryInfo(entryID, resourceMode);
        if(entry != null && entry.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.removeFile(entry.fileURI);
            setEntryInfo(entryID, null, resourceMode);
        }
    }
    
    public static int getAcquisitionStatusByEntryID(String entryID, String user) {
        return -1;
    }
    
}
