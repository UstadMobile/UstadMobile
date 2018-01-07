package com.ustadmobile.core.controller;

import com.ustadmobile.core.catalog.ContentTypeManager;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.opds.entities.UmOpdsLink;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.LoginView;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/* $if umplatform != 2 $ */
/* $endif */

/**
 * Created by mike on 4/17/17.
 */

public class CatalogEntryPresenter extends BaseCatalogPresenter implements AcquisitionListener,
        NetworkManagerListener, UstadJSOPDSItem.OpdsItemLoadCallback, DialogResultListener{

    private CatalogEntryView catalogEntryView;

    private Hashtable args;

    public static final String ARG_ENTRY_OPDS_STR = "opds_str";

    public static final String ARG_ENTRY_ID = "entry_id";

    /**
     * Where the dislpay mode is "normal" - e.g. DISPLAY_MODE_THUMBNAIL the title bar will collapse
     * and show the title header. This should normally come from the OPDS feed from which this item
     * was navigated to.
     */
    public static final String ARG_TITLEBAR_TEXT = "bar_title";

    public static final String APP_CONFIG_DISPLAY_MODE = "catalog_entry_display_mode";


    private UstadJSOPDSEntry entry;

    private UstadJSOPDSFeed entryFeed;

    private NetworkManagerCore manager;

    private long entryCheckTaskId = -1;

    private Vector alternativeTranslationLinks;

    private Vector[] sharedAcquiredEntries;

    private static final int CMD_REMOVE_PRESENTER_ENTRY = 60;

    private static final int CMD_SHARE_ENTRY = 63;

    protected AvailabilityMonitorRequest availabilityMonitorRequest;

    private long downloadTaskId;

    private RelatedItemLoader seeAlsoLoader = new RelatedItemLoader();

    private boolean openAfterLoginOrRegister = false;

    private boolean entryLoaded = false;

    private boolean seeAlsoVisible = false;


    /**
     * Represents a related item, as per the atom spec rel='related', used to provide see also
     * links for the user.
     */
    protected class RelatedItem {

        UstadJSOPDSItem opdsItem;

        String url;

        String[] link;

        /**
         *
         * @param opdsItem The OpdsItem (e.g. Entry or Feed) that represents the related item represents.
         *                 This may not have loaded yet.
         * @param link The link string array as per UstadJSOPDSItem containing the href and mime type.
         * @param baseHref The base href path from which links are resolveds
         */
        protected RelatedItem(UstadJSOPDSItem opdsItem, String[] link, String baseHref) {
            this.opdsItem = opdsItem;
            this.url = UMFileUtil.resolveLink(baseHref, link[UstadJSOPDSItem.ATTR_HREF]);
            this.link = link;
        }

        /**
         * Equivilent to calling RelatedItem(opdsItem, link, entry.getHref()) - resolves the link
         * href from the catalog entry presenter's main entry.
         *
         * @param opdsItem The OpdsItem (e.g. Entry or Feed) that represents the related item represents.
         *                 This may not have loaded yet.
         * @param link The link string array as per UstadJSOPDSItem containing the href and mime type.
         */
        protected RelatedItem(UstadJSOPDSItem opdsItem, String[] link) {
            this(opdsItem, link, CatalogEntryPresenter.this.entry.getHref());
        }
    }

    /**
     * Handles loading related items. In order to find the thumbnail for an item, we need to load
     * the entry xml itself, and then look in that to find the thumbnail url
     */
    protected class RelatedItemLoader implements UstadJSOPDSItem.OpdsItemLoadCallback{

        protected RelatedItem currentLoadingItem;

        private Vector itemsToLoad = new Vector();

        public void addItemToLoad(String[] link) {
            String linkUrl = UMFileUtil.resolveLink(entry.getHref(), link[UstadJSOPDSItem.ATTR_HREF]);
            UMFileUtil.TypeWithParamHeader typeWithParams = UMFileUtil.parseTypeWithParamHeader(
                    link[UstadJSOPDSItem.ATTR_MIMETYPE]);
            String catalogType = typeWithParams.getParam("type");
            UstadJSOPDSItem item;
            if(catalogType != null && catalogType.equals("entry")) {
                item = new UstadJSOPDSEntry(null);
            }else {
                item = new UstadJSOPDSFeed(linkUrl);
            }

            itemsToLoad.addElement(new RelatedItem(item, link));
            checkQueue();
        }

        private void checkQueue() {
            if(currentLoadingItem == null && itemsToLoad.size() > 0) {
                currentLoadingItem = (RelatedItem)itemsToLoad.remove(0);
                UstadMobileSystemImpl.l(UMLog.DEBUG, 679, currentLoadingItem.url);
                currentLoadingItem.opdsItem.loadFromUrlAsync(currentLoadingItem.url, null, getContext(),
                        this);
            }
        }

        @Override
        public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entryLoaded) {

        }

        @Override
        public void onDone(UstadJSOPDSItem item) {
            if(currentLoadingItem != null && currentLoadingItem.opdsItem == item) {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 680, currentLoadingItem.url);
                handleRelatedItemReady(currentLoadingItem);
                currentLoadingItem = null;
                checkQueue();
            }else {
                //something is wrong
            }
        }

        @Override
        public void onError(UstadJSOPDSItem item, Throwable cause) {
            if (currentLoadingItem != null && currentLoadingItem.opdsItem == item) {
                UstadMobileSystemImpl.l(UMLog.WARN, 681, currentLoadingItem.url);
                currentLoadingItem = null;
                checkQueue();
            } else {
                //something is wrong
            }
        }
    }


    public CatalogEntryPresenter(Object context) {
        super(context);
    }

    public CatalogEntryPresenter(Object context, CatalogEntryView view, Hashtable args) {
        super(context);
        this.catalogEntryView = view;
        this.args = args;
    }

    public void onCreate() {
        manager = UstadMobileSystemImpl.getInstance().getNetworkManager();
        if(this.args.containsKey(ARG_ENTRY_OPDS_STR)) {
            try {
                entryFeed = new UstadJSOPDSFeed();
                entryFeed.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                entry = entryFeed.getEntryById(args.get(ARG_ENTRY_ID).toString());
                handleEntryReady();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else {
            entry = new UstadJSOPDSEntry(null);
            entry.loadFromUrlAsync((String)args.get(ARG_URL), null, getContext(), this);
        }

        if(this.args.containsKey(ARG_TITLEBAR_TEXT))
            catalogEntryView.setTitlebarText((String)this.args.get(ARG_TITLEBAR_TEXT));

        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
    }

    @Override
    public void onEntryLoaded(UstadJSOPDSItem item, int position, UstadJSOPDSEntry entry) {

    }

    @Override
    public void onDone(UstadJSOPDSItem item) {
        catalogEntryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleEntryReady();
            }
        });
    }

    @Override
    public void onError(UstadJSOPDSItem item, Throwable cause) {
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                "Error: ", AppView.LENGTH_LONG);
    }

    public void handleEntryReady() {
        entryLoaded = true;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        catalogEntryView.setEntryTitle(entry.getTitle());

        if(entry.getNumAuthors() > 0) {
            catalogEntryView.setEntryAuthors(UMUtil.joinStrings(entry.getAuthors(), ", "));
        }

        CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(entry.getItemId(),
                CatalogPresenter.ALL_RESOURCES, context);
        catalogEntryView.setDescription(entry.getContent(), entry.getContentType());
        UmOpdsLink firstAcquisitionLink = entry.getFirstAcquisitionLink(null);
        if(firstAcquisitionLink != null
                && firstAcquisitionLink.getLength() > 0) {
            catalogEntryView.setSize(impl.getString(MessageID.size, getContext())
                    + ": "
                    + UMFileUtil.formatFileSize(firstAcquisitionLink.getLength()));
        }

        //set the available translated versions that can be found
        alternativeTranslationLinks = entry.getAlternativeTranslationLinks();

        String[] translatedLanguages = new String[alternativeTranslationLinks.size()];
        String[] translatedLink;
        for(int i = 0; i < translatedLanguages.length; i++) {
            translatedLink = (String[])alternativeTranslationLinks.elementAt(i);
            translatedLanguages[i] = translatedLink[UstadJSOPDSItem.ATTR_HREFLANG];
            if(UstadMobileConstants.LANGUAGE_NAMES.containsKey(translatedLanguages[i]))
                translatedLanguages[i] = UstadMobileConstants.LANGUAGE_NAMES
                        .get(translatedLanguages[i]).toString();
        }

        catalogEntryView.setAlternativeTranslationLinks(translatedLanguages);

        boolean isAcquired = entryInfo != null
                ? entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED
                : false;

        updateButtonsByStatus(isAcquired ? CatalogPresenter.STATUS_ACQUIRED :
                CatalogPresenter.STATUS_NOT_ACQUIRED);


        NetworkManagerCore networkManager = UstadMobileSystemImpl.getInstance().getNetworkManager();
        boolean isDownloadInProgress = entryInfo != null
                &&  networkManager.getTaskById(entryInfo.downloadID,
                NetworkManagerCore.QUEUE_ENTRY_ACQUISITION) != null;

        if(isDownloadInProgress) {
            catalogEntryView.setProgressVisible(true);
            downloadTaskId = entryInfo.downloadID;
        }


        //TODO: as this is bound to the activity - this might not be ready - lifecycle implication needs handled
        NetworkManagerCore manager  = UstadMobileSystemImpl.getInstance().getNetworkManager();
        /* $if umplatform != 2  $ */
        List<EntryCheckResponse> fileResponse = manager.getEntryResponsesWithLocalFile(entry.getItemId());
        if(fileResponse != null) {
            catalogEntryView.setLocallyAvailableStatus(CatalogEntryView.LOCAL_STATUS_AVAILABLE);
        }
        manager.addNetworkManagerListener(this);
        startMonitoringLocalAvailability();
        /* $endif$ */

        //set see also items
        if(entry != null){
            Vector relatedLinks = entry.getLinks(UstadJSOPDSItem.LINK_REL_RELATED, null);

            String[] thumbnailLink = null;
            String[] currentLink;
            String thumbnailUrl = null;
            UstadJSOPDSEntry relatedEntry;
            for(int i = 0; i < relatedLinks.size(); i++) {
                currentLink = (String[])relatedLinks.elementAt(i);
                relatedEntry = null;

                if(entryFeed != null) {
                    Vector relatedEntryMatch = entryFeed.getEntriesByLinkParams(
                            UstadJSOPDSFeed.LINK_REL_ALTERNATE, null,
                            currentLink[UstadJSOPDSItem.ATTR_HREF], entry.getLanguage());
                    if(relatedEntryMatch != null && relatedEntryMatch.size() > 0) {
                        relatedEntry = (UstadJSOPDSEntry) relatedEntryMatch.elementAt(0);
                    }
                }

                if(relatedEntry != null) {
                    handleRelatedItemReady(new RelatedItem(relatedEntry, currentLink));
                }else {
                    seeAlsoLoader.addItemToLoad(currentLink);
                }
            }
//
//            if(relatedLinks.size() == 0)
//                catalogEntryView.setSeeAlsoVisible(false);
        }

        Vector coverImages = entry.getLinks(UstadJSOPDSItem.LINK_COVER_IMAGE, null);
        if(coverImages != null && coverImages.size() > 0) {
            String coverImageUrl = UMFileUtil.resolveLink(entry.getHref(),
                    ((UmOpdsLink)coverImages.elementAt(0)).getHref());
            catalogEntryView.setHeader(coverImageUrl);
        }

        Vector thumbnails = entry.getThumbnails();
        if(thumbnails != null && thumbnails.size() > 0) {
            String thumbnailUrl = UMFileUtil.resolveLink(entry.getHref(),
                    ((UmOpdsLink) thumbnails.elementAt(0)).getHref());
            catalogEntryView.setThumbnail(thumbnailUrl);
        }else {
            catalogEntryView.setThumbnail(null);
        }

        updateLearnerProgress();
    }

    protected void updateLearnerProgress() {
        CourseProgress progress = UstadMobileSystemImpl.getInstance().getCourseProgress(
                new String[]{entry.getItemId()}, getContext());
        if(progress == null || progress.getStatus() == CourseProgress.STATUS_NOT_STARTED) {
            catalogEntryView.setLearnerProgressVisible(false);
        }else {
            catalogEntryView.setLearnerProgressVisible(true);
            catalogEntryView.setLearnerProgress(progress);
        }

    }


    /**
     * Handle adding a related item to the see also part of the view.
     *
     * @param item
     */
    protected void handleRelatedItemReady(final RelatedItem item) {
        UmOpdsLink thumbnailLink = item.opdsItem.getThumbnailLink(true);
        final String[] thumbnailUrl = new String[1];
        if(thumbnailLink != null) {
            thumbnailUrl[0] = UMFileUtil.resolveLink(entry.getHref(),
                    thumbnailLink.getHref());
        }

        catalogEntryView.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!seeAlsoVisible) {
                    catalogEntryView.setSeeAlsoVisible(true);
                    seeAlsoVisible = true;
                }
                catalogEntryView.addSeeAlsoItem(item.link, thumbnailUrl[0]);
            }
        });
    }


    public void onStart() {
        if(entryLoaded)
            updateLearnerProgress();
    }

    public void onStop() {
        NetworkTask entryStatusTask = UstadMobileSystemImpl.getInstance().getNetworkManager().getTaskById(
                entryCheckTaskId, NetworkManagerCore.QUEUE_ENTRY_STATUS);
        if(entryStatusTask != null)
            entryStatusTask.stop(NetworkTask.STATUS_STOPPED);

        stopMonitoringLocalAvailability();
    }

    protected void startMonitoringLocalAvailability() {
        if(availabilityMonitorRequest == null) {
            HashSet<String> monitorIdSet = new HashSet<>();
            monitorIdSet.add(entry.getItemId());
            availabilityMonitorRequest = new AvailabilityMonitorRequest(monitorIdSet);
            UstadMobileSystemImpl.getInstance().getNetworkManager().startMonitoringAvailability(
                    availabilityMonitorRequest, true);
        }
    }

    protected void stopMonitoringLocalAvailability() {
        if(availabilityMonitorRequest != null) {
            UstadMobileSystemImpl.getInstance().getNetworkManager().stopMonitoringAvailability(
                    availabilityMonitorRequest);
            availabilityMonitorRequest = null;
        }
    }


    /**
     * Update which buttons are shown according to the acquisition status
     *
     * @param acquisitionStatus
     */
    protected void updateButtonsByStatus(int acquisitionStatus) {
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD,
                acquisitionStatus != CatalogPresenter.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN,
                acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_MODIFY,
                acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED);
        catalogEntryView.setShareButtonVisible(acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED);
    }

    public void handleClickButton(int buttonId) {
        switch(buttonId) {
            case CatalogEntryView.BUTTON_DOWNLOAD:
                Vector selectedEntries = new Vector();
                selectedEntries.addElement(entry);
                handleClickDownload(entryFeed, selectedEntries);
                break;

            case CatalogEntryView.BUTTON_MODIFY:
                handleClickRemove();
                break;
            case CatalogEntryView.BUTTON_OPEN:
                handleOpenEntry(entry);
                break;

        }
    }

    public void handleOpenEntry(UstadJSOPDSEntry entry) {
        CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(entry.getItemId(),
                CatalogPresenter.SHARED_RESOURCE | CatalogPresenter.USER_RESOURCE, getContext());
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if (entryInfo != null && entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED) {
            //see if the user needs to login
            if(impl.getActiveUser(context) == null
                && impl.getAppConfigBoolean(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN, context)){
                openAfterLoginOrRegister = true;
                impl.go(LoginView.VIEW_NAME, context);
                return;
            }

            Hashtable openArgs = new Hashtable();
            openArgs.put(ContainerController.ARG_CONTAINERURI, entryInfo.fileURI);
            openArgs.put(ContainerController.ARG_MIMETYPE, entryInfo.mimeType);
            openArgs.put(ContainerController.ARG_OPFINDEX, new Integer(0));

            String viewName = ContentTypeManager.getViewNameForContentType(entryInfo.mimeType);

            if(viewName != null) {
                UstadMobileSystemImpl.getInstance().go(viewName, openArgs, getContext());
            }else {
                UstadMobileSystemImpl.l(UMLog.ERROR, 672, entryInfo.mimeType);
                impl.getAppView(getContext()).showNotification(impl.getString(0, getContext()),
                        AppView.LENGTH_LONG);
            }
        }else {
            UstadMobileSystemImpl.l(UMLog.ERROR, 673, entryInfo != null ? entryInfo.toString() : null);
            impl.getAppView(getContext()).showNotification(impl.getString(
                    MessageID.error_opening_file, getContext()), AppView.LENGTH_LONG);
        }
    }

    public void handleClickShare() {
        Hashtable args = new Hashtable();
        args.put("title", entry.getTitle());
        args.put("entries", new String[]{entry.getItemId()});
        UstadMobileSystemImpl.getInstance().go("SendCourse", args, getContext());
    }

    protected void handleClickRemove() {
        handleClickRemove(new UstadJSOPDSEntry[]{entry});
    }

    public void handleClickAlternativeTranslationLink(int index) {
        String[] translatedEntryLinks = (String[])alternativeTranslationLinks.elementAt(index);
        UstadJSOPDSEntry translatedEntry = entryFeed != null
                ? entryFeed.findEntryByAlternateHref(translatedEntryLinks[UstadJSOPDSItem.ATTR_HREF])
                : null;
        if(translatedEntry != null) {
            handleOpenEntry(translatedEntry);
        }else {
            String entryUrl = UMFileUtil.resolveLink(entry.getHref(),
                    translatedEntryLinks[UstadJSOPDSItem.ATTR_HREF]);
            handleOpenEntryView(entryUrl);
        }
    }

    public void handleClickSeeAlsoItem(String[] link) {
        if(entryFeed != null) {
            Vector relatedEntryMatch = entryFeed.getEntriesByLinkParams(
                    UstadJSOPDSFeed.LINK_REL_ALTERNATE, null,
                    link[UstadJSOPDSItem.ATTR_HREF], entry.getLanguage());
            if(relatedEntryMatch.size() > 0) {
                handleOpenEntryView((UstadJSOPDSEntry)relatedEntryMatch.elementAt(0));
                return;
            }
        }

        String entryUrl = UMFileUtil.resolveLink(entry.getHref(), link[UstadJSOPDSItem.ATTR_HREF]);
        handleOpenEntryView(entryUrl);
    }

    public void handleClickStopDownload() {
        CatalogEntryInfo entryInfo = CatalogPresenter.getEntryInfo(entry.getItemId(),
                CatalogPresenter.SHARED_RESOURCE | CatalogPresenter.USER_RESOURCE, getContext());
        if(entryInfo != null
                && entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS
                && entryInfo.downloadID > 0) {
            NetworkTask task = UstadMobileSystemImpl.getInstance().getNetworkManager().getTaskById(
                    entryInfo.downloadID, NetworkManagerCore.QUEUE_ENTRY_ACQUISITION);
            if(task != null)
                task.stop(NetworkTask.STATUS_STOPPED);
        }
    }

    
    public void appViewChoiceSelected(int commandId, int choice) {
        switch(commandId) {
            case CMD_REMOVE_PRESENTER_ENTRY:
//                UstadJSOPDSEntry entryToDelete = (UstadJSOPDSEntry)modifyAcquiredEntries[1].elementAt(choice);
//                handleClickRemove(new UstadJSOPDSEntry[]{entryToDelete});
                break;
        }

        super.appViewChoiceSelected(commandId, choice);
    }

    
    protected void onDownloadStarted() {
        catalogEntryView.setProgressVisible(true);
    }

    
    protected void onEntriesRemoved() {
        updateButtonsByStatus(CatalogPresenter.STATUS_NOT_ACQUIRED);
    }

    
    public void setUIStrings() {

    }

    
    public void acquisitionProgressUpdate(String entryId, final AcquisitionTaskStatus status) {
        if(entry != null && entryId.equals(entry.getItemId())) {
            catalogEntryView.runOnUiThread(new Runnable() {
                
                public void run() {
                    if(status.getTotalSize() == -1) {
                        catalogEntryView.setProgress(-1);
                    }else {
                        float completed = (float) status.getDownloadedSoFar() / (float) status.getTotalSize();
                        catalogEntryView.setProgress(completed);
                        catalogEntryView.setProgressStatusText(formatDownloadStatusText(status));
                    }
                }
            });
        }
    }

    
    public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
        if(entryId.equals(entry.getItemId())) {
            switch(status.getStatus()) {
                case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                    catalogEntryView.runOnUiThread(new Runnable() {
                        
                        public void run() {
                            catalogEntryView.setProgressVisible(false);
                            updateButtonsByStatus(CatalogPresenter.STATUS_ACQUIRED);
                        }
                    });
                    break;

                case NetworkTask.STATUS_STOPPED:
                    catalogEntryView.runOnUiThread(new Runnable() {
                        public void run() {
                            catalogEntryView.setProgressVisible(false);
                        }
                    });
                    break;
                //TODO: handle show download failed
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        manager.removeNetworkManagerListener(this);
        manager.removeAcquisitionTaskListener(this);
    }

    public void fileStatusCheckInformationAvailable(String[] fileIds) {
        if(UMUtil.getIndexInArray(entry.getItemId(), fileIds) != -1) {
            final boolean available = manager.isEntryLocallyAvailable(entry.getItemId());
            updateViewLocallyAvailableStatus(available ?
                    CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
        }
    }

    
    public void networkTaskStatusChanged(NetworkTask task) {
        /* $if umplatform != 2  $ */
        if(task.getTaskId() == entryCheckTaskId && task.getStatus() == NetworkTask.STATUS_COMPLETE) {
            boolean available =
                UstadMobileSystemImpl.getInstance().getNetworkManager().getEntryResponsesWithLocalFile(entry.getItemId()) != null;
            updateViewLocallyAvailableStatus(available ?
                CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
            startMonitoringLocalAvailability();
        }
        /* $endif$ */
    }

    private void updateViewLocallyAvailableStatus(final int status) {
        catalogEntryView.runOnUiThread(new Runnable() {
            
            public void run() {
                catalogEntryView.setLocallyAvailableStatus(status);
            }
        });
    }

    /* $if umplatform != 2  $ */
    public void networkNodeDiscovered(NetworkNode node) {

    }

    
    public void networkNodeUpdated(NetworkNode node) {

    }

    /* $endif$ */

    
    public void fileAcquisitionInformationAvailable(String entryId, long downloadId, int downloadSource) {

    }

    
    public void wifiConnectionChanged(String ssid, boolean connected, boolean connectedOrConnecting) {

    }

    @Override
    public void onDialogResult(int commandId, DismissableDialog dialog, Hashtable args) {
        dialog.dismiss();

        if((commandId == LoginController.RESULT_LOGIN_SUCCESSFUL
            || commandId == RegistrationPresenter.RESULT_REGISTRATION_SUCCESS)
            && openAfterLoginOrRegister) {
            openAfterLoginOrRegister = false;
            handleOpenEntry(entry);
        }
    }
}
