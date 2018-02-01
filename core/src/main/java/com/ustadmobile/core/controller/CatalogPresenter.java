package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * Standard pattern catalog presenter
 *
 *
 *
 * Created by mike on 9/30/17.
 */

public class CatalogPresenter extends BaseCatalogPresenter implements AcquisitionListener  {

    private CatalogView mView;

    @Deprecated
    private UstadJSOPDSFeed feed;

    private UmLiveData<OpdsEntryWithRelations> feedLiveData;

    private String loadedFeedId;

    private UmProvider<OpdsEntryWithRelations> entryProvider;

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

    private Set<String> selectedEntries;

    private boolean deleteEntryFromFeedEnabled;

    private String title;


    public CatalogPresenter(Object context, CatalogView view) {
        super(context);
        this.mView = view;
    }


    public void onCreate(Hashtable args, Hashtable savedState) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        this.args = args;
        opdsUri = (String)args.get(ARG_URL);
        if(opdsUri.indexOf("$USERLANG$") != -1) {
            opdsUri = opdsUri.replace("$USERLANG$", impl.getDisplayedLocale(getContext())
                    .substring(0, 2));
        }

        selectedEntries = new HashSet<>();

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


        if(opdsUri.startsWith("https://") || opdsUri.startsWith("http://")) {
            feedLiveData = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsRepository()
                    .getEntryByUrl(opdsUri);
            feedLiveData.observe(this, this::handleParentFeedLoaded);
        }else if(opdsUri.equals("entries:///my_library")) {
            final String libraryUuid = "my_library";
            UmLiveData<Boolean> libraryPresent = DbManager.getInstance(getContext())
                    .getOpdsEntryDao().isEntryPresent(libraryUuid);
            feedLiveData = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsDao()
                    .getEntryByUuid(libraryUuid);
            feedLiveData.observe(CatalogPresenter.this,
                    CatalogPresenter.this::handleParentFeedLoaded);
            UmObserver<Boolean> presentObserver = new UmObserver<Boolean>() {
                @Override
                public void onChanged(Boolean present) {
                    if(!present){
                        DbManager.getInstance(getContext()).getOpdsEntryWithRelationsRepository()
                                .getEntryByUrl("asset:///com/ustadmobile/core/feed-defaults/"
                                        + libraryUuid+ ".opds", libraryUuid);
                        libraryPresent.removeObserver(this);
                    }
                }
            };

            libraryPresent.observe(this, presentObserver);
            mView.setAddOptionAvailable(true);
            setDeleteEntryFromFeedEnabled(true);
        }else if(opdsUri.equals("entries:///findEntriesByContainerFileDirectory")) {
            UMStorageDir[] storageDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                    CatalogPresenter.SHARED_RESOURCE, getContext());
            entryProvider = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsRepository()
                    .findEntriesByContainerFileDirectoryAsProvider(storageDirs[0].getDirURI());
            mView.setEntryProvider(entryProvider);
            title = UstadMobileSystemImpl.getInstance().getString(MessageID.downloaded, getContext());
        }


        feed = new UstadJSOPDSFeed();

        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
        UstadMobileSystemImpl.getInstance().getNetworkManager().removeAcquisitionTaskListener(this);
    }

    private void handleParentFeedLoaded(OpdsEntryWithRelations opdsFeed) {
        if(opdsFeed != null && (loadedFeedId == null || !loadedFeedId.equals(opdsFeed.getUuid()))) {
            loadedFeedId = opdsFeed.getUuid();
            title = opdsFeed.getTitle();
            entryProvider = DbManager.getInstance(getContext()).getOpdsEntryWithRelationsDao()
                    .getEntriesByParent(loadedFeedId);
            mView.setEntryProvider(entryProvider);
        }
    }

    public String resolveLink(String href) {
        //TODO: refactor this to using a base href variable instead
        if(feedLiveData != null && feedLiveData.getValue() != null)
            return UMFileUtil.resolveLink(feedLiveData.getValue().getUrl(), href);
        else
            return href;
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


    public void handleClickEntry(final OpdsEntryWithRelations entry) {
        OpdsLink acquisitionLink = entry.getAcquisitionLink(null, false);


        if(acquisitionLink != null) {
            openCatalogEntryView(entry.getUuid());
            return;
        }

        if(entry.getUrl() != null && UMFileUtil.isFileUri(entry.getUrl())) {
            openCatalogEntryView(entry.getUuid());
            return;
        }

        Hashtable args = new Hashtable();

        List<OpdsLink> opdsLinks = entry.getLinks();
        if(opdsLinks == null)
            return;

        String linkType;
        for(OpdsLink link : opdsLinks) {
            linkType = link.getMimeType();
            if(linkType == null)
                continue;

            if(linkType.contains("type=entry")) {
                args.put(ARG_URL, UMFileUtil.resolveLink(feedLiveData.getValue().getUrl(),
                        link.getHref()));
                args.put(CatalogEntryPresenter.ARG_TITLEBAR_TEXT, feedLiveData.getValue().getTitle());
                UstadMobileSystemImpl.getInstance().go(CatalogEntryView.VIEW_NAME, args, getContext());
                return;
            }

            if(linkType.contains("type=opds-catalog")) {
                args.put(ARG_URL, UMFileUtil.resolveLink(feedLiveData.getValue().getUrl(),
                        link.getHref()));
                UstadMobileSystemImpl.getInstance().go(CatalogView.VIEW_NAME, args, getContext());
                return;
            }
        }

        //no relevant link found - but perhaps this is a file entry
    }

    protected void openCatalogEntryView(String entryUuid) {
        Hashtable args = new Hashtable();
        args.put(ARG_URL, "entry:///" + entryUuid);

        if(feedLiveData != null && feedLiveData.getValue() != null && feedLiveData.getValue().getUrl() != null)
            args.put(ARG_BASE_HREF, feedLiveData.getValue().getUrl());

        if(title != null)
            args.put(CatalogEntryPresenter.ARG_TITLEBAR_TEXT, title);

        UstadMobileSystemImpl.getInstance().go(CatalogEntryView.VIEW_NAME, args, getContext());
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
        args.put(AddFeedDialogPresenter.ARG_UUID, feedLiveData.getValue().getUuid());
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
        if(selectedEntries.size() > 0 && deleteEntryFromFeedEnabled) {
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showConfirmDialog(
                MessageID.delete, MessageID.delete_q, MessageID.ok, MessageID.cancel, 0,
                    (commandId, choice) -> {
                        if(choice == AppView.CHOICE_POSITIVE) {
                            DbManager.getInstance(getContext()).getOpdsEntryParentToChildJoinDao()
                                    .deleteByParentIdAndChildIdAsync(feedLiveData.getValue().getUuid(),
                                            new ArrayList<>(selectedEntries), new UmCallback<Integer>() {
                                                @Override
                                                public void onSuccess(Integer result) {
                                                    selectedEntries.clear();
                                                    mView.runOnUiThread(() -> mView.setSelectedEntries(selectedEntries));
                                                }

                                                @Override
                                                public void onFailure(Throwable exception) {

                                                }
                                            });
                        }
                    }
                );
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

    public void handleSelectedEntriesChanged(Set<String> selectedEntries) {
        this.selectedEntries = selectedEntries;
    }

    public boolean isDeleteEntryFromFeedEnabled() {
        return deleteEntryFromFeedEnabled;
    }

    public void setDeleteEntryFromFeedEnabled(boolean deleteEntryFromFeedEnabled) {
        this.deleteEntryFromFeedEnabled = deleteEntryFromFeedEnabled;
        mView.setDeleteOptionAvailable(deleteEntryFromFeedEnabled);
    }
}
