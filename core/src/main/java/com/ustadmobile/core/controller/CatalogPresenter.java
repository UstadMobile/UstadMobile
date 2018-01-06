package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.opds.OpdsEndpoint;
import com.ustadmobile.core.opds.OpdsFilterOptionField;
import com.ustadmobile.core.opds.OpdsFilterOptions;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.CatalogView;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Standard pattern catalog presenter
 *
 *
 *
 * Created by mike on 9/30/17.
 */

public class CatalogPresenter extends BaseCatalogPresenter implements UstadJSOPDSItem.OpdsItemLoadCallback,
        AcquisitionListener, OpdsEndpoint.OpdsChangeListener  {

    private CatalogView mView;

    private UstadJSOPDSFeed feed;

    /**
     * Constant that can be used in the buildconfig to set the bottom button to be a download all
     * button instead of a link to any other catalog
     */
    public static final String FOOTER_BUTTON_DOWNLOADALL = "downloadall";

    public static final String PREFKEY_STORAGE_DIR_CHECKTIME = "storagedir_lastchecked";

    public static final int STATUS_ACQUIRED = 0;

    public static final int STATUS_ACQUISITION_IN_PROGRESS = 1;

    public static final int STATUS_NOT_ACQUIRED = 2;

    public static final int STATUS_AVAILABLE_LOCALLY = 3;

    /**
     * Save/retrieve resource from user specific directory
     */
    public static final int USER_RESOURCE = 2;


    /**
     * Save/retrieve resource from shared directory
     */
    public static final int SHARED_RESOURCE = 4;

    public static final int ALL_RESOURCES = USER_RESOURCE | SHARED_RESOURCE;

    /**
     * Prefix used for pref keys that are used to store entry info
     */
    private static final String PREFIX_ENTRYINFO = "e2ei-";


    private int resourceMode;

    private String footerButtonUrl;

    private Hashtable args;

    /**
     * Alternative translations
     */
    private Vector alternativeTranslationLinks;

    /**
     * If the feed originates from a preference key (e.g. the user feed list), this will be the
     * preference key
     */
    private String feedPrefKey;

    /**
     * The uri where the OPDS for this catalog was loaded from
     */
    private String opdsUri;

    boolean opdsChangeListenerRegistered = false;

    private Vector selectedEntries;


    public CatalogPresenter(Object context, CatalogView view) {
        super(context);
        this.mView = view;
    }


    public void onCreate(Hashtable args, Hashtable savedState) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        this.args = args;
        opdsUri = (String)args.get(ARG_URL);
        if(opdsUri.indexOf("$USERLANG$") != -1)
            opdsUri = opdsUri.replace("$USERLANG$", impl.getDisplayedLocale(getContext()).substring(0, 2));

        selectedEntries = new Vector();

        if(args.containsKey(ARG_RESMOD)){
            resourceMode = ((Integer)args.get(ARG_RESMOD)).intValue();
        }else{
            resourceMode = SHARED_RESOURCE;
        }

        if(args.containsKey(ARG_BOTTOM_BUTTON_URL)) {
            String footerButtonUrl = (String)args.get(ARG_BOTTOM_BUTTON_URL);
            setFooterButtonUrl(footerButtonUrl);
            mView.setFooterButtonVisible(true);
            int footerButtonLabel = footerButtonUrl.equals(FOOTER_BUTTON_DOWNLOADALL)
                    ? MessageID.download_all : MessageID.browse_feeds;
            mView.setFooterButtonLabel(impl.getString(footerButtonLabel, getContext()));
        }else {
            mView.setFooterButtonVisible(false);
        }

        feed = new UstadJSOPDSFeed();

        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
        initEntryStatusCheck();
        //TODO: What should happen here is that we should load the catalog async, and at the same time
        // scan the file system. If something new is discovered, we can fire an event.
//        Hashtable feedLoadHeaders = new Hashtable();
//        String opdsUrl = (String)args.get(ARG_URL);
//        feed.loadFromUrlAsync(opdsUrl, feedLoadHeaders, this);
    }

    public void onDestroy() {
        super.onDestroy();
        if(opdsChangeListenerRegistered) {
            OpdsEndpoint.getInstance().removeOpdsChangeListener(this);
        }
        UstadMobileSystemImpl.getInstance().getNetworkManager().removeAcquisitionTaskListener(this);
    }


    public void initEntryStatusCheck(final boolean httpCacheMustRevalidate) {
        String lastCheckedDir = UstadMobileSystemImpl.getInstance().getAppPref(PREFKEY_STORAGE_DIR_CHECKTIME,
                getContext());
        long timeNow = new Date().getTime();

        UstadJSOPDSItem.OpdsItemLoadCallback feedCheckLoadedCallbackHandler = new UstadJSOPDSItem.OpdsItemLoadCallback() {
            @Override
            public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entryLoaded) {

            }

            @Override
            public void onDone(UstadJSOPDSItem item) {
                loadFeed(httpCacheMustRevalidate);
            }

            @Override
            public void onError(UstadJSOPDSItem item, Throwable cause) {
                loadFeed(httpCacheMustRevalidate);
            }
        };

        if(lastCheckedDir == null || timeNow - Long.parseLong(lastCheckedDir) > 500) {
            OpdsEndpoint.getInstance().loadItemAsync(OpdsEndpoint.OPDS_PROTO_DEVICE, null, context,
                    feedCheckLoadedCallbackHandler);
        }else {
            feedCheckLoadedCallbackHandler.onDone(null);
        }
    }

    public void initEntryStatusCheck() {
        initEntryStatusCheck(false);
    }

    private void loadFeed(boolean httpCacheMustRevalidate) {
        Hashtable feedLoadHeaders = new Hashtable();

        if(httpCacheMustRevalidate) {
            feedLoadHeaders.put("cache-control", "must-revalidate");
        }

        feed.loadFromUrlAsync(opdsUri, feedLoadHeaders, getContext(), CatalogPresenter.this);
    }

    @Override
    public void onEntryLoaded(UstadJSOPDSItem item, final int position, final UstadJSOPDSEntry entry) {
        mView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentIndex = mView.indexOfEntry(entry.getItemId());

                if(currentIndex == -1) {
                    mView.addEntry(entry);
                }else if(currentIndex != -1 && mView.indexOfEntry(entry.getItemId()) == currentIndex){
                    //same position - just refresh it
                    mView.setEntryAt(currentIndex, entry);
                }else {
                    mView.removeEntry(entry);//get rid of it from wherever it was before
                    mView.addEntry(currentIndex, entry);
                }

                UmOpdsLink thumbnailLinks = entry.getThumbnailLink(false);
                if(thumbnailLinks != null)
                    mView.setEntrythumbnail(entry.getItemId(), UMFileUtil.resolveLink(entry.getHref(),
                            thumbnailLinks.getHref()));

                CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(entry.getItemId(),
                        CatalogPresenter.SHARED_RESOURCE | CatalogPresenter.USER_RESOURCE,
                        getContext());
                if(entryInfo != null) {
                    mView.setEntryStatus(entry.getItemId(), entryInfo.acquisitionStatus);
                    mView.setDownloadEntryProgressVisible(entry.getItemId(),
                            entryInfo.acquisitionStatus == STATUS_ACQUISITION_IN_PROGRESS);
                }
            }
        });
    }

    @Override
    public void onDone(final UstadJSOPDSItem item) {
        UmOpdsLink prefKeyLink = item.getFirstLink(OpdsEndpoint.USTAD_PREFKEY_FEED_LINK_REL,
                null);
        if(prefKeyLink != null)
            this.feedPrefKey = prefKeyLink.getHref();
        else
            this.feedPrefKey = null;

        if(!opdsChangeListenerRegistered) {
            OpdsEndpoint.getInstance().addOpdsChangeListener(this);
            opdsChangeListenerRegistered = true;
        }

        mView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                while(mView.getNumEntries() > feed.size()) {
                    mView.removeEntry(feed.size());
                }

                mView.setRefreshing(false);

                OpdsFilterOptions filterOptions = new OpdsFilterOptions();
                OpdsFilterOptionField langField = new OpdsFilterOptionField();
                langField.setFilterName("Language");
                langField.setFilterOptions(new String[]{"English", "Dari"});
                filterOptions.addFilter(langField);
                mView.setFilterOptions(filterOptions);


                alternativeTranslationLinks = feed.getAlternativeTranslationLinks();
                if(alternativeTranslationLinks.size() > 0) {
                    String feedLang = feed.getLanguage();
                    int disabledItem = -1;
                    if(feedLang != null) {
                        alternativeTranslationLinks.insertElementAt(feedLang, 0);
                        disabledItem = 0;
                    }

                    mView.setAlternativeTranslationLinks(getNamesForLangaugeCodes(
                            alternativeTranslationLinks), disabledItem);
                }


                mView.setAddOptionAvailable(feedPrefKey != null);
                if(feedPrefKey != null)
                    mView.setDeleteOptionAvailable(true);

            }
        });
    }

    @Override
    public void onError(UstadJSOPDSItem item, Throwable cause) {

    }

    public int getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(int resourceMode) {
        this.resourceMode = resourceMode;
    }

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @return The OPDS URL for the browse button; null if there is none (default)
     */
    public String getFooterButtonUrl() {
        return footerButtonUrl;
    }

    /**
     * Catalog can have a browse button at the bottom: e.g. when the user is on the donwloaded
     * items page the browse button can take them to their feed list or a preset catalog URL directly
     *
     * @param footerButtonUrl OPDS URL for the browse button: null for none (default)
     */
    public void setFooterButtonUrl(String footerButtonUrl) {
        this.footerButtonUrl = footerButtonUrl;
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    protected void onDownloadStarted() {

    }

    @Override
    protected void onEntriesRemoved() {

    }


    /**
     * Triggered when the user selects an entry from the catalog. This could
     * be another OPDS catalog Feed to display or it could be a container
     * entry.
     *
     * @param entryId
     */
    public void handleClickEntry(final String entryId) {
        UstadJSOPDSEntry entry = feed.getEntryById(entryId);
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(!entry.getParentFeed().isAcquisitionFeed()) {
            //we are loading another opds catalog
            Vector entryLinks = entry.getLinks(null, UstadJSOPDSItem.TYPE_ATOMFEED,
                    true, true);

            if(entryLinks.size() > 0) {
                UmOpdsLink firstLink = (UmOpdsLink) entryLinks.elementAt(0);
                handleCatalogSelected(UMFileUtil.resolveLink(entry.getParentFeed().getHref(),
                        firstLink.getHref()));
            }
        }else {
            //Go to the entry view
            handleOpenEntryView(entry, entry.getParentFeed().getTitle());
        }
    }

    public void handleClickAdd() {
        Hashtable args = new Hashtable();
        args.put(AddFeedDialogPresenter.ARG_PREFKEY, feedPrefKey);
        UstadMobileSystemImpl.getInstance().go(AddFeedDialogView.VIEW_NAME, args, getContext());
    }

    protected void handleCatalogSelected(String url) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_URL, url);

        if(impl.getActiveUser(getContext()) != null) {
            args.put(ARG_HTTPUSER, impl.getActiveUser(getContext()));
            args.put(ARG_HTTPPPASS, impl.getActiveUserAuth(getContext()));
        }

        UstadMobileSystemImpl.getInstance().go(CatalogView.VIEW_NAME, args,
                getContext());
    }

    public void handleClickAlternativeLanguage(int langIndex) {
        String[] altLangLinks = (String[])alternativeTranslationLinks.elementAt(langIndex);
        String alternativeUrl = UMFileUtil.resolveLink(feed.getHref(),
                altLangLinks[UstadJSOPDSItem.ATTR_HREF]);
        handleCatalogSelected(alternativeUrl);
    }

    public void handleClickFooterButton() {
        if(FOOTER_BUTTON_DOWNLOADALL.equals(footerButtonUrl)) {
            handleClickDownloadAll();
        }else {
            handleCatalogSelected(footerButtonUrl);
        }
    }


    /**
     * Triggered by the view when the user has selected the download all button
     * for this feed
     *
     */
    public void handleClickDownloadAll() {
        handleClickDownload(feed, feed.getAllEntries());
    }

    public void handleClickDelete() {
        if(selectedEntries.size() > 0) {
            if(feedPrefKey != null) {
                UstadMobileSystemImpl.getInstance().getAppView(getContext()).showConfirmDialog(
                        MessageID.delete, MessageID.delete_q, MessageID.ok, MessageID.cancel, 0,
                        new AppViewChoiceListener() {
                            @Override
                            public void appViewChoiceSelected(int commandId, int choice) {
                                if(choice == AppView.CHOICE_POSITIVE) {
                                    OpdsEndpoint.getInstance().removeEntriesFromPreferenceKeyFeed(
                                            feedPrefKey, opdsUri, feed, selectedEntries, context);
                                    selectedEntries.removeAllElements();
                                    mView.setSelectedEntries(selectedEntries);
                                }
                            }
                        }
                );
            }
        }
    }

    /**
     * To be called by the view when the user clicks the share button the catalog. Used to send
     * acquired entries using wifi direct sharing.
     */
    public void handleClickShare() {
        //share all those in catalog that have been acquired
        Vector acquiredEntryIds = new Vector();
        CatalogEntryInfo entryInfo;
        for(int i = 0; i < feed.size(); i++) {
            entryInfo = getEntryInfo(feed.getEntry(i).getItemId(), CatalogPresenter.ALL_RESOURCES,
                    getContext());
            if(entryInfo != null && entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED) {
                acquiredEntryIds.addElement(feed.getEntry(i).getItemId());
            }
        }

        if(acquiredEntryIds.size() == 0) {
            //nothing downloaded...
            return;
        }

        String[] idsToShare = new String[acquiredEntryIds.size()];
        acquiredEntryIds.copyInto(idsToShare);
        Hashtable shareArgs = new Hashtable();
        shareArgs.put("entries", idsToShare);
        shareArgs.put("title", feed.getTitle());
        UstadMobileSystemImpl.getInstance().go("SendCourse", shareArgs, getContext());
    }


    @Override
    public void acquisitionProgressUpdate(String entryId, AcquisitionTaskStatus status) {
        UstadJSOPDSEntry entry=  feed.getEntryById(entryId);
        if(entry != null) {
            float progress = (float)((double)status.getDownloadedSoFar() / (double)status.getTotalSize());
            mView.updateDownloadEntryProgress(entryId, progress, formatDownloadStatusText(status));
        }
    }

    @Override
    public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
        switch(status.getStatus()){
            case UstadMobileSystemImpl.DLSTATUS_RUNNING:
                mView.setEntryStatus(entryId, CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS);
                mView.setDownloadEntryProgressVisible(entryId, true);
                break;
            case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                mView.setDownloadEntryProgressVisible(entryId, false);
                mView.setEntryStatus(entryId, CatalogPresenter.STATUS_ACQUIRED);
                break;
        }
    }

    public void handleRefresh() {
        mView.setRefreshing(true);
        initEntryStatusCheck(true);
    }

    @Override
    public void feedChanged(String feedUri) {
        if(feedUri.equals(opdsUri)) {
            mView.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleRefresh();
                }
            });
        }
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
     * @deprecated
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
        if(entry != null && entry.acquisitionStatus == STATUS_ACQUIRED) {
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.getLogger().l(UMLog.INFO, 520, entry.fileURI);
            impl.removeFile(entry.fileURI);
            setEntryInfo(entryID, null, resourceMode, context);
        }
    }

    public void handleSelectedEntriesChanged(Vector selectedEntries) {
        this.selectedEntries = selectedEntries;
    }

}
