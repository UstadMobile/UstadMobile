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

import com.ustadmobile.core.U;
import com.ustadmobile.core.app.Base64;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMProgressEvent;
import com.ustadmobile.core.impl.UMProgressListener;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UMTransferJob;
import com.ustadmobile.core.impl.UMTransferJobList;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UstadView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
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
public class CatalogController implements UstadController, UMProgressListener, AppViewChoiceListener, AsyncLoadableController, UMDownloadCompleteReceiver {
    
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
    
    /**
     * The currently selected download mode (set after choice dialog to user)
     */
    private int selectedDownloadMode = SHARED_RESOURCE;
    
    /**
     * The locations available for a user to store downloaded content
     */
    private UMStorageDir[] availableStorageDirs;    
    
    private int resourceMode;
    
    //The View (J2ME or Android)
    private CatalogView view;
    
    //this is where the feed (and its entries) live.
    private CatalogModel model;
    
    public static int[] catalogMenuOptIDS = new int[]{U.id.mycourses, 
        U.id.onthisdevice, U.id.logout, U.id.about};
    
    public static final int MENUINDEX_MYCOURSES = 0;
    
    public static final int MENUINDEX_MYDEVICE = 1;
    
    public static final int MENUINDEX_LOGOUT = 2;
    
    public static final int MENUINDEX_ABOUT = 3;
    
    public static final String LOCALOPDS_ID_SUFFIX = "-local";
    
    /**
     * Hardcoded OPDS extension (to save time in loops)
     */
    public static final String OPDS_EXTENSION = ".opds";
    
    /**
     * Hardcoded EPUB extension ".epub"
     */
    public static final String EPUB_EXTENSION = ".epub";
    
    /**
     * Hardcoded fixed path to the container.xml file as per the open container
     * format spec : META-INF/container.xml
     */
    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";
    
    /**
     * Prefix used for pref keys that are used to store entry info
     */
    private static final String PREFIX_ENTRYINFO = "e2ei-";
    
    
    /**
     *  Flag used to indicate the choice shown as part of selecting the storage space
     */
    public static final int CMD_SELECT_SHARED_OR_USERONLY = 10;
    
    /**
     * Flag used to indicate the choice is about which storage directory to use
     */
    public static final int CMD_SELECT_STORAGE_DIR = 20;
    
    private static final int CHOICE_DOWNLOAD_SHARED = 0;
    
    private static final int CHOICE_DOWNLOAD_USER = 1;
    
    private static final int CHOICE_DOWNLOAD_CANCEL = 2;
    
    
    public static final String KEY_URL = "url";
    
    public static final String KEY_RESMOD = "resmod";
    
    public static final String KEY_HTTPUSER = "httpu";
    
    public static final String KEY_HTTPPPASS = "httpp";
    
    public static final String KEY_FLAGS = "flags";
    
    
    public static final Integer LOAD_URL = new Integer(1);
    
    public static final Integer LOAD_IMPL = new Integer(2);
    
    public static final Integer LOAD_RESMODE = new Integer(3);
    
    public static final Integer LOAD_HTTPUSER = new Integer(4);
    
    public static final Integer LOAD_HTTPPASS = new Integer(5);
    
    public static final Integer LOAD_FLAGS = new Integer(6);
    
    private Object context;
    
    private Timer downloadUpdateTimer;
    
    public static final int DOWNLOAD_UPDATE_INTERVAL = 1000;
    
    //Hashtable indexed entry id -> download ID (Long object)
    private Hashtable downloadingEntries;
    
    public CatalogController(Object context) {
        this.context = context;
        downloadingEntries = new Hashtable();
    }
    
    public CatalogController(CatalogModel model, Object context){
        this(context);
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
    
    

    /**
     * The resource mode by which this CatalogController was loaded
     *
     * @return the value of resourceMode
     */
    public int getResourceMode() {
        return resourceMode;
    }

    /**
     * Set the value of resourceMode: the resource mode by which this CatalogController
     * was loaded
     *
     * @param resourceMode new value of resourceMode
     */
    public void setResourceMode(int resourceMode) {
        this.resourceMode = resourceMode;
    }

    
    //methods go here..
    
    public void handleClickRefresh() {
        
    }
    
    //shows the view
    /**
     * @deprecated 
     */
    public void show() {
        /*
        UstadJSOPDSFeed feed = this.getModel().opdsFeed;
        if(feed.isAcquisitionFeed()) {
            //go through and set the acquisition status
            for(int i = 0; i < feed.entries.length; i++) {
                CatalogEntryInfo info = getEntryInfo(feed.entries[i].id, 
                    USER_RESOURCE | SHARED_RESOURCE, context);
                if(info != null) {
                    this.view.setEntryStatus(feed.entries[i].id, 
                        info.acquisitionStatus);
                }
                
            }
        }
        */
        
    }
    
    public UstadView getView() {
        return this.view;
    }
    
    public void setUIStrings(UstadView view) {
        CatalogView cView = (CatalogView)view;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        cView.setMenuOptions(new String[]{
            impl.getString(catalogMenuOptIDS[MENUINDEX_MYCOURSES]),
            impl.getString(catalogMenuOptIDS[MENUINDEX_MYDEVICE]),
            impl.getString(catalogMenuOptIDS[MENUINDEX_LOGOUT]),
            impl.getString(catalogMenuOptIDS[MENUINDEX_ABOUT])});
        
    }
    
    
    public void setView(UstadView view) {
        this.view = (CatalogView)view;
    }
    
    
    public void hide() {
        
    }
    
    public static boolean isInProgress(String entryID) {
        return false;
        //TODO: Fix me
        //return currentAcquisitionJobs.containsKey(entryID);
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
    public static CatalogController makeControllerByURL(String url, UstadMobileSystemImpl impl, int resourceMode, String httpUser, String httpPassword, int flags, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = CatalogController.getCatalogByURL(url, resourceMode, 
            httpUser, httpPassword, flags, context);
        CatalogController result = new CatalogController(
            new CatalogModel(opdsFeed), context);
        result.setResourceMode(resourceMode);
        
        return result;
    }
    
    /**
     * Makes the controller for the given view used when the underlying system 
     * (e.g. Android etc.) has made a view by restoring it from a saved state
     * etc.
     * 
     * This is done asynchronously - this is normally going to be called from a UI
     * thread.
     * 
     * @param view The view we are attaching with
     * @param url as per makeControllerByURL
     * @param impl as per makeControllerByURL
     * @param resourceMode as per makeControllerByURL 
     * @param flags as per makeControllerByURL
     * 
     * @see CatalogController#makeControllerByURL(java.lang.String, com.ustadmobile.core.impl.UstadMobileSystemImpl, int, java.lang.String, java.lang.String, int) 
     * 
     * @return CatalogController attached with the given view
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public static void makeControllerForView(final CatalogView view, final String url, final UstadMobileSystemImpl impl, final int resourceMode, final int flags, final ControllerReadyListener listener) {
        Hashtable args = new Hashtable();
        if(impl == null) {
            throw new IllegalArgumentException("impl cannot be null dimwit");
        }
        
        Object ctx = view.getContext();
        
        args.put(LOAD_URL, url);
        args.put(LOAD_IMPL, impl);
        args.put(LOAD_RESMODE, Integer.valueOf(resourceMode));
        args.put(LOAD_FLAGS, Integer.valueOf(flags));
        
        if(impl.getActiveUser(ctx) != null && impl.getActiveUserAuth(ctx) != null) {
            args.put(LOAD_HTTPUSER, impl.getActiveUser(ctx));
            args.put(LOAD_HTTPPASS, impl.getActiveUserAuth(ctx));
        }
        
        CatalogController controller = new CatalogController(ctx);
        new LoadControllerThread(args, controller, listener, view).start();
    }

    
    public UstadController loadController(Hashtable args, Object ctx) throws Exception {
        return makeControllerByURL((String)args.get(LOAD_URL), 
            (UstadMobileSystemImpl)args.get(LOAD_IMPL), 
            ((Integer)args.get(LOAD_RESMODE)).intValue(), 
            args.get(LOAD_HTTPUSER) != null ? (String)args.get(LOAD_HTTPUSER) : null, 
            args.get(LOAD_HTTPPASS) != null ? (String)args.get(LOAD_HTTPPASS) : null, 
            ((Integer)args.get(LOAD_FLAGS)).intValue(), ctx);
    }
    
    /**
     * Make a CatalogController for the user's default OPDS catalog
     * 
     * @param impl system implementation to be used
     * @deprecated
     * 
     * @return CatalogController representing the default catalog for the active user
     */
    public static CatalogController makeUserCatalog(UstadMobileSystemImpl impl, Object context) throws IOException, XmlPullParserException{
        String opdsServerURL = impl.getUserPref("opds_server_primary", 
            UstadMobileDefaults.DEFAULT_OPDS_SERVER, context);
        
        String activeUser = impl.getActiveUser(context);
        String activeUserAuth = impl.getActiveUserAuth(context);
        return CatalogController.makeControllerByURL(opdsServerURL, impl, 
            USER_RESOURCE, activeUser, activeUserAuth, CACHE_ENABLED, context);
        
    }
    
    public static Hashtable makeUserCatalogArgs(Object context) {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String catalogURL = impl.getUserPref("opds_server_primary", 
            UstadMobileDefaults.DEFAULT_OPDS_SERVER, context);
        args.put(CatalogController.KEY_URL, catalogURL);
        args.put(CatalogController.KEY_HTTPUSER, impl.getActiveUser(context));
        args.put(CatalogController.KEY_HTTPPPASS, impl.getActiveUserAuth(context));
        args.put(CatalogController.KEY_FLAGS, 
            new Integer(CatalogController.CACHE_ENABLED));
        args.put(CatalogController.KEY_RESMOD, 
            new Integer(CatalogController.USER_RESOURCE));
        
        return args;
    }
    
    public static CatalogController makeDeviceCatalog(Object context) throws IOException {
        UstadJSOPDSFeed deviceFeed = makeDeviceFeed(null, null, 
                USER_RESOURCE | SHARED_RESOURCE, context);
        return new CatalogController(new CatalogModel(deviceFeed), context);
    }
    
    /**
     * Make a catalog representing the files that are now in the shared and user
     * directories
     * 
     * @param sharedDir - Shared directory to use: or null to use default shared directory
     * @param userDir - User content directory to use: or null to use default user directory
     * @param dirFlags - Set which directories to scan: inc USER_RESOURCE , SHARED_RESOURCE
     * 
     * @return CatalogController representing files on the device
     */
    public static UstadJSOPDSFeed makeDeviceFeed(String sharedDir, String userDir, int dirFlags, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean incShared = (dirFlags & SHARED_RESOURCE) == SHARED_RESOURCE;
        boolean incUser = (dirFlags & USER_RESOURCE) == USER_RESOURCE;
        
        
        verifyKnownEntries(SHARED_RESOURCE, context);
        
        Vector opdsFilesVector = new Vector();
        int opdsUserStartIndex = 0;
        Vector containerFilesVector = new Vector();
        int containerUserStartIndex = 0;
        
        sharedDir = (sharedDir == null) ? impl.getSharedContentDir() : sharedDir;
        if(incUser && userDir == null) {
            userDir = impl.getUserContentDirectory(impl.getActiveUser(context));
        }
        
        if(incShared) {
            findOPDSFilesInDir(sharedDir, opdsFilesVector, containerFilesVector);
            opdsUserStartIndex = opdsFilesVector.size();
            containerUserStartIndex = containerFilesVector.size();
        }
        
        if(incUser) {
            findOPDSFilesInDir(userDir, opdsFilesVector, containerFilesVector);
        }
        
        String[] opdsFiles = new String[opdsFilesVector.size()];
        opdsFilesVector.copyInto(opdsFiles);
        opdsFilesVector = null;
        
        String[] containerFiles = new String[containerFilesVector.size()];
        containerFilesVector.copyInto(containerFiles);
        containerFilesVector = null;
        
        String generatedHREFBase = incUser ? impl.getUserContentDirectory(
                impl.getActiveUser(context)) : impl.getSharedContentDir();
        
        String looseFilePath = UMFileUtil.joinPaths(new String[] {generatedHREFBase, 
            "cache-loose"});
        
        boolean[] userOPDSFiles = new boolean[opdsFiles.length];
        UMUtil.fillBooleanArray(userOPDSFiles, true, opdsUserStartIndex, 
                userOPDSFiles.length);
        boolean[] userEPUBFiles = new boolean[containerFiles.length];
        UMUtil.fillBooleanArray(userEPUBFiles, true, containerUserStartIndex, 
                containerUserStartIndex);
        
        return scanFiles(opdsFiles, userOPDSFiles, containerFiles, userEPUBFiles, 
            looseFilePath, generatedHREFBase, "My Device", 
            "scandir-" + sanitizeIDForFilename(generatedHREFBase), context);
        
    }
    
    /**
     * Triggered by the view when the user has selected the download all button
     * for this feed
     * 
     */
    public void handleClickDownloadAll() {
        selectedEntries = getModel().opdsFeed.entries;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        view.showConfirmDialog(impl.getString(U.id.download_q), 
            LocaleUtil.formatMessage(impl.getString(U.id.download_all_q), 
                String.valueOf(selectedEntries.length)),
                impl.getString(U.id.ok), impl.getString(U.id.cancel), 
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
     * @param entries OPDS item selected
     */
    public void handleClickDeleteEntries(UstadJSOPDSEntry[] entries) {
        selectedEntries = entries;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        this.view.showConfirmDialog(impl.getString(U.id.delete_q), 
            LocaleUtil.formatMessage(impl.getString(U.id.delete_x_entries_from_device), 
                String.valueOf(entries.length)), impl.getString(U.id.delete), 
                impl.getString(U.id.cancel), CMD_DELETEENTRY);
    }
    
    public void handleConfirmDeleteEntries() {
        for(int i = 0; i < selectedEntries.length; i++) {
            CatalogController.removeEntry(selectedEntries[i].id, 
                USER_RESOURCE | SHARED_RESOURCE, context);
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
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!entry.parentFeed.isAcquisitionFeed()) {
            //we are loading another opds catalog
            Vector entryLinks = entry.getLinks(null, UstadJSOPDSItem.TYPE_ATOMFEED, 
                true, true);
            
            final Object ctx = this.context;
            
            if(entryLinks.size() > 0) {
                String[] firstLink = (String[])entryLinks.elementAt(0);
                final String url = UMFileUtil.resolveLink(entry.parentFeed.href, 
                    firstLink[UstadJSOPDSItem.LINK_HREF]);
                
                Hashtable args = new Hashtable();
                args.put(KEY_URL, url);
                args.put(KEY_HTTPUSER, impl.getActiveUser(context));
                args.put(KEY_HTTPPPASS, impl.getActiveUserAuth(context));
                args.put(KEY_RESMOD, new Integer(getResourceMode()));
                args.put(KEY_FLAGS, new Integer(CACHE_ENABLED));
                
                
                UstadMobileSystemImpl.getInstance().go(CatalogView.class, args, 
                        context);
            }
        }else {
            CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id, 
                    SHARED_RESOURCE | USER_RESOURCE, context);
            if(entryInfo != null && entryInfo.acquisitionStatus == STATUS_ACQUIRED) {
                Hashtable openArgs = new Hashtable();
                openArgs.put(ContainerController.ARG_CONTAINERURI, entryInfo.fileURI);
                openArgs.put(ContainerController.ARG_MIMETYPE, entryInfo.mimeType);
                UstadMobileSystemImpl.getInstance().go(ContainerView.class, openArgs, 
                        context);
            }else if(isInProgress(entry.id)){
                UstadMobileSystemImpl.getInstance().getAppView(context).showNotification(
                        impl.getString(U.id.download_in_progress), AppView.LENGTH_LONG);
            }else{
                this.handleClickDownloadEntries(new UstadJSOPDSEntry[]{entry});
            }
        }
    }
    
    /**
     * Handle when the user has selected where the entries they want to 
     * download should be stored
     * 
     * @param storageMode 
     */
    public void handleSelectDownloadStorageMode(int storageMode) {
        selectedDownloadMode = storageMode;
        availableStorageDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
            storageMode, context);
        String[] storageChoices = new String[availableStorageDirs.length];
        for(int i = 0; i < storageChoices.length; i++) {
            storageChoices[i] = availableStorageDirs[i].getName();
        }
        UstadMobileSystemImpl.getInstance().getAppView(context).showChoiceDialog(
            UstadMobileSystemImpl.getInstance().getString(U.id.save_to), 
            storageChoices, CMD_SELECT_STORAGE_DIR, this);
    }

    /**
     * Handle when the choice dialog has resulted in a decision 
     * 
     * @param commandId
     * @param choice 
     */
    public void appViewChoiceSelected(int commandId, int choice) {
        AppView appView = UstadMobileSystemImpl.getInstance().getAppView(context);
        switch(commandId) {
            case CMD_SELECT_SHARED_OR_USERONLY:
                switch(choice) {
                    case CHOICE_DOWNLOAD_USER:
                        handleSelectDownloadStorageMode(USER_RESOURCE);
                        break;
                    case CHOICE_DOWNLOAD_SHARED:
                        handleSelectDownloadStorageMode(SHARED_RESOURCE);
                        break;
                    case CHOICE_DOWNLOAD_CANCEL:
                        appView.dismissChoiceDialog();
                        break;
                }
                break;
                
            case CMD_SELECT_STORAGE_DIR:
                //here we need to call confirmDownload - and add a parameter for the storage dir selected
                appView.dismissChoiceDialog();
                String destDirURI = availableStorageDirs[choice].getDirURI();
                handleConfirmDownloadEntries(selectedEntries, destDirURI);
                break;
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
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] choices = new String[]{impl.getString(U.id.all_users), 
            impl.getString(U.id.only_me), impl.getString(U.id.cancel)};
        impl.getAppView(context).showChoiceDialog(impl.getString(U.id.download_for),
            choices, CMD_SELECT_SHARED_OR_USERONLY, this);
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
            case CMD_DELETEENTRY:
                this.handleConfirmDeleteEntries();
                break;
        }
    }
    
    
    /**
     * Triggered when the user confirms that they wish to download a given set 
     * of entries
     * 
     * @param entries - the entries that the user wants to download
     * @param destDirURI - the directory in which the entries should be saved
     */
    public void handleConfirmDownloadEntries(UstadJSOPDSEntry[] entries, String destDirURI) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        AcquireRequest request = new AcquireRequest(entries, destDirURI, 
            impl.getActiveUser(context), impl.getActiveUserAuth(context), 
            selectedDownloadMode, context, this);
        CatalogController.acquireCatalogEntries(request);
        
        //TODO: Add event listeners to update progress etc.
        /*
        if(activeTransferJobs == null) {
            activeTransferJobs = new Vector();
        }
        transferJob.addProgressListener(this);
        setViewEntryProgressVisible(entries, true);
        transferJob.start();
        activeTransferJobs.addElement(transferJob);
        */
        
        this.view.setSelectedEntries(new UstadJSOPDSEntry[0]);
        
    }
    
    /**
     * Called when the user selects an item from the menu (e.g. Drawer, J2ME options
     * menu etc)
     * 
     * @param index Index of the item clicked corresponding with the menuitem string array
     */
    public void handleClickMenuItem(int index) {
        switch(index) {
            case MENUINDEX_LOGOUT:
                UstadMobileSystemImpl.getInstance().go(LoginView.class, 
                    null, context);
                break;
            case MENUINDEX_MYDEVICE:
                try {
                    CatalogController deviceCatCtrl = 
                            CatalogController.makeDeviceCatalog(context);
                    deviceCatCtrl.show();
                }catch(IOException e) {
                    UstadMobileSystemImpl.getInstance().getAppView(context).showNotification(
                        UstadMobileSystemImpl.getInstance().getString(U.id.error_loading_catalog), 
                        AppView.LENGTH_LONG);
                }
                break;
                            
        }
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
    public static UstadJSOPDSFeed getCatalogByURL(String url, int resourceMode, String httpUsername, String httpPassword, int flags, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getLogger().l(UMLog.INFO, 307, url);
        
        Hashtable headers = makeAuthHeaders(httpUsername, httpPassword);
        
        XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
        HTTPResult result = impl.readURLToString(url, headers);
        if(result.getStatus() != 200) {
            throw new IOException("HTTP Error " + result.getStatus());
        }
        
        byte[] opdsContents = result.getResponse();
        parser.setInput(
            new ByteArrayInputStream(opdsContents), 
            "UTF-8");

        opdsFeed = UstadJSOPDSFeed.loadFromXML(parser);
        impl.getLogger().l(UMLog.DEBUG, 504, "Catalog Null:" + (opdsFeed == null));
        opdsFeed.href = url;
        stripEntryUMCloudIDPrefix(opdsFeed);
        CatalogController.cacheCatalog(opdsFeed, resourceMode, new String(opdsContents, 
            "UTF-8"), context);
        
        return opdsFeed;
    }
    
    /**
     * Workaround for UMCloud issue: UMCloud includes an http tin can activity
     * prefix on the id that is not present in the epub files themselves.
     * 
     * TODO: Remove this as soon as the issue is removed from the server.
     * 
     * @param feed 
     */
    private static void stripEntryUMCloudIDPrefix(UstadJSOPDSFeed feed) {
        
        String prefix = "http://www.ustadmobile.com/um-tincan/activities/";
        
        if(feed.isAcquisitionFeed()) {
            for(int i = 0; i < feed.entries.length; i++) {
                if(feed.entries[i].id.startsWith(prefix)) {
                    feed.entries[i].id = feed.entries[i].id.substring(
                            prefix.length());
                }
            }
        }
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
            catalogID = UstadMobileSystemImpl.getInstance().getUserPref(prefKey,
                context);
        }
        
        if(catalogID == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            catalogID = UstadMobileSystemImpl.getInstance().getAppPref(prefKey,
                context);
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
        return "cache-" + sanitizeIDForFilename(feedId) + OPDS_EXTENSION;
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
    public static void cacheCatalog(UstadJSOPDSFeed catalog, int resourceMode, String serializedCatalog, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getLogger().l(UMLog.VERBOSE, 405, "id: " + catalog.id + " href: " + catalog.href);
        
	String destPath;
        boolean isUserMode = (resourceMode & USER_RESOURCE) == USER_RESOURCE;
        
        destPath = impl.getCacheDir(resourceMode, context);
        
	destPath = UMFileUtil.joinPaths(new String[]{destPath, 
            getFileNameForOPDSFeedId(catalog.id)});
        
        impl.getLogger().l(UMLog.DEBUG, 505, destPath);
        
        if(serializedCatalog == null) {
            serializedCatalog = catalog.toString();
        }
	
        impl.writeStringToFile(serializedCatalog, destPath, "UTF-8");
        String keyName = "opds-cache-" + catalog.id;
	
        impl.setPref(isUserMode, keyName, destPath, context);
        impl.setPref(isUserMode, getPrefKeyNameForOPDSURLToIDMap(catalog.id), 
            catalog.href, context);
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
     * @param resourceMode SHARED_RESOURCE or USER_RESOURCE
     * @return A feed containing an entry for each entry in the remote catalog
     * already acquired; with the links pointing to the local file container
     * 
     */
    public static UstadJSOPDSFeed generateLocalCatalog(UstadJSOPDSFeed catalog, int resourceMode, int acquisitionStatusMode, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String baseDir = resourceMode == SHARED_RESOURCE ? impl.getSharedContentDir() :
            impl.getUserContentDirectory(impl.getActiveUser(context));
        String localCatalogID = catalog.id + LOCALOPDS_ID_SUFFIX;
        String filename = sanitizeIDForFilename(localCatalogID) + ".local.opds";
        
        /* $if umplatform == 2  $ */
            String filePath = UMFileUtil.joinPaths(new String[]{baseDir, filename});
         /* $else$
            String filePath = UMFileUtil.joinPaths(new String[]{"file:///", baseDir, filename});
        $endif$ */
        
        
        impl.getLogger().l(UMLog.DEBUG, 514, filePath);
        
        UstadJSOPDSFeed newFeed = new UstadJSOPDSFeed(filePath, 
            catalog.title + LOCALOPDS_ID_SUFFIX, catalog.id + LOCALOPDS_ID_SUFFIX);
        
        int i;//loop 1- through entries
        int j;//loop 2 - through each entry which is acquired
        Vector entryLinks;//links in each entry
        Vector linksToRemove = new Vector();
        
        for(i = 0; i < catalog.entries.length; i++){
            CatalogEntryInfo info = getEntryInfo(catalog.entries[i].id, 
                acquisitionStatusMode, context);
            if(info != null && info.acquisitionStatus == STATUS_ACQUIRED) {
                //add an entry with a pointer to the local file
                UstadJSOPDSEntry entryCopy = new UstadJSOPDSEntry(newFeed, 
                    catalog.entries[i]);
                //remove acquisition links and replace with pointers to the local file
                entryLinks = entryCopy.getLinks();
                linksToRemove.removeAllElements();
                
                for(j = 0; j < entryLinks.size(); j++) {
                    String[] thisLink = (String[])entryLinks.elementAt(j);
                    if(thisLink[UstadJSOPDSItem.LINK_REL].startsWith(UstadJSOPDSEntry.LINK_ACQUIRE)) {
                        linksToRemove.addElement(thisLink);
                    }
                }
                
                for(j = 0; j < linksToRemove.size(); j++) {
                    entryLinks.removeElement(linksToRemove.elementAt(j));
                }
                
                entryCopy.addLink(UstadJSOPDSItem.LINK_ACQUIRE, info.fileURI, 
                    info.mimeType);
                newFeed.addEntry(entryCopy);
            }
        }
        
        String savePath = UMFileUtil.joinPaths(new String[]{baseDir, filename});
        impl.getLogger().l(UMLog.DEBUG, 516, savePath);
        impl.writeStringToFile(newFeed.toString(), savePath, "UTF-8");
        
        return newFeed;
    }
    
    /**
      * Get a cached copy of a given catalog according to it's ID
      * 
      * @param catalogID - The catalogID to retrieve the cached entry for (if available)
      * @param resourceMode - 
      * 
      * 
      */
    public static UstadJSOPDSFeed getCachedCatalogByID(String catalogID, int resourceMode, Object context) throws IOException, XmlPullParserException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        impl.getLogger().l(UMLog.VERBOSE, 406, catalogID);
        
        String filename = null;
        
        String key = "opds-cache-" + catalogID;
        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            filename = impl.getUserPref(key, context);
            impl.getLogger().l(UMLog.DEBUG, 509, filename);
        }
        
        if(filename == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            filename = impl.getAppPref(key, context);
            impl.getLogger().l(UMLog.DEBUG, 510, filename);
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
     * Find OPDS and container (e.g. .epub files) in the given directory.  Add
     * them to the given vectors
     * 
     * @param dir The directory to look in
     * @param opdsFiles A vector into which OPDS files will be added : As a string path dir joined to the filename
     * @param containerFiles A vector into which containerFiles will be added : As a string path dir joined to the filename
     */
    public static void findOPDSFilesInDir(String dir, Vector opdsFiles, Vector containerFiles) throws IOException{
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] dirContents = impl.listDirectory(dir);
        
        for(int i = 0; i < dirContents.length; i++) {
            if(dirContents[i].startsWith("cache")) {
                continue;
            }
            
            if(dirContents[i].endsWith(OPDS_EXTENSION)) {
                opdsFiles.addElement(UMFileUtil.joinPaths(new String[]{dir, 
                    dirContents[i]}));
            }else if(dirContents[i].endsWith(EPUB_EXTENSION)) {
                containerFiles.addElement(UMFileUtil.joinPaths(new String[]{dir, 
                    dirContents[i]}));
            }
        }
    }
    
    
    
    /**
     * This is to verify that the entries we think we know about... are in fact there
     * e.g. in case the user deletes files etc.
     * 
     * @param resourceMode SHARED_RESOURCE or USER_RESOURCE
     */
    public static void verifyKnownEntries(int resourceMode, Object context) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.INFO, 313, "mode: " + resourceMode);
        boolean isShared = resourceMode == SHARED_RESOURCE;
        String[] entryInfoKeys = UMUtil.filterArrByPrefix(
            isShared ? impl.getAppPrefKeyList(context) : impl.getUserPrefKeyList(context),
            PREFIX_ENTRYINFO);
        
        CatalogEntryInfo info;
        for(int i = 0; i < entryInfoKeys.length; i++) {
            info = CatalogEntryInfo.fromString(
                isShared ? impl.getAppPref(entryInfoKeys[i], context) : impl.getUserPref(entryInfoKeys[i], context));
            if(info.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
                boolean canAccessFile = false;
                try {
                    canAccessFile = impl.fileExists(info.fileURI);
                    impl.l(UMLog.DEBUG, 513, info.fileURI +':' + canAccessFile);
                }catch(IOException e) {
                    impl.l(UMLog.ERROR, 112, info.fileURI, e);
                    //this might happen if a whole volume was unmounted or something like this
                }
                
                if(!canAccessFile) {
                    //remove the entry from our listing
                    impl.l(UMLog.VERBOSE, 407, info.fileURI);
                    if(isShared) {
                        impl.setAppPref(entryInfoKeys[i], null, context);
                    }else {
                        impl.setUserPref(entryInfoKeys[i], null, context);
                    }
                }
            }
        }
    }
    
    /**
     * Scan a given set of opds files and container files (e.g. epubs) in a 
     * directory and return OPDSFeed object representing what was found
     * 
     * The resulting feed will contain an entry for each of the opdsFiles given.
     * Additionally if any of the containerFiles are not present in any of the
     * opdsFiles feeds then another entry will be added for unsorted / loose 
     * items.  It will be saved into looseContainerFile
     * 
     * @param opdsFiles Array of file paths to OPDS files
     * @param opdsFileModes Boolean array - set to true for any file that should be considered user specific
     * @param containerFiles Array of file paths to container files (e.g. epubs)
     * @param containerFileModes Boolean array - set to true for any file that should be considered user specific
     * @param looseContainerFile A file path where we can save the loose
     * @param baseHREF The base HREF for the generated feed
     * @param title Title for the generated feed
     * @param feedID ID for the generated feed
     * @return A feed object with entries for each opdsFile and if required a loose/unsorted feed
     */
    public static UstadJSOPDSFeed scanFiles(String[] opdsFiles, boolean[] opdsFileModes, String[] containerFiles, boolean[] containerFileModes, String looseContainerFile, String baseHREF, String title, String feedID, Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UstadJSOPDSFeed retVal = new UstadJSOPDSFeed(
                UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, baseHREF), 
                title, feedID);
        
        //hashtable in the form of ID to path
        Hashtable knownContainerIDs = new Hashtable();
        int i;
        int j;
        UstadJSOPDSFeed feed;
        UstadJSOPDSEntry feedEntry;
        
        for(i = 0; i < opdsFiles.length; i++) {
            try {
                //let's try reading it and loading it in
                feed = UstadJSOPDSFeed.loadFromXML(impl.readFileAsText(opdsFiles[i]));
                feedEntry = UstadJSOPDSEntry.makeEntryForItem(
                    feed, retVal, "subsection", UstadJSOPDSEntry.TYPE_NAVIGATIONFEED, 
                    UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, 
                        opdsFiles[i]));
                retVal.addEntry(feedEntry);
                for(j = 0; j < feed.entries.length; j++) {
                    knownContainerIDs.put(feed.entries[j].id, 
                            UMFileUtil.resolveLink(opdsFiles[i], 
                            UMFileUtil.getFilename(opdsFiles[i])));
                }
            }catch(Exception e) {
                impl.l(UMLog.ERROR, 114, opdsFiles[i]);
            }
        }
        feed = null;
        feedEntry = null;
        
        ZipFileHandle zipHandle;
        ZipEntryHandle zipEntryHandle;
        InputStream zIs = null;
        UstadOCF ocf;
        UstadJSOPF opf;
        UstadJSOPDSFeed looseContainerFeed = new UstadJSOPDSFeed(baseHREF, 
            "Loose files", feedID+"-loose");
        
        for(i = 0; i < containerFiles.length; i++) {
            impl.l(UMLog.VERBOSE, 408, containerFiles[i]);
            zipHandle = null;
            ocf = null;
            zIs = null;
            zipEntryHandle = null;
            
            try {
                zipHandle = impl.openZip(containerFiles[i]);
                zIs = zipHandle.openInputStream(OCF_CONTAINER_PATH);
                ocf = UstadOCF.loadFromXML(impl.newPullParser(zIs));
                UMIOUtils.closeInputStream(zIs);
                zIs = null;
                
                for(j = 0; j < ocf.rootFiles.length; j++) {
                    zIs = zipHandle.openInputStream(ocf.rootFiles[j].fullPath);
                    opf = UstadJSOPF.loadFromOPF(impl.newPullParser(zIs), 
                            UstadJSOPF.PARSE_METADATA);
                    UMIOUtils.closeInputStream(zIs);
                    zIs = null;
                    
                    if(!knownContainerIDs.containsKey(opf.id) && looseContainerFeed.getEntryById(opf.id) == null) {
                        UstadJSOPDSEntry looseEntry= new UstadJSOPDSEntry(looseContainerFeed,
                            opf, null, containerFiles[i]);
                        looseContainerFeed.addEntry(looseEntry);
                    }
                    
                    //Make sure that this entry is marked as acquired
                    int resourceMode = containerFileModes[i] ? USER_RESOURCE 
                            : SHARED_RESOURCE;
                    
                    CatalogEntryInfo thisEntryInfo = getEntryInfo(opf.id,
                        resourceMode, context);
                    if(thisEntryInfo == null) {
                        impl.l(UMLog.VERBOSE, 409, containerFiles[i]);
                        thisEntryInfo = new CatalogEntryInfo();
                        thisEntryInfo.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
                        thisEntryInfo.fileURI = containerFiles[i];
                        thisEntryInfo.mimeType = UstadJSOPDSItem.TYPE_EPUBCONTAINER;
                        thisEntryInfo.srcURLs = new String[] { containerFiles[i] };
                        setEntryInfo(opf.id, thisEntryInfo, resourceMode, context);
                    }
                }
            }catch(Exception e) {
               impl.l(UMLog.ERROR, 113, containerFiles[i], e);
            }finally {
                UMIOUtils.closeInputStream(zIs);
                UMIOUtils.closeZipFileHandle(zipHandle);
            }
        }
        
        if(looseContainerFeed.entries.length > 0) {
            try {
                impl.writeStringToFile(looseContainerFeed.toString(), looseContainerFile, 
                    "UTF-8");
            }catch(IOException e) {
                //impl.getAppView().showNotification(impl.getString(U.id.error)
                //    + " : 159", AppView.LENGTH_LONG);
                impl.l(UMLog.ERROR, 159, looseContainerFile, e);
            }
            
            retVal.addEntry(UstadJSOPDSEntry.makeEntryForItem(
                    looseContainerFeed, retVal, "subsection", UstadJSOPDSEntry.TYPE_NAVIGATIONFEED,
                    UMFileUtil.ensurePathHasPrefix(UMFileUtil.PROTOCOL_FILE, 
                        looseContainerFile)));
        }
        
        return retVal;
    }
    
    
    /**
     * Generates a String preference key for the given entryID.  Used to map
     * in the form of entryID -> EntryInfo (serialized)
     * 
     * @param entryID
     * @return 
     */
    private static String getEntryInfoKey(String entryID) {
        return PREFIX_ENTRYINFO + entryID;
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
    public static void setEntryInfo(String entryID, CatalogEntryInfo info, int resourceMode, Object context) {
        UstadMobileSystemImpl.getInstance().setPref(resourceMode == USER_RESOURCE, 
            getEntryInfoKey(entryID), info != null? info.toString(): null, context);
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
    public static CatalogEntryInfo getEntryInfo(String entryID, int resourceMode, Object context) {
        String prefKey = getEntryInfoKey(entryID);
        String entryInfoStr = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            entryInfoStr = impl.getUserPref(prefKey, context);
        }
        
        if(entryInfoStr == null && (resourceMode & SHARED_RESOURCE) ==SHARED_RESOURCE) {
            entryInfoStr = impl.getAppPref(prefKey, context);
        }
        
        if(entryInfoStr != null) {
            return CatalogEntryInfo.fromString(entryInfoStr);
        }else {
            return null;
        }
    }
    
    
    
    /**
     * Fetch and download the given containers, save required information about it to
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
     * @param request The AcquireReuqest 
     * 
     */
    public static void acquireCatalogEntries(CatalogController.AcquireRequest request) {
        UstadJSOPDSEntry[] entries = request.getEntries();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] mimeTypes = new String[entries.length];
        
        Hashtable authHeaders = makeAuthHeaders(request.getHttpUsername(), 
                request.getHttpPassword());
        int resourceMode = request.getResourceMode();
        
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
                request.getDestDirPath(), UMFileUtil.getFilename(itemHref)
            });
            
            CatalogEntryInfo info = new CatalogEntryInfo();
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.srcURLs = new String[]{itemURL};
            info.fileURI = destFilename;
            info.mimeType = mimeTypes[i];
            
            
            
            long downloadID = impl.queueFileDownload(itemURL, destFilename, authHeaders, 
                    request.getContext());
            info.downloadID = downloadID;
            setEntryInfo(entries[i].id, info, resourceMode, request.getContext());
            
            if(request.getController() != null) {
                request.getController().registerDownloadingEntry(entries[i].id, 
                        new Long(downloadID));
            }
        }
    }
    
    public int getEntryAcquisitionStatus(String entryID) {
        CatalogEntryInfo info = getEntryInfo(entryID, getResourceMode(), context);
        if(info != null) {
            return info.acquisitionStatus;
        }else {
            return CatalogEntryInfo.ACQUISITION_STATUS_NOTACQUIRED;
        }
        
    }
    
    public void registerDownloadingEntry(String entryID, Long downloadID) {
        downloadingEntries.put(entryID, downloadID);
        if(view != null) {
            view.setDownloadEntryProgressVisible(entryID, true);
        }
        
        if(downloadUpdateTimer == null) {
            downloadUpdateTimer = new Timer();
            downloadUpdateTimer.schedule(new UpdateProgressTimerTask(), 
                DOWNLOAD_UPDATE_INTERVAL, DOWNLOAD_UPDATE_INTERVAL);
            UstadMobileSystemImpl.getInstance().registerDownloadCompleteReceiver(
                    this, context);
        }
    }
    
    public void unregisterDownloadingEntry(String entryID) {
        downloadingEntries.remove(entryID);
        if(view != null) {
            view.setDownloadEntryProgressVisible(entryID, false);
        }
        
        if(downloadingEntries.size() < 1) {
            downloadUpdateTimer.cancel();
            downloadUpdateTimer = null;
            UstadMobileSystemImpl.getInstance().unregisterDownloadCompleteReceiver(
                    this, context);
        }
    }

    public void downloadStatusUpdated(UMDownloadCompleteEvent evt) {
        Enumeration downloadEntryKeys = downloadingEntries.keys();
        String entryID;
        Long downloadID;
        while(downloadEntryKeys.hasMoreElements()) {
            entryID = (String)downloadEntryKeys.nextElement();
            downloadID = (Long)downloadingEntries.get(entryID);
            if(downloadID.longValue() == evt.getDownloadID()) {
                //this means our download has completed - update status and such
                unregisterDownloadingEntry(entryID);
                registerItemAcquisitionCompleted(entryID);
                return;
            }
        }
    }
    
    protected void registerItemAcquisitionCompleted(String entryID) {
        int resMode = getResourceMode();//TODO: This is wrong: we need the download mode of the Download **NOT** the catalog itself.
        CatalogEntryInfo info = getEntryInfo(entryID, getResourceMode(), context);
        info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
        CatalogController.setEntryInfo(entryID, info, resourceMode, context);
        if(this.view != null) {
            this.view.setEntryStatus(entryID, info.acquisitionStatus);
            this.view.setDownloadEntryProgressVisible(entryID, false);
        }
    }
    
    private class UpdateProgressTimerTask extends TimerTask {

        @Override
        public void run() {
            //here we actually go and set the progress bars...
            Enumeration entries = CatalogController.this.downloadingEntries.keys();
            String entryID;
            Long downloadID;
            while(entries.hasMoreElements()) {
                entryID = (String)entries.nextElement();
                downloadID = (Long)CatalogController.this.downloadingEntries.get(entryID);
                int[] downloadStatus = UstadMobileSystemImpl.getInstance().getFileDownloadStatus(
                    downloadID.longValue(), CatalogController.this.context);
                CatalogController.this.view.updateDownloadEntryProgress(entryID, 
                    downloadStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], 
                    downloadStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL]);
            }
        }
        
    }
    
    public void handlePause() {
        //stop the download update timer here
    }
    
    public void handleResume() {
        //restart the download update timer here
    }
    
    

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
                this.view.setDownloadEntryProgressVisible(entry.id, false);
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
    
    /**
     * Class represents a request to acquire a set of OPDS entries
     */
    public static class AcquireRequest {
        
        private UstadJSOPDSEntry[] entries;
        
        private String destDirPath;
        
        private String httpUsername;
        
        private String httpPassword;
        
        private int resourceMode;
        
        private int flags;
        
        private Object context;
        
        private CatalogController controller;
        
        /**
         * @param entries The OPDS Entries that should be acquired.  Must be OPDS 
         * Entry items with acquire links.  For now the first acquisition link will
         * be used
         * TODO: Enable user specification of preferred acquisition types
         * @param destDirPath The destination directory where to save acquired entries
         * @param httpUsername optional HTTP authentication username - can be null
         * @param httpPassword optional HTTP authentication password - can be null
         * @param resourceMode SHARED_RESOURCE or USER_RESOURCE - controls where 
         * @param controller : the controller that will want to know about updates etc.  Can be null
         * we update info about this acquisition - in user prefs or in app wide prefs
         */
        public AcquireRequest(UstadJSOPDSEntry[] entries, String destDirPath, String httpUsername, String httpPassword, int resourceMode, Object context, CatalogController controller) {
            this.entries = entries;
            this.destDirPath = destDirPath;
            this.httpUsername = httpUsername;
            this.httpPassword = httpPassword;
            this.resourceMode = resourceMode;
            this.context = context;
            this.controller = controller;
        }
        
        /**
         * Get the entries to be downloaded
         * @return 
         */
        public UstadJSOPDSEntry[] getEntries() {
            return entries;
        }
        
        
        public String getDestDirPath() {
            return destDirPath;
        }
        
        public void setDestDirPath(String destDirPath) {
            this.destDirPath = destDirPath;
        }
        
        public String getHttpUsername() {
            return httpUsername;
        }
        
        public String getHttpPassword() {
            return httpPassword;
        }
        
        public int getResourceMode() {
            return resourceMode;
        }
        
        public Object getContext() {
            return context;
        }
        
        public CatalogController getController() {
            return controller;
        }
        
        
    }
    
    
    private static class AcquirePostDownloadRunnable implements Runnable {
        private UstadJSOPDSEntry[] entries;
        
        private UMTransferJob[] srcJobs;
        
        private String[] mimeTypes;
        
        private int resourceMode;
        
        private Object context;
        
        public AcquirePostDownloadRunnable(UstadJSOPDSEntry[] entries, UMTransferJob[] srcJobs, String[] mimeTypes, int resourceMode, Object context) {
            this.entries = entries;
            this.srcJobs = srcJobs;
            this.mimeTypes = mimeTypes;
            this.resourceMode = resourceMode;
            this.context = context;
        }
        
        public void run() {
            Hashtable parentFeeds = new Hashtable();
            for(int i = 0; i < entries.length; i++) {
                CatalogEntryInfo info = new CatalogEntryInfo();
                info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
                info.srcURLs = new String[]{srcJobs[i].getSource()};
                info.fileURI = srcJobs[i].getDestination();
                info.mimeType = mimeTypes[i];
                //unregisterDownloadInProgress(entries[i].id);
                CatalogController.setEntryInfo(entries[i].id, info, resourceMode, 
                        context);
                parentFeeds.put(entries[i].parentFeed, entries[i].parentFeed);
            }
            
            Enumeration parentFeedKeys = parentFeeds.keys();
            while(parentFeedKeys.hasMoreElements()) {
                UstadJSOPDSFeed parentFeed = (UstadJSOPDSFeed)parentFeedKeys.nextElement();
                try {
                    CatalogController.generateLocalCatalog(parentFeed, resourceMode, 
                    CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, context);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    /**
     * Delete the given entry
     * 
     * @param entryID
     * @param resourceMode
     */
    public static void removeEntry(String entryID, int resourceMode, Object context) {
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            actionRemoveEntry(entryID, USER_RESOURCE, context);
        }
        
        if((resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            actionRemoveEntry(entryID, SHARED_RESOURCE, context);
        }
    }
    
    private static void actionRemoveEntry(String entryID, int resourceMode, Object context) {
        CatalogEntryInfo entry = getEntryInfo(entryID, resourceMode, context);
        if(entry != null && entry.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
  	    impl.getLogger().l(UMLog.INFO, 520, entry.fileURI);
            impl.removeFile(entry.fileURI);
            setEntryInfo(entryID, null, resourceMode, context);
        }
    }
    
    public static int getAcquisitionStatusByEntryID(String entryID, String user) {
        return -1;
    }
    
    
    
}
