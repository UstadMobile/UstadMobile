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
import com.ustadmobile.core.impl.UMStorageDir;
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
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.core.view.UserSettingsView;
import com.ustadmobile.core.view.UstadView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */


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
public class CatalogController extends UstadBaseController implements AppViewChoiceListener, AsyncLoadableController, UMDownloadCompleteReceiver, Runnable {
    
    public static final int STATUS_ACQUIRED = 0;
    
    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;
    
    public static final int STATUS_NOT_ACQUIRED = 2;
    
    //Begin flags
    
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
    
    /**
     * Flag Indicates that contents should be sorted in a descending order
     */
    public static final int SORT_DESC = 8;
    
    /**
     * Flag Indicates that the contents should be sorted in an ascending manner
     */
    public static final int SORT_ASC = 16;
    
    /**
     * Indicates that the contents should not be sorted
     */
    public static final int SORT_NONE = 32;
    
    /**
     * Flag indicates that the contents should be sorted according to when they were last used
     */
    public static final int SORT_BY_LASTACCESSED = 64;
    
    /**
     * Flag Indicates that the contents should be sorted by the title
     */
    public static final int SORT_BY_TITLE = 128;
    
    
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
        U.id.onthisdevice, U.id.logout, U.id.about, U.id.settings};
    
    public static final int MENUINDEX_MYCOURSES = 0;
    
    public static final int MENUINDEX_MYDEVICE = 1;
    
    public static final int MENUINDEX_LOGOUT = 2;
    
    public static final int MENUINDEX_ABOUT = 3;
    
    public static final int MENUINDEX_SETTINGS = 4;
    
    public static final String LOCALOPDS_ID_SUFFIX = "-local";
    
    /**
     * Hardcoded OPDS extension (to save time in loops)
     */
    public static final String OPDS_EXTENSION = ".opds";
    
    public static final String CONTAINER_INFOCACHE_EXT = ".umcache";
    
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
            
    /**
     * A url that provides a list of the of the contents that have been downloaded
     * to the device
     */
    public static final String OPDS_PROTO_DEVICE = "opds:///com.ustadmobile.app.devicefeed";
    
    /**
     * The prefix that we use for the user's own feedlist
     */
    public static final String USER_FEEDLIST_ID_PREFIX = "com.ustadmobile.app.userfeedlist";
    
    /**
     * A url that provides a feed giving this users list of root feeds
     */
    public static final String OPDS_PROTO_USER_FEEDLIST = "opds:///" + USER_FEEDLIST_ID_PREFIX;
    
    /**
     * The preference key that will be used to save the user's feedlist as a
     * json string
     */
    public static final String PREFKEY_USERFEEDLIST = "userfeedlist";
    
    private Timer downloadUpdateTimer;
    
    public static final int DOWNLOAD_UPDATE_INTERVAL = 1000;
    
    //Hashtable indexed entry id -> download ID (Long object)
    private Hashtable downloadingEntries;
    
    private Thread thumbnailLoadThread;
    
    
    public CatalogController(Object context) {
        super(context);
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
    }
    
    public UstadView getView() {
        return this.view;
    }
    
    public void setUIStrings() {
        CatalogView cView = (CatalogView)view;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] menuOpts = new String[catalogMenuOptIDS.length];
        for(int i = 0; i < menuOpts.length; i++) {
            menuOpts[i] = impl.getString(catalogMenuOptIDS[i]);
        }
        
        cView.setMenuOptions(menuOpts);
        cView.setDirection(UstadMobileSystemImpl.getInstance().getDirection());
        
    }
    
    
    public void setView(UstadView view) {
        super.setView(view);
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
     * @return 
     */
    public static CatalogController makeControllerByURL(String url, int resourceMode, String httpUser, String httpPassword, int flags, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = CatalogController.getCatalogByURL(url, resourceMode, 
            httpUser, httpPassword, flags, context);
        
        if((flags & SORT_ASC) == SORT_ASC || (flags & SORT_DESC) == SORT_DESC) {
            if((flags & SORT_BY_LASTACCESSED) == SORT_BY_LASTACCESSED) {
                opdsFeed.sortEntries(new 
                    CatalogLastAccessTimeComparer(flags, context));
            }
        }
        
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
     * @param resourceMode as per makeControllerByURL 
     * @param flags as per makeControllerByURL
     * 
     * @see CatalogController#makeControllerByURL(java.lang.String, com.ustadmobile.core.impl.UstadMobileSystemImpl, int, java.lang.String, java.lang.String, int) 
     * 
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public static void makeControllerForView(final CatalogView view, final String url, final int resourceMode, final int flags, final ControllerReadyListener listener) {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        Object ctx = view.getContext();
        
        args.put(KEY_URL, url);
        args.put(KEY_RESMOD, new Integer(resourceMode));
        args.put(KEY_FLAGS, new Integer(flags));
        
        if(impl.getActiveUser(ctx) != null && impl.getActiveUserAuth(ctx) != null) {
            args.put(KEY_HTTPUSER, impl.getActiveUser(ctx));
            args.put(KEY_HTTPPPASS, impl.getActiveUserAuth(ctx));
        }
        
        CatalogController controller = new CatalogController(ctx);
        new LoadControllerThread(args, controller, listener, view).start();
    }

    
    public UstadController loadController(Hashtable args, Object ctx) throws Exception {
        CatalogController newController = makeControllerByURL((String)args.get(KEY_URL), 
            ((Integer)args.get(KEY_RESMOD)).intValue(), 
            args.get(KEY_HTTPUSER) != null ? (String)args.get(KEY_HTTPUSER) : null, 
            args.get(KEY_HTTPPPASS) != null ? (String)args.get(KEY_HTTPPPASS) : null, 
            ((Integer)args.get(KEY_FLAGS)).intValue(), ctx);
        newController.initEntryStatusCheck();
        return newController;
    }
    
    /**
     * Asynchronously load thumbnails for this controller and set them on the
     * view accordingly.  This will fork a new thread using itself as the
     * the runnable target.
     * 
     */
    public void loadThumbnails() {
        if(thumbnailLoadThread == null) {
            thumbnailLoadThread = new Thread(this);
            thumbnailLoadThread.start();
        }
    }
    
    
    /**
     * This is the runnable target method that actually loads thumbnails
     */
    public void run() {
        UstadJSOPDSFeed feed = model.opdsFeed;
        String[] thumbnailLinks;
        HTTPCacheDir cache = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(
            resourceMode, context);
            
        String thumbnailFile;
        
        for(int i = 0; i < feed.entries.length; i++) {
            thumbnailLinks = feed.entries[i].getThumbnailLink(false);
            thumbnailFile = null;
            if(thumbnailLinks != null) {
                String thumbnailURI = UMFileUtil.resolveLink(
                        feed.href, thumbnailLinks[UstadJSOPDSEntry.LINK_HREF]);
                if(thumbnailURI.startsWith("file://")) {
                    thumbnailFile = thumbnailURI;//this is already on disk...
                }else {
                    try {
                        thumbnailFile = cache.get(thumbnailURI);
                    }catch(Exception e) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, 132, 
                            feed.entries[i].title + ": " + feed.entries[i].id, e);
                    }
                }
                
                if(thumbnailFile != null) {
                    view.setEntrythumbnail(feed.entries[i].id, thumbnailFile);
                }
            }
        }
    }
    
    
    /**
     * Make a CatalogController for the user's default OPDS catalog
     * 
     * @param impl system implementation to be used
     * 
     * @return CatalogController representing the default catalog for the active user
     */
    public static CatalogController makeUserCatalog(UstadMobileSystemImpl impl, Object context) throws IOException, XmlPullParserException{
        String opdsServerURL = impl.getUserPref("opds_server_primary", 
            UstadMobileDefaults.DEFAULT_OPDS_SERVER, context);
        
        String activeUser = impl.getActiveUser(context);
        String activeUserAuth = impl.getActiveUserAuth(context);
        return CatalogController.makeControllerByURL(opdsServerURL, 
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
    
    
    private static void putStorageDirsInVectors(UMStorageDir[] dirs, boolean userSpecific, Vector opdsFilesVector, Vector containerFilesVector) {
        for(int i = 0; i < dirs.length; i++) {
            if(dirs[i].isAvailable() && dirs[i].isUserSpecific() == userSpecific) {
                try {
                    boolean dirExists = UstadMobileSystemImpl.getInstance().dirExists(
                            dirs[i].getDirURI());
                    if(dirExists) {
                        findOPDSFilesInDir(dirs[i].getDirURI(), opdsFilesVector, 
                            containerFilesVector);
                    }
                }catch(IOException ioe) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 177, dirs[i].getDirURI());
                }
            }
        }
    }
    
    public static String getUserFeedListIdPrefix(Object context) {
        return OPDS_PROTO_USER_FEEDLIST + 
            UstadMobileSystemImpl.getInstance().getActiveUser(context);
    }
    
    /**
     * 
     * @param context
     * @return 
     */
    public static UstadJSOPDSFeed makeUserFeedListFeeed(Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String rootFeedsStr = impl.getUserPref(PREFKEY_USERFEEDLIST, 
            UstadMobileDefaults.DEFAULT_FEEDLIST, context);
        String feedID =  getUserFeedListIdPrefix(context);
        UstadJSOPDSFeed usersFeeds = new UstadJSOPDSFeed(OPDS_PROTO_USER_FEEDLIST,
                "My Feeds",feedID);
        JSONArray arr = BasePointController.getUserFeedListArray(context);
        try {
            JSONObject currentFeed;
            UstadJSOPDSEntry newEntry;
            for(int i = 0; i < arr.length(); i++) {
                currentFeed = arr.getJSONObject(i);
                 newEntry = new UstadJSOPDSEntry(usersFeeds, 
                    currentFeed.getString("title"), feedID + i, "subsection", 
                    UstadJSOPDSEntry.TYPE_NAVIGATIONFEED, 
                    currentFeed.getString("url"));
                 usersFeeds.addEntry(newEntry);
            }
        }catch(JSONException e) {
            
        }
        
        return usersFeeds;
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
    public static UstadJSOPDSFeed makeDeviceFeed(UMStorageDir[] dirs, int dirFlags, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        boolean incShared = (dirFlags & SHARED_RESOURCE) == SHARED_RESOURCE;
        boolean incUser = (dirFlags & USER_RESOURCE) == USER_RESOURCE;
        
        verifyKnownEntries(SHARED_RESOURCE, context);
        
        Vector opdsFilesVector = new Vector();
        int opdsUserStartIndex = 0;
        Vector containerFilesVector = new Vector();
        int containerUserStartIndex = 0;
                
        
        if(incShared) {
            putStorageDirsInVectors(dirs, false, opdsFilesVector, containerFilesVector);
            opdsUserStartIndex = opdsFilesVector.size();
            containerUserStartIndex = containerFilesVector.size();
        }
        
        if(incUser) {
            putStorageDirsInVectors(dirs, true, opdsFilesVector, containerFilesVector);
        }
        
        String[] opdsFiles = new String[opdsFilesVector.size()];
        opdsFilesVector.copyInto(opdsFiles);
        opdsFilesVector = null;
        
        String[] containerFiles = new String[containerFilesVector.size()];
        containerFilesVector.copyInto(containerFiles);
        containerFilesVector = null;
        
        String generatedHREFBase = incUser ? impl.getUserContentDirectory(
                impl.getActiveUser(context)) : impl.getSharedContentDir();
        
        String looseFilePath = UMFileUtil.joinPaths(new String[] {
            impl.getCacheDir(incUser ? USER_RESOURCE : SHARED_RESOURCE, context), 
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
                USER_RESOURCE | SHARED_RESOURCE, getContext());
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
                        
            if(entryLinks.size() > 0) {
                String[] firstLink = (String[])entryLinks.elementAt(0);
                final String url = UMFileUtil.resolveLink(entry.parentFeed.href, 
                    firstLink[UstadJSOPDSItem.LINK_HREF]);
                
                Hashtable args = new Hashtable();
                args.put(KEY_URL, url);
                
                if(impl.getActiveUser(getContext()) != null) {
                    args.put(KEY_HTTPUSER, impl.getActiveUser(getContext()));
                    args.put(KEY_HTTPPPASS, impl.getActiveUserAuth(getContext()));
                }
                
                args.put(KEY_RESMOD, new Integer(getResourceMode()));
                args.put(KEY_FLAGS, new Integer(CACHE_ENABLED));
                
                
                UstadMobileSystemImpl.getInstance().go(CatalogView.class, args, 
                        getContext());
            }
        }else {
            CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id, 
                    SHARED_RESOURCE | USER_RESOURCE, getContext());
            if(entryInfo != null && entryInfo.acquisitionStatus == STATUS_ACQUIRED) {
                Hashtable openArgs = new Hashtable();
                openArgs.put(ContainerController.ARG_CONTAINERURI, entryInfo.fileURI);
                openArgs.put(ContainerController.ARG_MIMETYPE, entryInfo.mimeType);
                UstadMobileSystemImpl.getInstance().go(ContainerView.class, openArgs, 
                        getContext());
            }else if(isInProgress(entry.id)){
                UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
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
        UMStorageDir[] allDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
            storageMode, getContext());
        Vector availableVector = new Vector();
        for(int i = 0; i < allDirs.length; i++) {
            if(allDirs[i].isAvailable()) {
                availableVector.addElement(allDirs[i]);
            }
        }
        availableStorageDirs = new UMStorageDir[availableVector.size()];
        availableVector.copyInto(availableStorageDirs);
        
        String[] storageChoices = new String[availableStorageDirs.length];
        for(int i = 0; i < storageChoices.length; i++) {
            storageChoices[i] = availableStorageDirs[i].getName();
        }
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showChoiceDialog(
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
        AppView appView = UstadMobileSystemImpl.getInstance().getAppView(getContext());
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
                UstadMobileSystemImpl.l(UMLog.DEBUG, 526, "#" + choice + ": " 
                    + destDirURI);
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
        impl.getAppView(getContext()).showChoiceDialog(impl.getString(U.id.download_for),
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
        final AcquireRequest request = new AcquireRequest(entries, destDirURI, 
            impl.getActiveUser(getContext()), impl.getActiveUserAuth(getContext()), 
            selectedDownloadMode, getContext(), this);
        this.view.setSelectedEntries(new UstadJSOPDSEntry[0]);
        
        Thread startDownloadThread = new Thread(new Runnable() {
            public void run() {
                CatalogController.acquireCatalogEntries(request);
            }
        });
        startDownloadThread.start();
        
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
                    null, getContext());
                break;
            case MENUINDEX_MYDEVICE:
                Hashtable args = new Hashtable();
                args.put(KEY_URL, OPDS_PROTO_DEVICE);
                args.put(KEY_RESMOD, new Integer(USER_RESOURCE | SHARED_RESOURCE));
                args.put(KEY_FLAGS, new Integer(CACHE_ENABLED));
                
                UstadMobileSystemImpl.getInstance().go(CatalogView.class,
                        args, getContext());
                
                break;
            case MENUINDEX_SETTINGS:
                UstadMobileSystemImpl.getInstance().go(UserSettingsView.class, 
                        new Hashtable(), getContext());
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
     * @return The OPDS Feed object representing this catalog or null if it cannot be accessed
     */
    public static UstadJSOPDSFeed getCatalogByURL(String url, int resourceMode, String httpUsername, String httpPassword, int flags, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = null;
        Exception e = null; 
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getLogger().l(UMLog.INFO, 307, url);
        
        Hashtable headers = makeAuthHeaders(httpUsername, httpPassword);
        InputStream catalogIn = null;
        
        if(url.startsWith("opds:///")) {
            if(url.equals(OPDS_PROTO_DEVICE)) {
                opdsFeed = makeDeviceFeed(
                    impl.getStorageDirs(resourceMode, context), 
                    resourceMode, context);
            }else if(url.equals(OPDS_PROTO_USER_FEEDLIST)) {
                opdsFeed = makeUserFeedListFeeed(context);
            }
        }else {
            try {
                XmlPullParser parser = UstadMobileSystemImpl.getInstance().newPullParser();
                String catalogID = getCatalogIDByURL(url, resourceMode, context);
                HTTPResult[] resultBuf = new HTTPResult[1];
                String opdsFileURI = null;
                
                if(url.startsWith("http://") || url.startsWith("https://")) {
                    HTTPCacheDir primaryCacheDir = null;
                    HTTPCacheDir fallbackCache = null;


                    String filename = null;
                    if(catalogID != null) {
                        filename = getFileNameForOPDSFeedId(catalogID);
                    }

                    if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
                        primaryCacheDir = impl.getHTTPCacheDir(USER_RESOURCE, context);
                    }

                    if((resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
                        if(primaryCacheDir == null) {
                            primaryCacheDir = impl.getHTTPCacheDir(SHARED_RESOURCE, 
                                context);
                        }else {
                            fallbackCache = impl.getHTTPCacheDir(SHARED_RESOURCE, 
                                context);
                        }
                    }

                    opdsFileURI = primaryCacheDir.get(url, filename, headers, 
                        resultBuf, fallbackCache);
                }else if(url.startsWith("file://")) {
                    opdsFileURI = url;
                }
                
                
                if(opdsFileURI == null) {
                    throw new IOException("HTTP Error : could not cache file: " 
                        + url);
                }
                
                if(resultBuf[0] != null) {
                    catalogIn = new ByteArrayInputStream(resultBuf[0].getResponse());
                }else {
                    catalogIn = impl.openFileInputStream(opdsFileURI);
                }
                
                parser.setInput(catalogIn, "UTF-8");
                opdsFeed = UstadJSOPDSFeed.loadFromXML(parser);
                opdsFeed.href = url;
                CatalogController.cacheCatalog(opdsFeed, resourceMode, context);
            }catch(Exception e1) {
                UstadMobileSystemImpl.l(UMLog.WARN, 201, url, e1);
                e = e1;
            }
        }
        
        //If we have loaded it from remote server or cache it's not null here
        if(opdsFeed != null) {
            impl.getLogger().l(UMLog.DEBUG, 504, "Catalog Null:" + (opdsFeed == null));
            opdsFeed.href = url;
            stripEntryUMCloudIDPrefix(opdsFeed);
        }else if(e != null){
            if(e instanceof IOException) {
                throw (IOException)e;
            }else if(e instanceof XmlPullParserException) {
                throw (XmlPullParserException)e;
            }else {
                throw new IOException(e.toString());
            }
        }else {
            throw new IOException("OPDS Catalog not loaded: " + url);
        }
        
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
    public static String getCatalogIDByURL(String url, int resourceMode, Object context) {
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
     * @param resourceMode 
     * @param context
     * @throws IOException
     */
    public static void cacheCatalog(UstadJSOPDSFeed catalog, int resourceMode, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getLogger().l(UMLog.VERBOSE, 405, "id: " + catalog.id + " href: " + catalog.href);
        
        
        boolean isUserMode = (resourceMode & USER_RESOURCE) == USER_RESOURCE;
                            
        impl.getLogger().l(UMLog.DEBUG, 505, catalog.id + "/mode:" + resourceMode);
	
        String filename = getFileNameForOPDSFeedId(catalog.id);        
        String idKeyName = "opds-cache-" + catalog.id;
	String urlKeyName = getPrefKeyNameForOPDSURLToIDMap(catalog.href);
        
        impl.setPref(isUserMode, idKeyName, filename, context);
        impl.setPref(isUserMode, urlKeyName, catalog.id, context);
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
        HTTPCacheDir cacheDir = null;
        impl.getLogger().l(UMLog.VERBOSE, 406, catalogID);
        
        String filename = null;
        
        String key = "opds-cache-" + catalogID;
        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            filename = impl.getUserPref(key, context);
            cacheDir = impl.getHTTPCacheDir(USER_RESOURCE, context);
            impl.getLogger().l(UMLog.DEBUG, 509, filename);
        }
        
        if(filename == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            filename = impl.getAppPref(key, context);
            cacheDir = impl.getHTTPCacheDir(SHARED_RESOURCE, context);
            impl.getLogger().l(UMLog.DEBUG, 510, filename);
        }


        if(filename != null) {
            String contentsXML = impl.readFileAsText(
                cacheDir.getCacheFileURIByFilename(filename), "UTF-8");
            UstadJSOPDSFeed feed = UstadJSOPDSFeed.loadFromXML(contentsXML);
            return feed;
        }
	
        return null;
    }
    
    /**
     * Get a cached copy of a given OPDS catalog by URL
     * 
     * @param url The OPDS url e.g. http://server.com/dir/place.opds
     * @param resourceMode Where to look : flag can set SHARED_RESOURCE or USER_RESOURCE
     * 
     * @return the feed if it is available in the cache, null otherwise
     */
    public static UstadJSOPDSFeed getCachedCatalogByURL(String url, int resourceMode, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed retVal = null;
        String entryID = getCatalogIDByURL(url, resourceMode, context);
        if(entryID != null) {
            return getCachedCatalogByID(entryID, resourceMode, context);
        }else {
            return null;
        }
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
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 429, dir);
        
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
        
        UstadJSOPDSFeed looseContainerFeed = new UstadJSOPDSFeed(baseHREF, 
            "Loose files", feedID+"-loose");
        Hashtable foundContainerIDS = new Hashtable();
        
        int i;
        int j;
        UstadJSOPDSEntry epubEntry;
        UstadJSOPDSFeed containerFeed;
        String entryCacheFile;
        
        for(i = 0; i < containerFiles.length; i++) {
            containerFeed = null;
            impl.l(UMLog.VERBOSE, 408, containerFiles[i]);
            
            try {
                entryCacheFile = containerFiles[i] + CONTAINER_INFOCACHE_EXT;
                //see oif
                if(impl.fileExists(entryCacheFile) && impl.fileLastModified(entryCacheFile) > impl.fileLastModified(containerFiles[i])) {
                    try { 
                        containerFeed = UstadJSOPDSFeed.loadFromXML(
                            impl.readFileAsText(entryCacheFile, "UTF-8"));
                    }catch(IOException e) {
                        impl.l(UMLog.ERROR, 140, entryCacheFile, e);
                    }
                }
                
                if(containerFeed == null) {
                    containerFeed = ContainerController.generateContainerFeed(
                        containerFiles[i], containerFiles[i] + CONTAINER_INFOCACHE_EXT);
                    try {
                        impl.writeStringToFile(containerFeed.toString(), 
                            entryCacheFile, "UTF-8");
                    }catch(IOException e) {
                        impl.l(UMLog.ERROR, 138, entryCacheFile, e);
                    }
                }
                
                
                
                for(j = 0; j < containerFeed.entries.length; j++) {
                    epubEntry =new UstadJSOPDSEntry(retVal,
                        containerFeed.entries[j]);
                    
                    if(!foundContainerIDS.containsKey(epubEntry.id)) {
                        retVal.addEntry(epubEntry);
                        foundContainerIDS.put(epubEntry.id, epubEntry.id);
                    }
                                        
                    //Make sure that this entry is marked as acquired
                    int resourceMode = containerFileModes[i] ? USER_RESOURCE 
                            : SHARED_RESOURCE;
                    
                    CatalogEntryInfo thisEntryInfo = getEntryInfo(
                        containerFeed.entries[j].id, resourceMode, context);
                    if(thisEntryInfo == null) {
                        impl.l(UMLog.VERBOSE, 409, containerFiles[i]);
                        thisEntryInfo = new CatalogEntryInfo();
                        thisEntryInfo.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
                        thisEntryInfo.fileURI = containerFiles[i];
                        thisEntryInfo.mimeType = UstadJSOPDSItem.TYPE_EPUBCONTAINER;
                        thisEntryInfo.srcURLs = new String[] { containerFiles[i] };
                        setEntryInfo(containerFeed.entries[j].id, thisEntryInfo, 
                            resourceMode, context);
                    }
                }
            }catch(Exception e) {
               impl.l(UMLog.ERROR, 113, containerFiles[i], e);
            }
        }
        
        //hashtable in the form of ID to path
        Hashtable knownContainerIDs = new Hashtable();
        
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
     * Find the best acquisition link according to the given request parameters
     * 
     * Right now: this is looking at matching the width and height. If the request
     * has no specified width and height: prefer the link with no specified
     * width and height.  If there is a preferred (e.g. screen) width and height
     * prefer the source that is the smallest available that matches the
     * is at least as big as the preferred width and height
     * 
     * TODO: Handle that width and height (e.g. orientation) are interchangable.
     * 
     * @param request
     * @param entry
     * @return 
     */
    public static String[] getBestAcquisitionLinkByParams(CatalogController.AcquireRequest request, UstadJSOPDSEntry entry) {
        Vector acquisitionLinks = entry.getAcquisitionLinks();
        if(acquisitionLinks == null || acquisitionLinks.size() == 0) {
            return null;
        }
        
        String[] preferredLink = null;
        int[] preferredSize = request.getPreferredResolution();
        int[] bestSizeMatch = null;
        String[] linkInfo;
        
        Hashtable mimeParams;
        
        for(int i = 0; i < acquisitionLinks.size(); i++) {
            linkInfo = (String[])acquisitionLinks.elementAt(i);
            mimeParams = UMFileUtil.parseTypeWithParamHeader(
                linkInfo[UstadJSOPDSItem.LINK_MIMETYPE]).params;
            if(preferredSize == null && mimeParams == null) {
                //we want the screen size neutral version and this is it
                preferredLink = linkInfo;
                break;
            }else if(preferredLink == null) {
                //we have no other link right now - take this one
                preferredLink = linkInfo;
                bestSizeMatch = getScreenSizeFromMimeParams(mimeParams);
            }else if(preferredSize != null) {
                int[] thisLinkSize = getScreenSizeFromMimeParams(mimeParams);
                if(thisLinkSize == null) {
                    continue;
                }
                
                boolean fitsScreen = thisLinkSize[0] >= preferredSize[0] && thisLinkSize[1] >= preferredSize[1];
                boolean smallerThanLast = true;
                if(bestSizeMatch != null) {
                    smallerThanLast= (thisLinkSize[0] * thisLinkSize[1]) < (bestSizeMatch[0] * bestSizeMatch[1]);
                }
                
                if(fitsScreen && smallerThanLast) {
                    preferredLink = linkInfo;
                    bestSizeMatch = thisLinkSize;
                }
            }
        }
        
        return preferredLink;
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
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 433, request.getDestDirPath());
        
        String[] preferredLink;        
        String itemHref;
        String itemURL;
        String suggestedFilename;
        String mimeWithoutParams;
        String requiredExtension;
        
        for(int i = 0; i < entries.length; i++) {
            preferredLink = getBestAcquisitionLinkByParams(request, entries[i]);
            
            if(preferredLink == null) {
                continue;
            }
            
            itemHref = preferredLink[UstadJSOPDSItem.LINK_HREF];
            mimeTypes[i] = preferredLink[UstadJSOPDSItem.LINK_MIMETYPE];
            
            itemURL = UMFileUtil.resolveLink(entries[i].parentFeed.href, 
                    itemHref);
            
            CatalogEntryInfo info = new CatalogEntryInfo();
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.srcURLs = new String[]{itemURL};
            
            
            HTTPResult result = null;
            try {
                result = impl.makeRequest(itemURL, null, null,"HEAD");
                suggestedFilename = result.getSuggestedFilename(itemURL);
                info.downloadTotalSize  = result.getContentLength();
            }catch(Exception e) {
                //the info we want isn't available right now ... can still try to queue it...
                suggestedFilename = UMFileUtil.getFilename(itemURL);
                info.downloadTotalSize = HTTPResult.HTTP_SIZE_IO_EXCEPTION;
            }
            
            mimeWithoutParams = UMFileUtil.stripMimeParams(mimeTypes[i]);
            requiredExtension = impl.getExtensionFromMimeType(mimeWithoutParams);
            if(requiredExtension != null) {
                suggestedFilename = UMFileUtil.ensurePathHasSuffix(
                    '.' + requiredExtension, suggestedFilename);
            }
            
            saveThumbnail(entries[i], request, suggestedFilename);
            info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS;
            info.srcURLs = new String[]{itemURL};
            info.fileURI = UMFileUtil.joinPaths(new String[] {
                request.getDestDirPath(), suggestedFilename
            });;
            info.mimeType = mimeTypes[i];
            
            UstadMobileSystemImpl.l(UMLog.VERBOSE, 435, itemURL + "->" + 
                info.fileURI);
            long downloadID = impl.queueFileDownload(itemURL, info.fileURI, 
                authHeaders, request.getContext());
            info.downloadID = downloadID;
            setEntryInfo(entries[i].id, info, resourceMode, request.getContext());
            
            if(request.getController() != null) {
                request.getController().registerDownloadingEntry(entries[i].id, 
                        new Long(downloadID));
            }
        }
    }
    
    /**
     * Grabs the Thumbnail for an entry that is being downloaded from the HTTP
     * cache and puts it into the same directory as containername.thumb.filetype
     * e.g. bookname.epub.thumb.png
     * 
     * @param entry The entry that we want to get a thumbnail for
     * @param request The Acquisition request being acted upon
     * @param containerFilename the filename of the container being downloaded e.g. bookname.epub
     * @return The base name of the thumbnail if it was found and copied, null otherwise
     */
    public static String saveThumbnail(UstadJSOPDSEntry entry, AcquireRequest request, String containerFilename) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] thumbnailLink = entry.getThumbnailLink(false);
        String result = null;
        
        if(thumbnailLink != null) {
            String thumbnailURL = UMFileUtil.resolveLink(
                entry.parentFeed.href, 
                thumbnailLink[UstadJSOPDSEntry.LINK_HREF]);
                    
            String thumbnailFile = impl.getHTTPCacheDir(request.getResourceMode(),
                request.getContext()).getCacheFileURIByURL(thumbnailURL);
            
            if(thumbnailFile != null) {
                //we have the thumbnail file in the cahce... let's copy it
                InputStream in = null;
                OutputStream out = null;
                String extension = impl.getExtensionFromMimeType(
                    thumbnailLink[UstadJSOPDSEntry.LINK_MIMETYPE]);
                String thumbnailFilename = containerFilename + ".thumb." + 
                    extension;
                try {
                    in = impl.openFileInputStream(thumbnailFile);
                    out = impl.openFileOutputStream(UMFileUtil.joinPaths(
                        new String[]{request.getDestDirPath(), thumbnailFilename}), 0);
                    UMIOUtils.readFully(in, out, 1024);
                    result = thumbnailFilename;
                }catch(Exception e) {
                    impl.l(UMLog.ERROR, 119, containerFilename, e);
                }finally {
                    UMIOUtils.closeInputStream(in);
                    UMIOUtils.closeOutputStream(out);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Figure out the width and height (if any) present in the mime parameters
     * in the given hashtable (which may be null if there are none)
     * 
     * @param mimeParams Mime parameters as a hashtable
     * 
     * @return int[]{width,height} of screen size params given; null if there are none
     */
    private static int[] getScreenSizeFromMimeParams(Hashtable mimeParams) {
        if(mimeParams != null && mimeParams.containsKey(UstadMobileConstants.MIMEPARAM_WIDTH) && mimeParams.containsKey(UstadMobileConstants.MIMEPARAM_HEIGHT)) {
            return new int[] { 
                Integer.parseInt(mimeParams.get(UstadMobileConstants.MIMEPARAM_WIDTH).toString()),
                Integer.parseInt(mimeParams.get(UstadMobileConstants.MIMEPARAM_HEIGHT).toString())
            };
        }else {
            return null;
        }
    }
    
    /**
     * Figure out what search mode to use: if a user is logged in set the
     * USER_RESOURCE flag in the return value, otherwise set only the 
     * SHARED_RESOURCE flag in the return value
     * 
     * @return SHARED_RESOURCE if no active user, SHARED_RESOURC | USER_RESOURCE if there's an active user
     */
    protected int determineSearchMode() {
        int searchMode = SHARED_RESOURCE;
        if(UstadMobileSystemImpl.getInstance().getActiveUser(getContext()) != null) {
            searchMode |= USER_RESOURCE;
        }
        
        return searchMode;
    }
    
    public int getEntryAcquisitionStatus(String entryID) {
        CatalogEntryInfo info = getEntryInfo(entryID, determineSearchMode(), getContext());
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
        
        startDownloadTimer();
    }
    
    /**
     * Used when the view is attached to see what the status of entries are right
     * now.
     * 
     */
    private synchronized void initEntryStatusCheck() {
        CatalogEntryInfo info;
        UstadJSOPDSFeed feed = getModel().opdsFeed;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        for(int i = 0; i< feed.entries.length; i++) {
            info = getEntryInfo(feed.entries[i].id, determineSearchMode(), getContext());
            if(info != null){
                if(info.acquisitionStatus == CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS) {
                    int[] fileDownloadStatus = impl.getFileDownloadStatus(info.downloadID, getContext());
                    if(fileDownloadStatus != null) {
                        int downloadStatus = fileDownloadStatus[UstadMobileSystemImpl.IDX_STATUS];
                        switch(downloadStatus) {
                            case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                                registerItemAcquisitionCompleted(feed.entries[i].id);
                                break;
                            case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                            case UstadMobileSystemImpl.DLSTATUS_PENDING:
                            case UstadMobileSystemImpl.DLSTATUS_PAUSED:
                                registerDownloadingEntry(feed.entries[i].id, 
                                    new Long(info.downloadID));
                        }
                    }else {
                        //perhaps the system is not tracking the download anymore and it's actually complete
                        int downloadedSize = 
                            (int)UstadMobileSystemImpl.getInstance().fileSize(info.fileURI);
                        if(downloadedSize != -1 && downloadedSize == info.downloadTotalSize) 
                            //this download has in fact completed
                            registerItemAcquisitionCompleted(feed.entries[i].id);
                    }
                }
            }
        }
    }
    
    /**
     * Starts a Timer to watch the status of downloads.  Calling this twice
     * will have no effect - a new timer will only start if there is none currently
     * running for this controller
     */
    private synchronized void startDownloadTimer() {
        if(downloadUpdateTimer == null) {
            downloadUpdateTimer = new Timer();
            downloadUpdateTimer.schedule(new UpdateProgressTimerTask(), 
                DOWNLOAD_UPDATE_INTERVAL, DOWNLOAD_UPDATE_INTERVAL);
            UstadMobileSystemImpl.getInstance().registerDownloadCompleteReceiver(
                    this, getContext());
        }
    }
    
    /**
     * If the timer to run downloads is running - stop it
     */
    private synchronized void stopDownloadTimer() {
        if(downloadUpdateTimer != null) {
            downloadUpdateTimer.cancel();
            downloadUpdateTimer = null;
            UstadMobileSystemImpl.getInstance().unregisterDownloadCompleteReceiver(
                    this, getContext());
        }
    }
    
    
    public void unregisterDownloadingEntry(String entryID) {
        downloadingEntries.remove(entryID);
        if(view != null) {
            view.setDownloadEntryProgressVisible(entryID, false);
        }
        
        if(downloadingEntries.size() < 1) {
            stopDownloadTimer();
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
        //first lookup as a user storage only download: if it's not - see if it is a shared space download
        int entryAcquireResMode = USER_RESOURCE;
        CatalogEntryInfo info = getEntryInfo(entryID, USER_RESOURCE, getContext());
        if(info == null) {
            info = getEntryInfo(entryID, SHARED_RESOURCE, getContext());
            entryAcquireResMode = SHARED_RESOURCE;
        }
        
        info.acquisitionStatus = CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED;
        CatalogController.setEntryInfo(entryID, info, entryAcquireResMode, getContext());
        if(this.view != null) {
            this.view.setEntryStatus(entryID, info.acquisitionStatus);
            this.view.setDownloadEntryProgressVisible(entryID, false);
        }
    }

    private class UpdateProgressTimerTask extends TimerTask {

        public void run() {
            //here we actually go and set the progress bars...
            Enumeration entries = CatalogController.this.downloadingEntries.keys();
            String entryID;
            Long downloadID;
            while(entries.hasMoreElements()) {
                entryID = (String)entries.nextElement();
                downloadID = (Long)CatalogController.this.downloadingEntries.get(entryID);
                int[] downloadStatus = UstadMobileSystemImpl.getInstance().getFileDownloadStatus(
                    downloadID.longValue(), CatalogController.this.getContext());
                if(CatalogController.this.view != null) {
                    CatalogController.this.view.updateDownloadEntryProgress(entryID, 
                        downloadStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], 
                        downloadStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL]);
                }
            }
        }
        
    }
    
    public void handleViewPause() {
        stopDownloadTimer();
    }
    
    public void handleViewResume() {
        startDownloadTimer();
    }
    
    public void handleViewDestroy() {
        stopDownloadTimer();
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
        
        private int[] preferredResolution;
        
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
            this.preferredResolution = null;
        }
        
        /**
         * The preferred resolution for download of resources: if they have 
         * width and height attributes look for the smallest one that fits
         * both width and height.
         * 
         * @return Preferred dimension as int[] width, height or null for no preferred dimension (e.g. use original unscaled version)
         */
        public int[] getPreferredResolution() {
            return preferredResolution;
        }
        
        /**
         * he preferred resolution for download of resources: if they have 
         * width and height attributes look for the smallest one that fits
         * both width and height.
         * 
         * @param preferredResolution Preferred dimension as int[] width, height or null for no preferred dimension (e.g. use original unscaled version)
         */
        public void setPreferredResolution(int[] preferredResolution) {
            this.preferredResolution= preferredResolution;
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
