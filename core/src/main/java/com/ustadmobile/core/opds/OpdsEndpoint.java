package com.ustadmobile.core.opds;

import com.ustadmobile.core.catalog.DirectoryScanner;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by mike on 10/4/17.
 *
 * Used to handle internal OPDS operations. This includes:
 *  - Generating an OPDS feed of all content that has been downloaded on the device
 *  - Retrieving and storing OPDS feeds from/to the preferences (e.g. to store a user's library list)
 */
public class OpdsEndpoint {

    private static OpdsEndpoint instance = new OpdsEndpoint();

    public static final String OPDS_PROTOCOL = "opds:///";

    /**
     * A url that provides a list of the of the contents that have been downloaded
     * to the device
     */
    public static final String OPDS_PROTO_DEVICE = "opds:///com.ustadmobile.app.devicefeed";

    /**
     * A url that provides an OPDS feed from a preference key string
     */
    public static final String OPDS_PROTO_PREFKEY_FEEDS = "opds:///com.ustadmobile.app.prefkey";

    public static final String ARG_BASE_HREF = "basehref";

    public static final String ARG_LINK_HREF_MODE = "hrefmode";

    public static final String ARG_DEVICEFEED_RESOURCE_MODE = "resmode";

    public static final String USTAD_PREFKEY_FEED_LINK_REL = "http://www.ustadmobile.com/ns/opds/prefkey_feed";

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


    private static final String PREF_KEY_FEED_LIST = "mylibrary_feeds";

    public static final String OPDS_USERFEED_ID_PREFIX = "com.ustadmobile.userfeed.";

    private Vector opdsChangeListeners = new Vector();

    private OpdsEndpointAsyncHelper asyncHelper;

    public static OpdsEndpoint getInstance() {
        return instance;
    }

    public OpdsEndpoint() {
        asyncHelper = new OpdsEndpointAsyncHelper(this);
    }


    /**
     * Used to notify of when a feed is changed.
     */
    public interface OpdsChangeListener {

        /**
         * Notification that the given opds feed uri has changed.
         *
         * @param feedUri
         */
        void feedChanged(String feedUri);
    }

    /**
     *
     * @param opdsUri
     * @param item An UstadJSOPDSFeed or UstadJSOPDSEntry object that the opdsUri will be loaded into.
     *             (Optional). Can be null, if null a new object will be created.
     * @param context
     * @return
     * @throws IOException
     */
    public UstadJSOPDSItem loadItem(String opdsUri, UstadJSOPDSItem item, Object context,
                                    UstadJSOPDSItem.OpdsItemLoadCallback callback) throws IOException {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UstadMobileSystemImpl.l(UMLog.DEBUG, 678, "OpdsEndpoint: load " + opdsUri);
        UstadJSOPDSFeed destFeed = item != null ? (UstadJSOPDSFeed)item : null;
        if(opdsUri.startsWith(OPDS_PROTO_DEVICE)) {
            Hashtable args = UMFileUtil.parseURLQueryString(opdsUri);
            int resourceMode = args.containsKey(ARG_DEVICEFEED_RESOURCE_MODE) ?
                    ((Integer)args.get(ARG_DEVICEFEED_RESOURCE_MODE)).intValue() : CatalogPresenter.SHARED_RESOURCE;

            String baseHref;
            if(args.containsKey(ARG_BASE_HREF)) {
                baseHref = (String)args.get(ARG_BASE_HREF);
            }else {
                if((resourceMode & CatalogPresenter.USER_RESOURCE) == CatalogPresenter.USER_RESOURCE) {
                    baseHref = impl.getUserContentDirectory(impl.getActiveUser(context));
                }else {
                    baseHref = impl.getSharedContentDir();
                }
            }

            int linkMode = args.containsKey(ARG_LINK_HREF_MODE) ?
                    ((Integer)args.get(ARG_LINK_HREF_MODE)).intValue() : LINK_HREF_MODE_FILE;

            UMStorageDir[] dirs = impl.getStorageDirs(resourceMode, context);
            UstadJSOPDSFeed feed = makeDeviceFeed(dirs, baseHref, linkMode, destFeed, context, callback);
            feed.title = impl.getString(MessageID.downloaded, context);
            return feed;
        }else if(opdsUri.startsWith(OPDS_PROTO_PREFKEY_FEEDS)) {
            return getFeedFromPreferenceKey(UMFileUtil.getFilename(opdsUri), opdsUri, destFeed, callback,
                    context);
        }else {
            return null;
        }
    }

    public void loadItemAsync(final String opdsUri, final UstadJSOPDSItem item, final Object context,
                              final UstadJSOPDSItem.OpdsItemLoadCallback callback) {
        asyncHelper.loadItemAsync(opdsUri, item, context, callback);
    }

    /**
     * Loads an OPDS feed from a preference key. OPDS feeds (e.g. the list of the user's library
     * feeds) can be serialized to strings and stored to a preference key. The internal OPDS url scheme
     * is as opds://com.ustadmobile.app.prefkey/preference_key where preference_key is the name of the
     * preference key itself.
     *
     * Defaults: place a .opds file in the assets using the following naming convention for it to
     * become the default feed for a given preference key:
     *
     * com/ustadmobile/core/feed-defaults/prefkyename.opds
     *
     * @see #OPDS_PROTO_PREFKEY_FEEDS
     *
     * @param prefKey Preference key to retrieve the OPDS from.
     * @param url The internal url (e.g. as above) that this feed is being referred to by
     * @param destFeed The destination feed into which items will be loaded
     * @param callback (optional) OPDSItemLoadCallback
     * @param context Context object
     *
     * @return UstadJSOPDSFeed populated from the string stored in the given preference key
     */
    protected UstadJSOPDSFeed getFeedFromPreferenceKey(String prefKey, String url, UstadJSOPDSFeed destFeed,
                                                       UstadJSOPDSItem.OpdsItemLoadCallback callback, Object context) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String activeUser = impl.getActiveUser(context);
        String opdsFeedStr;
        if(destFeed == null) {
            destFeed = new UstadJSOPDSFeed(url, impl.getString(MessageID.my_libraries, context),
                    OPDS_USERFEED_ID_PREFIX + (activeUser != null ? activeUser : "guest"));
        }

        if(activeUser != null) {
            opdsFeedStr = impl.getUserPref(prefKey, context);
        }else {
            opdsFeedStr = impl.getAppPref(prefKey, context);
        }


        if(opdsFeedStr != null) {
            try {
                destFeed.loadFromString(opdsFeedStr, callback);
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 684, opdsFeedStr, e);
            }catch(XmlPullParserException x) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 684, opdsFeedStr, x);
            }
        }else {
            //it's a new feed - just add the prefkey link so the view knows it can add
            InputStream assetIn = null;
            try {
                assetIn = impl.openResourceInputStream(
                        "/com/ustadmobile/core/feed-defaults/" + prefKey + ".opds", context);
                XmlPullParser xpp = impl.newPullParser(assetIn);
                destFeed.loadFromXpp(xpp, callback);
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 684, opdsFeedStr, e);
            }catch(XmlPullParserException x) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 685, opdsFeedStr, x);
            }finally {
                UMIOUtils.closeInputStream(assetIn);
            }

            destFeed.addLink(USTAD_PREFKEY_FEED_LINK_REL, UstadJSOPDSItem.TYPE_NAVIGATIONFEED, prefKey);
        }

        if(callback != null)
            callback.onDone(destFeed);

        return destFeed;
    }

    protected UstadJSOPDSFeed makeDeviceFeed(UMStorageDir[] dirs, String baseHREF, int linkHrefMode,
                 UstadJSOPDSFeed deviceFeed, Object context,
                 UstadJSOPDSItem.OpdsItemLoadCallback callback) throws IOException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(deviceFeed == null) {
            deviceFeed = new UstadJSOPDSFeed(OpdsEndpoint.OPDS_PROTO_DEVICE,
                    impl.getString(MessageID.my_resources, context), "com.ustadmobile.devicefeed");
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setAcquisitionLinkHrefPrefix(baseHREF);
        scanner.setLinkHrefMode(linkHrefMode);

        int dirMode;
        for(int i = 0; i < dirs.length; i++) {
            dirMode = dirs[i].isUserSpecific()
                    ? CatalogPresenter.USER_RESOURCE
                    : CatalogPresenter.SHARED_RESOURCE;
            scanner.scanDirectory(dirs[i].getDirURI(), impl.getCacheDir(dirMode, context), "scandir",
                    "scandir", dirMode, callback, deviceFeed, context);
        }

        if(callback != null)
            callback.onDone(deviceFeed);

        return deviceFeed;
    }

    public void saveFeedToPreferences(UstadJSOPDSFeed feed, String prefkey, Object context) {
        String feedStr = feed.serializeToString();
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(impl.getActiveUser(context) != null) {
            impl.setUserPref(prefkey, feedStr, context);
        }else {
            impl.setAppPref(prefkey, feedStr, context);
        }

        fireFeedChanged(UMFileUtil.joinPaths(new String[]{OPDS_PROTO_PREFKEY_FEEDS, prefkey}));
    }

    /**
     * Remove a list of entries from the feed in the given preference key. Once complete, save the
     * resulting feed back to it's preference key
     *
     * @param prefKey The preference key that the feed is stored in
     * @param url The url through which the feed is accessed e.g. OPDS_PROTO_PREFKEY_FEEDS/prefKey
     * @param entriesToRemove A vector where each element is either a String of the entry id to remove
     *                        or an OpdsItem where the id is the id of the item to remove
     * @param context System context object
     */
    public void removeEntriesFromPreferenceKeyFeed(String prefKey, String url, UstadJSOPDSFeed feed,
                                                   Vector entriesToRemove, Object context) {
        feed = getFeedFromPreferenceKey(prefKey, url, feed, null, context);
        int entryIndex;
        Object entryObj;
        String idToRemove;
        for(int i = 0; i < entriesToRemove.size(); i++) {
            entryObj = entriesToRemove.elementAt(i);
            if(entryObj instanceof String) {
                idToRemove = (String)entryObj;
            }else {
                idToRemove = ((UstadJSOPDSItem)entryObj).id;
            }

            entryIndex = feed.indexOfEntryId(idToRemove);

            if(entryIndex >= 0) {
                feed.removeEntry(entryIndex);
            }
        }

        saveFeedToPreferences(feed, prefKey, context);
        fireFeedChanged(url);
    }



    /**
     * Add an OPDS change listener. If a feed is modified using OpdsEndpoint methods, an event will
     * be fired so that any relevant views can refresh
     *
     * @param listener Listener to add
     */
    public void addOpdsChangeListener(OpdsChangeListener listener) {
        opdsChangeListeners.add(listener);
    }

    /**
     * Remove an OPDS change listener
     *
     * @param listener Listener to remove
     */
    public void removeOpdsChangeListener(OpdsChangeListener listener) {
        opdsChangeListeners.remove(listener);
    }

    protected void fireFeedChanged(String opdsUri) {
        for(int i = 0; i < opdsChangeListeners.size(); i++) {
            ((OpdsChangeListener)opdsChangeListeners.elementAt(i)).feedChanged(opdsUri);
        }
    }

}
