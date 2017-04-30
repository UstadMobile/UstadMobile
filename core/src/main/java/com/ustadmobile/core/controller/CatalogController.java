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

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.AcquisitionStatusEvent;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CatalogModel;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.ContainerView;
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
import org.xmlpull.v1.XmlSerializer;


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
public class CatalogController extends BaseCatalogController implements AppViewChoiceListener, AsyncLoadableController, UMDownloadCompleteReceiver, Runnable {
    
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
    
    /**
     * Flag indicates that the cache must be disabled: applies the cache-control
     * header no-cache
     */
    public static final int CACHE_DISABLED = 256;
    
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
     * The locations available for a user to store downloaded content
     */
    private UMStorageDir[] availableStorageDirs;    
    
    private int resourceMode;
    
    //The View (J2ME or Android)
    private CatalogView view;
    
    //this is where the feed (and its entries) live.
    private CatalogModel model;
    
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
    
    public static final String PDF_EXTENSION = ".pdf";
    
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
    
    /**
     * Flag to indicate that the user is being asked to choose a download format
     */
    public static final int CMD_SELECT_FORMAT = 30;
    
    private static final int CHOICE_DOWNLOAD_SHARED = 0;
    
    private static final int CHOICE_DOWNLOAD_USER = 1;
    
    private static final int CHOICE_DOWNLOAD_CANCEL = 2;
    
    
    public static final String KEY_URL = "url";
    
    public static final String KEY_RESMOD = "resmod";
    
    public static final String KEY_HTTPUSER = "httpu";
    
    public static final String KEY_HTTPPPASS = "httpp";
    
    public static final String KEY_FLAGS = "flags";

    public static final String KEY_BROWSE_BUTTON_URL = "browesbtnu";
            
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
    
    public static final int CACHEDIR_PRIMARY = 0;
    
    public static final int CACHEDIR_FALLBACK = 1;
    
    //Hashtable indexed entry id -> download ID (Long object)
    private Hashtable downloadingEntries;
    
    private Thread thumbnailLoadThread;

    //True if this is a user's own catalog feed that they can add/remove from - false otherwise
    private boolean isUserCatalogFeed = false;

    public static final int OPDS_SELECTPROMPT = 0;

    public static final int OPDS_CUSTOM = 1;

    public static final int OPDS_FEEDS_INDEX_URL = 0;

    public static final int OPDS_FEEDS_INDEX_TITLE = 1;


    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * with file URLs
     */
    public static final int LINK_HREF_MODE_FILE = 0;

    /**
     * Flag for use with scanFiles: indicates that the feed acquisition links should be set using
     * ids e.g. baseHref/containerId
     */
    public static final int LINK_HREF_MODE_ID = 1;

    /**
     * Constant representing the link type for background images in course listings - this is a
     * non-standard link and requires AppConfig.OPDS_ITEM_ENABLE_BACKGROUNDS to be set to true
     *
     * Constant value: "http://www.ustadmobile.com/catalog/image/background"
     */
    public static final String OPDS_ENTRY_BACKGROUND_LINKREL = "http://www.ustadmobile.com/catalog/image/background";

    private String browseButtonURL;

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

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @return The OPDS URL for the browse button; null if there is none (default)
     */
    public String getBrowseButtonURL() {
        return browseButtonURL;
    }

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @param browseButtonURL OPDS URL for the browse button: null for none (default)
     */
    public void setBrowseButtonURL(String browseButtonURL) {
        this.browseButtonURL = browseButtonURL;
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
        cView.setDirection(UstadMobileSystemImpl.getInstance().getDirection());
        cView.setBrowseButtonLabel(impl.getString(MessageIDConstants.browse_feeds));
        if(model != null && model.opdsFeed != null && model.opdsFeed.isAcquisitionFeed()) {
            cView.setDeleteOptionAvailable(true);
        }

        setStandardAppMenuOptions();
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
     * @param args Hashtable containing catalog controller standard args as per KEY_ flags
     * @param context System context object
     *
     * @return
     */
    public static CatalogController makeControllerByArgsTable(Hashtable args, Object context) throws IOException, XmlPullParserException{
        UstadJSOPDSFeed opdsFeed = CatalogController.getCatalogByArgsTable(args, context);

        int flags = args.containsKey(KEY_FLAGS) ? ((Integer)args.get(KEY_FLAGS)).intValue() : 0;

        if((flags & SORT_ASC) == SORT_ASC || (flags & SORT_DESC) == SORT_DESC) {
            if((flags & SORT_BY_LASTACCESSED) == SORT_BY_LASTACCESSED) {
                opdsFeed.sortEntries(new 
                    CatalogLastAccessTimeComparer(flags, context));
            }
        }

        int resourceMode = args.containsKey(KEY_RESMOD) ? ((Integer)args.get(KEY_RESMOD)).intValue() : SHARED_RESOURCE;

        CatalogController result = new CatalogController(new CatalogModel(opdsFeed), context);
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
     * @param args Hashtable with KEY_ arguments set including URL, resource mode, http authentication parameters etc.
     *
     * @see CatalogController#makeControllerByArgsTable(Hashtable, Object)
     * 
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public static void makeControllerForView(final CatalogView view, Hashtable args, final ControllerReadyListener listener) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        CatalogController controller = new CatalogController(view.getContext());
        view.setBrowseButtonVisible(args.containsKey(KEY_BROWSE_BUTTON_URL));

        String url = (String)args.get(KEY_URL);
        if(url.equals(OPDS_PROTO_USER_FEEDLIST)) {
            view.setAddOptionAvailable(true);
            view.setDeleteOptionAvailable(true);
        }

        new LoadControllerThread(args, controller, listener, view).start();
    }

    
    public UstadController loadController(Hashtable args, Object ctx) throws Exception {
        CatalogController newController = makeControllerByArgsTable(args, ctx);

        if(args.containsKey(KEY_BROWSE_BUTTON_URL)) {
            newController.setBrowseButtonURL((String)args.get(KEY_BROWSE_BUTTON_URL));
        }
        
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
        String[] imageLinks;
        HTTPCacheDir cache = UstadMobileSystemImpl.getInstance().getHTTPCacheDir(context);
            
        String imageURI;
        Vector bgVector;

        if(CoreBuildConfig.OPDS_ITEM_ENABLE_BACKGROUNDS) {
            bgVector = feed.getLinks(OPDS_ENTRY_BACKGROUND_LINKREL, null);
            if(bgVector.size() > 0 && !isDestroyed() && view != null) {
                imageURI = getItemImageAsset(cache, (String[])bgVector.elementAt(0), feed, feed);
                view.setCatalogBackground(imageURI);
            }
        }

        for(int i = 0; i < feed.entries.length && !isDestroyed(); i++) {
            imageLinks = feed.entries[i].getThumbnailLink(false);

            if(imageLinks != null) {
                imageURI = getItemImageAsset(cache, imageLinks, feed.entries[i], feed);
                
                if(isDestroyed()) {
                    return;
                }
                
                if(imageURI != null) {
                    view.setEntrythumbnail(feed.entries[i].id, imageURI);
                }
            }

            if(CoreBuildConfig.OPDS_ITEM_ENABLE_BACKGROUNDS) {
                bgVector = feed.entries[i].getLinks(OPDS_ENTRY_BACKGROUND_LINKREL, null);
                if(bgVector.size() > 0 && !isDestroyed() && view != null) {
                    imageURI = getItemImageAsset(cache, (String[])bgVector.elementAt(0), feed.entries[i], feed);
                    view.setEntryBackground(feed.entries[i].id, imageURI);
                }
            }
        }
    }

    private String getItemImageAsset(HTTPCacheDir cache, String[] imageLinks, UstadJSOPDSItem item, UstadJSOPDSFeed feed) {
        String imageURI = UMFileUtil.resolveLink(
                feed.href, imageLinks[UstadJSOPDSEntry.LINK_HREF]);
        String imageFile = null;
        if(imageURI.startsWith("file://")) {
            imageFile = imageURI;//this is already on disk...
        }else {
            try {
                imageFile = cache.get(imageURI);
            }catch(Exception e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 132, item.title + ": " + item.id, e);
            }
        }

        return imageFile;
    }

    /**
     *
     * @param cache
     * @param imageLinks
     */
    private String getItemImageAsset(HTTPCacheDir cache, String[] imageLinks, UstadJSOPDSEntry entry) {

        String imageURI = UMFileUtil.resolveLink(
                entry.parentFeed.href, imageLinks[UstadJSOPDSEntry.LINK_HREF]);
        String imageFile = null;
        if(imageURI.startsWith("file://")) {
            imageFile = imageURI;//this is already on disk...
        }else {
            try {
                imageFile = cache.get(imageURI);
            }catch(Exception e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 132, entry.title + ": " + entry.id, e);
            }
        }

        return imageFile;
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
        String feedID =  getUserFeedListIdPrefix(context);
        UstadJSOPDSFeed usersFeeds = new UstadJSOPDSFeed(OPDS_PROTO_USER_FEEDLIST,
                impl.getString(MessageIDConstants.my_libraries),feedID);
        JSONArray arr = CatalogController.getUserFeedListArray(context);
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
     * @param dirs - Content directories that should be scanned represented by the UMStorageDir object
     * @param dirFlags - Set which directories to scan: e.g. USER_RESOURCE | SHARED_RESOURCE
     * @param context - Context object being used by the controller
     *
     * @return CatalogController representing files on the device
     */
    public static UstadJSOPDSFeed makeDeviceFeed(UMStorageDir[] dirs, int dirFlags, Object context) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String generatedHREFBase =  (dirFlags & USER_RESOURCE) == USER_RESOURCE ? impl.getUserContentDirectory(
                impl.getActiveUser(context)) : impl.getSharedContentDir();
        return makeDeviceFeed(dirs, dirFlags, generatedHREFBase, LINK_HREF_MODE_FILE, context);
    }


    public static UstadJSOPDSFeed makeDeviceFeed(UMStorageDir[] dirs, int dirFlags, String baseHREF, int linkHrefMode, Object context) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.DEBUG, 637, null);
        
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
        

        String looseFilePath = UMFileUtil.joinPaths(new String[] {
            impl.getCacheDir(incUser ? USER_RESOURCE : SHARED_RESOURCE, context), 
            "cache-loose"});
        impl.l(UMLog.DEBUG, 638, looseFilePath);
        
        boolean[] userOPDSFiles = new boolean[opdsFiles.length];
        UMUtil.fillBooleanArray(userOPDSFiles, true, opdsUserStartIndex, 
                userOPDSFiles.length);
        boolean[] userEPUBFiles = new boolean[containerFiles.length];
        UMUtil.fillBooleanArray(userEPUBFiles, true, containerUserStartIndex, 
                containerUserStartIndex);
        
        return scanFiles(opdsFiles, userOPDSFiles, containerFiles, userEPUBFiles, 
            looseFilePath, baseHREF, "My Device",
            "scandir-" + sanitizeIDForFilename(baseHREF), linkHrefMode, context);
        
    }
    
    /**
     * Triggered by the view when the user has selected the download all button
     * for this feed
     * 
     */
    public void handleClickDownloadAll() {
        selectedEntries = getModel().opdsFeed.entries;
        handleClickDownload(getModel().opdsFeed);
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
        this.view.showConfirmDialog(impl.getString(MessageIDConstants.delete_q),
            LocaleUtil.formatMessage(impl.getString(MessageIDConstants.delete_x_entries_from_device),
                String.valueOf(entries.length)), impl.getString(MessageIDConstants.delete),
                impl.getString(MessageIDConstants.cancel), CMD_DELETEENTRY);
    }
    
    public void handleConfirmDeleteEntries() {
        if(isUserFeedList()) {
            handleRemoveItemsFromUserFeed(selectedEntries);
        }else {
            for(int i = 0; i < selectedEntries.length; i++) {
                CatalogController.removeEntry(selectedEntries[i].id,
                        USER_RESOURCE | SHARED_RESOURCE, getContext());
            }
        }

        //TODO: Enable a refresh here that does not force no-cache
        view.refresh();
    }
    
    /**
     * Triggered when the user selects an entry from the catalog.  This could
     * be another OPDS catalog Feed to display or it could be a container
     * entry.
     * 
     * @param entry 
     */
    public void handleClickEntry(final UstadJSOPDSEntry entry) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!entry.parentFeed.isAcquisitionFeed()) {
            //we are loading another opds catalog
            Vector entryLinks = entry.getLinks(null, UstadJSOPDSItem.TYPE_ATOMFEED, 
                true, true);

            if(entryLinks.size() > 0) {
                String[] firstLink = (String[])entryLinks.elementAt(0);
                handleCatalogSelected(UMFileUtil.resolveLink(entry.parentFeed.href,
                        firstLink[UstadJSOPDSItem.ATTR_HREF]));
            }
        }else {
            //Go to the entry view
            Hashtable catalogEntryArgs = new Hashtable();
            UstadJSOPDSFeed entryFeed = entry.getEntryFeed();
            String[] entryAbsoluteLink = entryFeed.getAbsoluteSelfLink();
            catalogEntryArgs.put(CatalogEntryPresenter.ARG_ENTRY_OPDS_STR,
                    entry.getEntryFeed().serializeToString());
            impl.go(CatalogEntryView.VIEW_NAME, catalogEntryArgs, context);
        }
    }

    protected void handleCatalogSelected(String url) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(KEY_URL, url);

        if(impl.getActiveUser(getContext()) != null) {
            args.put(KEY_HTTPUSER, impl.getActiveUser(getContext()));
            args.put(KEY_HTTPPPASS, impl.getActiveUserAuth(getContext()));
        }

        args.put(KEY_RESMOD, new Integer(getResourceMode()));
        args.put(KEY_FLAGS, new Integer(CACHE_ENABLED));

        UstadMobileSystemImpl.getInstance().go(CatalogView.VIEW_NAME, args,
                getContext());
    }

    public void handleClickBrowseButton() {
        handleCatalogSelected(browseButtonURL);
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
            headers.put("Authorization", "Basic "+ Base64Coder.encodeString(
                username + ':' + password));
        }
        return headers;
    }

    /**
     * Get an OPDS catalog using the Arguments Hashtable containing the url, resourceMode,
     * http username, http password, and flags as per getCatalogByURL
     *
     *
     * @see CatalogController#getCatalogByURL(String, int, String, String, int, Object)
     * @param args Hashtable with arguments as per KEY_ constants
     * @param context System context object
     * @return UstadJSOPDSFeed as per getCatalogByURL
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static UstadJSOPDSFeed getCatalogByArgsTable(Hashtable args, Object context) throws IOException, XmlPullParserException{
        String httpUsername = args.containsKey(KEY_HTTPUSER) ? (String)args.get(KEY_HTTPUSER) : null;
        String httpPassword = args.containsKey(KEY_HTTPPPASS) ? (String)args.get(KEY_HTTPPPASS) : null;
        int flags = args.containsKey(KEY_FLAGS) ? ((Integer)args.get(KEY_FLAGS)).intValue() : 0;

        return getCatalogByURL((String)args.get(KEY_URL), ((Integer)args.get(KEY_RESMOD)).intValue(),
                httpUsername, httpPassword, flags, context);
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
        
        //set headers as per flags
        if((flags & CACHE_DISABLED) == CACHE_DISABLED) {
            headers.put("cache-control", "no-cache");
        }
        
        
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
                    HTTPCacheDir httpCache = impl.getHTTPCacheDir(context);
                    String filename = null;
                    if(catalogID != null) {
                        filename = getFileNameForOPDSFeedId(catalogID);
                    }

                    opdsFileURI = httpCache.get(url, filename, headers, resultBuf);
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
                //opdsFeed = UstadJSOPDSFeed.loadFromXML(parser);
                opdsFeed = new UstadJSOPDSFeed();
                opdsFeed.loadFromXpp(parser);
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
                    if(thisLink[UstadJSOPDSItem.ATTR_REL].startsWith(UstadJSOPDSEntry.LINK_ACQUIRE)) {
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
        newFeed.serialize(impl.openFileOutputStream(savePath, 0));
        
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

        cacheDir = impl.getHTTPCacheDir(context);

        
        if((resourceMode & USER_RESOURCE) == USER_RESOURCE) {
            filename = impl.getUserPref(key, context);
            //cacheDir = impl.getHTTPCacheDir(USER_RESOURCE, context);
            impl.getLogger().l(UMLog.DEBUG, 509, filename);
        }
        
        if(filename == null && (resourceMode & SHARED_RESOURCE) == SHARED_RESOURCE) {
            filename = impl.getAppPref(key, context);
            //cacheDir = impl.getHTTPCacheDir(SHARED_RESOURCE, context);
            impl.getLogger().l(UMLog.DEBUG, 510, filename);
        }


        if(filename != null) {
            String contentsXML = impl.readFileAsText(
                cacheDir.getCacheFileURIByFilename(filename), "UTF-8");
            UstadJSOPDSFeed feed = new UstadJSOPDSFeed();
            feed.loadFromString(contentsXML);
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
        String currentPath;
        
        for(int i = 0; i < dirContents.length; i++) {
            if(dirContents[i].startsWith("cache")) {
                continue;
            }
            
            currentPath = UMFileUtil.joinPaths(new String[]{dir, dirContents[i]});
            if(dirContents[i].endsWith(OPDS_EXTENSION)) {
                opdsFiles.addElement(currentPath);
            }else if(dirContents[i].endsWith(EPUB_EXTENSION)) {
                containerFiles.addElement(currentPath);
            }else if(dirContents[i].endsWith(PDF_EXTENSION)){
                if(impl.fileExists(currentPath + CONTAINER_INFOCACHE_EXT)) {
                    containerFiles.addElement(currentPath);
                }
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
     * @param linkHrefMode The feeds generated by this method can be used for the app internally or can be served over HTTP.
     *                      If used internally we probably want the file path to the entry. If used externally over HTTP
     *                      the HTTP Server will serve files by container ID
     * @return A feed object with entries for each opdsFile and if required a loose/unsorted feed
     */
    public static UstadJSOPDSFeed scanFiles(String[] opdsFiles, boolean[] opdsFileModes, String[] containerFiles, boolean[] containerFileModes, String looseContainerFile, String baseHREF, String title, String feedID, int linkHrefMode, Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.DEBUG, 639, null);
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
        boolean isEPUB;
        Vector acquisitionLinks;

        
        for(i = 0; i < containerFiles.length; i++) {
            containerFeed = null;
            impl.l(UMLog.VERBOSE, 408, containerFiles[i]);
            
            try {
                //the relative path within the cache directory
                entryCacheFile = UMFileUtil.stripPrefixIfPresent("file:///", containerFiles[i])
                        + CONTAINER_INFOCACHE_EXT;
                
                /*
                 On J2ME roots often look like file://E:/ for the memory card etc.
                 The ':' comes from the drive letter and is not valid in a normal
                 filename so must be replaced
                */
                entryCacheFile = entryCacheFile.replace(':', '_');
                
                entryCacheFile = UMFileUtil.joinPaths(new String[] {
                    impl.getCacheDir(containerFileModes[i] ? USER_RESOURCE : SHARED_RESOURCE, context),
                    entryCacheFile
                });
                isEPUB = containerFiles[i].endsWith(EPUB_EXTENSION);
                //see oif
                if(impl.fileExists(entryCacheFile) && (impl.fileLastModified(entryCacheFile) > impl.fileLastModified(containerFiles[i])) || !isEPUB) {
                    try {
                        containerFeed = new UstadJSOPDSFeed();
                        containerFeed.loadFromString(impl.readFileAsText(entryCacheFile, "UTF-8"));
                    }catch(IOException e) {
                        impl.l(UMLog.ERROR, 140, entryCacheFile, e);
                    }
                }
                
                if(containerFeed == null && isEPUB) {
                    containerFeed = ContainerController.generateContainerFeed(
                        containerFiles[i], entryCacheFile);
                    OutputStream fout = null;
                    try {
                        impl.makeDirectoryRecursive(
                            UMFileUtil.getParentFilename(entryCacheFile));
                        fout = impl.openFileOutputStream(entryCacheFile, 0);
                        XmlSerializer xs = impl.newXMLSerializer();
                        xs.setOutput(fout, "UTF-8");
                        containerFeed.serialize(xs);
                        fout.flush();
                    }catch(IOException e) {
                        impl.l(UMLog.ERROR, 138, entryCacheFile, e);
                    }finally {
                        UMIOUtils.closeOutputStream(fout);
                    }
                }
                
                
                for(j = 0; j < containerFeed.entries.length; j++) {
                    epubEntry =new UstadJSOPDSEntry(retVal, containerFeed.entries[j]);

                    //If this is a catalog being made to serve over HTTP : replace acquisition HREF with a base path followed by the ID
                    if(linkHrefMode == LINK_HREF_MODE_ID) {
                        acquisitionLinks = epubEntry.getAcquisitionLinks();
                        if(acquisitionLinks != null && acquisitionLinks.size() > 0) {
                            String[] links = (String[])acquisitionLinks.elementAt(0);
                            links[UstadJSOPDSItem.ATTR_HREF] = UMFileUtil.joinPaths(new String[]{
                                baseHREF, epubEntry.id});
                        }
                    }

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
                feed = new UstadJSOPDSFeed();
                feed.loadFromString(impl.readFileAsText(opdsFiles[i]));
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
                looseContainerFeed.serialize(impl.openFileOutputStream(looseContainerFile, 0));
            }catch(IOException e) {
                //impl.getAppView().showNotification(impl.getString(MessageIDConstants.error)
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
     * @param info CatalogEntryInfo object with required info about entry
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
     * Filter the given vector of acquisition links by the x-umprofile parameter
     * of the mime type in the acquisition links.  The server may be able to 
     * provide an epub version formatted for smaller devices (e.g. small images
     * 3gp video format etc).  Where there is one version matching the profile
     * and another version not matching the profile we will want to show only
     * the version that matches the profile.
     * 
     * @see UstadJSOPDSItem#getLinks(java.lang.String, java.lang.String, boolean, boolean) 
     * 
     * @param links Vector of String arrays indexed as per UstadJSOPDSItem.getLinks
     * @param profileName the profile parameter of the platform that we want links to match
     *  : currently only null for the generic original version and micro for micro optimized
     *    version
     * 
     * @return a new Vector filtered as described
     */
    public static Vector filterAcquisitionLinksByProfile(Vector links, String profileName) {
        Vector filteredLinks = new Vector();
        Hashtable linksByType = new Hashtable();
        String[] currentLink;
        String currentMime;
        int semiPos;
        for(int i = 0; i < links.size(); i++) {
            currentLink = (String[])links.elementAt(i);
            currentMime = currentLink[UstadJSOPDSItem.ATTR_MIMETYPE];
            semiPos = currentMime.indexOf(';');
            if(semiPos != -1) {
                currentMime = currentMime.substring(0, semiPos).trim();
            }
            
            if(linksByType.containsKey(currentMime)) {
                //this means we have found two versions here
                Integer otherIndex = (Integer)linksByType.get(currentMime);
                String[] otherLink = (String[])links.elementAt(otherIndex.intValue());
                String otherProfile = UMFileUtil.parseTypeWithParamHeader(
                    otherLink[UstadJSOPDSItem.ATTR_MIMETYPE]).getParam("x-umprofile");
                String currentProfile = UMFileUtil.parseTypeWithParamHeader(
                    currentLink[UstadJSOPDSItem.ATTR_MIMETYPE]).getParam("x-umprofile");
                
                boolean otherMatches = false;
                boolean currentMatches = false;
                if(profileName == null) {
                    otherMatches = otherProfile == null;
                    currentMatches = currentProfile == null;
                }else {
                    otherMatches = otherProfile != null && 
                        otherProfile.equals(profileName);
                    currentMatches = currentProfile != null && 
                        currentProfile.equals(profileName);
                }
                
                if(otherMatches && !currentMatches) {
                    //do nothing - do not add this one to the filtered links
                }else if(!otherMatches && currentMatches) {
                    //replace the other one
                    filteredLinks.setElementAt(currentLink, otherIndex.intValue());
                }else {
                    filteredLinks.addElement(currentLink);
                }
            }else {
                linksByType.put(currentMime, new Integer(filteredLinks.size()));
                filteredLinks.addElement(currentLink);
            }
        }
        
        return filteredLinks;
    }
    
    /**
     * Save the portion of the OPDS feed we are looking at that is entry information
     * about what we are downloading to be the initial cache of that info.
     * 
     * @param entry The OPDSEntry that is about to be acquired
     * @param info CatalogEntryInfo for the item being acquired: including mime type, file destination etc.
     * @param suggestedFilename the base filename that the entry is going to be saved to
     * @return 
     */
    public static boolean saveEntryInfo(UstadJSOPDSEntry entry, CatalogEntryInfo info, String suggestedFilename) {
        boolean savedOK = false;
        UstadJSOPDSFeed entryFeed = new UstadJSOPDSFeed(info.fileURI, suggestedFilename, 
            CatalogController.sanitizeIDForFilename(info.fileURI));
        UstadJSOPDSEntry newEntry = new UstadJSOPDSEntry(entryFeed, 
            entry.title, entry.id, UstadJSOPDSItem.LINK_ACQUIRE,
            info.mimeType, info.fileURI);
        
        entryFeed.addEntry(newEntry);
            
        try {
            entryFeed.serialize(UstadMobileSystemImpl.getInstance().openFileOutputStream(
                info.fileURI + CONTAINER_INFOCACHE_EXT, 0));
            savedOK = true;
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 160,info.fileURI + 
                CONTAINER_INFOCACHE_EXT, e);
        }
        
        return savedOK;
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
    
    public void registerDownloadingEntry(String entryID, String downloadID) {
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
        //TODO: For every entry marked as acquired : Make sure that the entry is still on disk

        CatalogEntryInfo info;
        UstadJSOPDSFeed feed = getModel().opdsFeed;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        for(int i = 0; i< feed.entries.length; i++) {
            info = getEntryInfo(feed.entries[i].id, determineSearchMode(), getContext());
            if(info == null) {
                continue;//nothing known or to check here
            }

            switch(info.acquisitionStatus) {
                case CatalogEntryInfo.ACQUISITION_STATUS_INPROGRESS:
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
                                registerDownloadingEntry(feed.entries[i].id, info.downloadID);
                        }
                    }else {
                        //perhaps the system is not tracking the download anymore and it's actually complete
                        int downloadedSize =
                                (int)UstadMobileSystemImpl.getInstance().fileSize(info.fileURI);
                        if(downloadedSize != -1 && downloadedSize == info.downloadTotalSize)
                            //this download has in fact completed
                            registerItemAcquisitionCompleted(feed.entries[i].id);
                    }
                    break;

                case CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED:
                    try {
                        if(!impl.fileExists(info.fileURI)) {
                            setEntryInfo(feed.entries[i].id, null, resourceMode, context);
                        }
                    }catch(IOException e) {
                        impl.l(UMLog.ERROR, 87, feed.entries[i].id, e);
                    }
                    break;
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
        String downloadID;
        while(downloadEntryKeys.hasMoreElements()) {
            entryID = (String)downloadEntryKeys.nextElement();
            downloadID = (String)downloadingEntries.get(entryID);
            if(downloadID.equals(evt.getDownloadID())) {
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
            String downloadID;
            while(entries.hasMoreElements()) {
                entryID = (String)entries.nextElement();
                downloadID = (String)CatalogController.this.downloadingEntries.get(entryID);
                int[] downloadStatus = UstadMobileSystemImpl.getInstance().getFileDownloadStatus(
                    downloadID, CatalogController.this.getContext());
                if(CatalogController.this.view != null) {
                    CatalogController.this.view.updateDownloadEntryProgress(entryID, 
                        downloadStatus[UstadMobileSystemImpl.IDX_DOWNLOADED_SO_FAR], 
                        downloadStatus[UstadMobileSystemImpl.IDX_BYTES_TOTAL]);
                }
            }
        }
        
    }
    
    public void handleViewPause() {
        super.handleViewPause();
        stopDownloadTimer();
    }
    
    public void handleViewResume() {
        super.handleViewResume();
        startDownloadTimer();
    }
    
    public void handleViewDestroy() {
        super.handleViewDestroy();
        stopDownloadTimer();
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



    public void handleClickAdd() {
        view.showAddFeedDialog();
    }

    public void handleFeedPresetSelected(int index) {
        view.setAddFeedDialogTextFieldsVisible(index == OPDS_CUSTOM);
        if(index > OPDS_CUSTOM) {
            String[] selectedPreset = UstadMobileConstants.OPDS_FEEDS_PRESETS[index];
            view.setAddFeedDialogTitle(selectedPreset[OPDS_FEEDS_INDEX_TITLE]);
            view.setAddFeedDialogURL(selectedPreset[OPDS_FEEDS_INDEX_URL]);
        }else if(index == OPDS_CUSTOM) {
            view.setAddFeedDialogTitle("");
            view.setAddFeedDialogURL("");
        }
    }

    /**
     * Return a one dimensional string array for the prepopulated OPDS_FEEDS_PRESETS
     * of common OPDS sources
     *
     * @param column
     * @return
     */
    public String[] getFeedList(int column) {
        String[] retVal = new String[UstadMobileConstants.OPDS_FEEDS_PRESETS.length];
        for(int i = 0; i < retVal.length; i++) {
            retVal[i] = UstadMobileConstants.OPDS_FEEDS_PRESETS[i][column];
        }

        return retVal;
    }

    public static void addFeedToUserFeedList(String url, String title, String authUser, String authPass, Object context) {
        try {
            JSONArray arr = CatalogController.getUserFeedListArray(context);
            JSONObject newFeed = new JSONObject();
            newFeed.put("url", url);
            newFeed.put("title", title);
            newFeed.put("httpu", authUser);
            newFeed.put("httpp", authPass);
            arr.put(newFeed);
            CatalogController.setUserFeedListArray(arr, context);
        }catch(JSONException e) {

        }
    }

    public static JSONArray getUserFeedListArray(Object context) {
        JSONArray retVal = null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            String currentJSON = impl.getUserPref(
                    CatalogController.PREFKEY_USERFEEDLIST, null, context);
            if(currentJSON != null) {
                retVal = new JSONArray(currentJSON);
            }else {
                retVal = getDefaultUserFeedList(context);
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 148, null, e);
        }

        return retVal;
    }

    /**
     * Generates the default user feed list by resolving the DEFAULT_OPDS_SERVER
     * relative to the current xAPI server
     *
     * @param context
     * @return
     */
    public static JSONArray getDefaultUserFeedList(Object context) {
        JSONArray retVal = null;
        String xAPIServer = UstadMobileSystemImpl.getInstance().getAppPref(
                UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                UstadMobileDefaults.DEFAULT_XAPI_SERVER, context);
        try {
            retVal = new JSONArray();
            JSONObject serverFeed = new JSONObject();
            serverFeed.put("title", "Ustad Mobile");
            serverFeed.put("url", UMFileUtil.resolveLink(xAPIServer,
                    UstadMobileDefaults.DEFAULT_OPDS_SERVER));
            serverFeed.put("auth", ":appuser:");
            retVal.put(serverFeed);
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 164, xAPIServer, e);
        }

        return retVal;
    }

    public static void setUserFeedListArray(JSONArray arr, Object context) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            impl.setUserPref(CatalogController.PREFKEY_USERFEEDLIST,
                    arr.toString(), context);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 146, null, e);
        }
    }

    public void handleAddFeed(String url, String title) {
        CatalogController.addFeedToUserFeedList(url, title, null, null, context);
        view.refresh();
    }

    public boolean isUserFeedList() {
        if(model != null && model.opdsFeed != null) {
            return model.opdsFeed.href != null && model.opdsFeed.href.equals(OPDS_PROTO_USER_FEEDLIST);
        }else {
            return false;
        }
    }

    public void handleRemoveItemsFromUserFeed(UstadJSOPDSEntry[] entriesToRemove) {
        if(entriesToRemove.length == 0) {
            return;//nothing to do here
        }

        String userPrefix = CatalogController.getUserFeedListIdPrefix(context);
        JSONArray userFeedList = CatalogController.getUserFeedListArray(context);
        JSONArray newUserFeedList = new JSONArray();
        try {
            boolean removeItem;
            String currentID;
            int j;

            for(int i = 0; i < userFeedList.length(); i++) {
                removeItem = false;
                currentID = userPrefix + i;

                for(j = 0; j < entriesToRemove.length && !removeItem; j++) {
                    if(entriesToRemove[j].id.equals(currentID)) {
                        removeItem = true;
                        break;
                    }
                }

                if(!removeItem) {
                    newUserFeedList.put(userFeedList.get(i));
                }
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 144, null, e);
        }

        CatalogController.setUserFeedListArray(newUserFeedList, context);

        view.refresh();
    }

    @Override
    protected void onDownloadStarted() {

    }

    @Override
    protected void onEntriesRemoved() {

    }

    @Override
    public void statusUpdated(AcquisitionStatusEvent event) {

    }
}
