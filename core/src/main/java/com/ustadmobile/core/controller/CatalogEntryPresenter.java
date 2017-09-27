package com.ustadmobile.core.controller;

import com.ustadmobile.core.catalog.ContentTypeManager;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.networkmanager.AcquisitionListener;
import com.ustadmobile.core.networkmanager.AcquisitionTaskStatus;
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.CatalogEntryView;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform != 2 $ */
import java.util.List;
import com.ustadmobile.core.networkmanager.NetworkNode;
import com.ustadmobile.core.networkmanager.EntryCheckResponse;
/* $endif */

/**
 * Created by mike on 4/17/17.
 */

public class CatalogEntryPresenter extends BaseCatalogController implements AcquisitionListener, NetworkManagerListener{

    private CatalogEntryView catalogEntryView;

    private Hashtable args;

    public static final String ARG_ENTRY_OPDS_STR = "opds_str";

    public static final String ARG_ENTRY_ID = "entry_id";

    private UstadJSOPDSEntry entry;

    private UstadJSOPDSFeed entryFeed;

    private NetworkManagerCore manager;

    private long entryCheckTaskId = -1;

    private String[] entryTranslationIds;

    private Integer[] modifyCommandsAvailable;

    private Vector[] modifyAcquiredEntries;

    private Vector[] modifyUnacquiredEntries;

    private Vector[] sharedAcquiredEntries;

    private static final int CMD_REMOVE_PRESENTER_ENTRY = 60;

    private static final int CMD_DOWNLOAD_OTHER_LANG = 61;

    private static final int CMD_MODIFY_ENTRY = 62;

    private static final int CMD_SHARE_ENTRY = 63;

    protected AvailabilityMonitorRequest availabilityMonitorRequest;

    private long downloadTaskId;

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
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(this.args.containsKey(ARG_ENTRY_OPDS_STR)) {
            try {
                entryFeed = new UstadJSOPDSFeed();
                entryFeed.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                entry = entryFeed.getEntryById(args.get(ARG_ENTRY_ID).toString());
                catalogEntryView.setTitle(entry.title);

                CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                        CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, context);
                catalogEntryView.setDescription(entry.content, entry.getContentType());
                String[] firstAcquisitionLink = entry.getFirstAcquisitionLink(null);
                if(firstAcquisitionLink != null
                        && firstAcquisitionLink[UstadJSOPDSItem.ATTR_LENGTH] != null) {
                    catalogEntryView.setSize(impl.getString(MessageID.size, getContext())
                            + ": "
                            + UMFileUtil.formatFileSize(
                                Long.valueOf(firstAcquisitionLink[UstadJSOPDSItem.ATTR_LENGTH])));
                }

                entryTranslationIds = entry.getAlternativeTranslationEntryIds();

                //set the available translated versions that can be found
                Vector alternativeTranslationLinks = entry.getAlternativeTranslationLinks();

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
                        ? entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED
                        : false;

                updateButtonsByStatus(isAcquired ? CatalogController.STATUS_ACQUIRED :
                        CatalogController.STATUS_NOT_ACQUIRED);


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
                List<EntryCheckResponse> fileResponse = manager.getEntryResponsesWithLocalFile(entry.id);
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
                    for(int i = 0; i < relatedLinks.size(); i++) {
                        currentLink = (String[])relatedLinks.elementAt(i);
                        Vector relatedEntryMatch = entryFeed.getEntriesByLinkParams(
                                UstadJSOPDSFeed.LINK_REL_ALTERNATE, null,
                                currentLink[UstadJSOPDSItem.ATTR_HREF], entry.getLanguage());
                        UstadJSOPDSEntry entryLink;
                        if(relatedEntryMatch != null && relatedEntryMatch.size() > 0) {
                            entryLink = (UstadJSOPDSEntry)relatedEntryMatch.elementAt(0);
                            thumbnailLink = entryLink.getThumbnailLink(true);
                            if(thumbnailLink != null)
                                thumbnailUrl = UMFileUtil.resolveLink(
                                        entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                                        thumbnailLink[UstadJSOPDSItem.ATTR_HREF]);
                        }

                        catalogEntryView.addSeeAlsoItem((String[])relatedLinks.elementAt(i),
                                thumbnailUrl);
                    }

                    if(relatedLinks.size() == 0)
                        catalogEntryView.setSeeAlsoVisible(false);
                }

                Vector coverImages = entry.getLinks(UstadJSOPDSItem.LINK_COVER_IMAGE, null);
                if(coverImages != null && coverImages.size() > 0) {
                    String coverImageUrl = UMFileUtil.resolveLink(
                            entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                            ((String[])coverImages.elementAt(0))[UstadJSOPDSItem.ATTR_HREF]);
                    catalogEntryView.setHeader(coverImageUrl);
                }

                Vector thumbnails = entry.getThumbnails();
                if(thumbnails != null && thumbnails.size() > 0) {
                    String thumbnailUrl = UMFileUtil.resolveLink(
                            entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                            ((String[]) thumbnails.elementAt(0))[UstadJSOPDSItem.ATTR_HREF]);
                    catalogEntryView.setIcon(thumbnailUrl);
                }

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        UstadMobileSystemImpl.getInstance().getNetworkManager().addAcquisitionTaskListener(this);
    }

    public void onStart() {
        String[] progressIds = new String[entryTranslationIds.length + 1];
        for(int i = 0; i < entryTranslationIds.length; i++) {
            progressIds[i] = entryTranslationIds[i];
        }
        progressIds[entryTranslationIds.length] = entry.id;

        CourseProgress progress = UstadMobileSystemImpl.getInstance().getCourseProgress(progressIds,
                getContext());
        if(progress == null || progress.getStatus() == CourseProgress.STATUS_NOT_STARTED) {
            catalogEntryView.setLearnerProgressVisible(false);
        }else {
            catalogEntryView.setLearnerProgressVisible(true);
            catalogEntryView.setLearnerProgress(progress);
        }
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
            monitorIdSet.add(entry.id);
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
                acquisitionStatus != CatalogController.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN,
                acquisitionStatus == CatalogController.STATUS_ACQUIRED);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_MODIFY,
                acquisitionStatus == CatalogController.STATUS_ACQUIRED);
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
        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, getContext());
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if (entryInfo != null && entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED) {
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
        sharedAcquiredEntries = getTranslatedAlternativesLangVectors(entry,
                CatalogController.STATUS_ACQUIRED);
        if(sharedAcquiredEntries[0].size() == 0) {
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                    "Not downloaded (in this language)!", AppView.LENGTH_LONG);
        }else if(sharedAcquiredEntries[0].size() == 1) {
            UstadJSOPDSEntry entry = (UstadJSOPDSEntry)sharedAcquiredEntries[1].elementAt(0);
            handleShareSelectedEntry(entry.id);
        }else {
            String[] languagesToShare = new String[sharedAcquiredEntries[0].size()];
            sharedAcquiredEntries[0].copyInto(languagesToShare);
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.getAppView(getContext()).showChoiceDialog(
                    impl.getString(MessageID.share, getContext()), languagesToShare,
                    CMD_SHARE_ENTRY,this);
        }
    }

    protected void handleShareSelectedEntry(String entryId) {
        Hashtable args = new Hashtable();
        args.put("title", entry.title);
        args.put("entries", new String[]{entryId});
        UstadMobileSystemImpl.getInstance().go("SendCourse", args, getContext());
    }


    protected void handleClickRemove() {
        handleClickRemove(new UstadJSOPDSEntry[]{entry});
    }

    public void handleClickAlternativeTranslationLink(int index) {
        UstadJSOPDSEntry entry = entryFeed.getEntryById(entryTranslationIds[index]);
        if(entry != null) {
            handleOpenEntryView(entry);
        }
    }

    public void handleClickSeeAlsoItem(String[] link) {
        Vector relatedEntryMatch = entryFeed.getEntriesByLinkParams(
                UstadJSOPDSFeed.LINK_REL_ALTERNATE, null,
                link[UstadJSOPDSItem.ATTR_HREF], entry.getLanguage());
        if(relatedEntryMatch.size() > 0) {
            handleOpenEntryView((UstadJSOPDSEntry)relatedEntryMatch.elementAt(0));
        }
    }

    public void handleClickStopDownload() {
        CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, getContext());
        if(entryInfo != null
                && entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUISITION_IN_PROGRESS
                && entryInfo.downloadID > 0) {
            NetworkTask task = UstadMobileSystemImpl.getInstance().getNetworkManager().getTaskById(
                    entryInfo.downloadID, NetworkManagerCore.QUEUE_ENTRY_ACQUISITION);
            if(task != null)
                task.stop(NetworkTask.STATUS_STOPPED);
        }
    }

    
    public void appViewChoiceSelected(int commandId, int choice) {
        switch(commandId) {
            case CMD_SHARE_ENTRY:
                UstadJSOPDSEntry entryToShare = (UstadJSOPDSEntry)sharedAcquiredEntries[1].elementAt(choice);
                handleShareSelectedEntry(entryToShare.id);
                break;

            case CMD_REMOVE_PRESENTER_ENTRY:
                UstadJSOPDSEntry entryToDelete = (UstadJSOPDSEntry)modifyAcquiredEntries[1].elementAt(choice);
                handleClickRemove(new UstadJSOPDSEntry[]{entryToDelete});
                break;
        }

        super.appViewChoiceSelected(commandId, choice);
    }

    
    protected void onDownloadStarted() {
        catalogEntryView.setProgressVisible(true);
    }

    
    protected void onEntriesRemoved() {
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD, true);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_MODIFY, false);
        catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN, false);
    }

    
    public void setUIStrings() {

    }

    
    public void acquisitionProgressUpdate(String entryId, final AcquisitionTaskStatus status) {
        if(entry != null && entryId.equals(entry.id)) {
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
        if(entryId.equals(entry.id)) {
            switch(status.getStatus()) {
                case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                    catalogEntryView.runOnUiThread(new Runnable() {
                        
                        public void run() {
                            catalogEntryView.setProgressVisible(false);
                            updateButtonsByStatus(CatalogController.STATUS_ACQUIRED);
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
        if(UMUtil.getIndexInArray(entry.id, fileIds) != -1) {
            final boolean available = manager.isEntryLocallyAvailable(entry.id);
            updateViewLocallyAvailableStatus(available ?
                    CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
        }
    }

    
    public void networkTaskStatusChanged(NetworkTask task) {
        /* $if umplatform != 2  $ */
        if(task.getTaskId() == entryCheckTaskId && task.getStatus() == NetworkTask.STATUS_COMPLETE) {
            boolean available =
                UstadMobileSystemImpl.getInstance().getNetworkManager().getEntryResponsesWithLocalFile(entry.id) != null;
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
}
