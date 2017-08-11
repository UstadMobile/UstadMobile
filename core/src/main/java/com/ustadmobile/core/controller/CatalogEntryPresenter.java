package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
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
import com.ustadmobile.core.view.CatalogEntryView;


import java.io.IOException;
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

    private static final int CMD_REMOVE_PRESENTER_ENTRY = 60;

    private static final int CMD_DOWNLOAD_OTHER_LANG = 61;

    private static final int CMD_MODIFY_ENTRY = 62;

    protected AvailabilityMonitorRequest availabilityMonitorRequest;

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
        manager.addNetworkManagerListener(this);
        if(this.args.containsKey(ARG_ENTRY_OPDS_STR)) {
            try {
                entryFeed = new UstadJSOPDSFeed();
                entryFeed.loadFromString(args.get(ARG_ENTRY_OPDS_STR).toString());
                entry = entryFeed.getEntryById(args.get(ARG_ENTRY_ID).toString());
                catalogEntryView.setTitle(entry.title);

                CatalogEntryInfo entryInfo = CatalogController.getEntryInfo(entry.id,
                        CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE, context);
                catalogEntryView.setDescription(entry.content, entry.getContentType());
                entryTranslationIds = entry.getAlternativeTranslationEntryIds();

                boolean isAcquired = entryInfo != null
                        ? entryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED
                        : false;

                CatalogEntryInfo translatedEntryInfo;
                for(int i = 0; i < entryTranslationIds.length && !isAcquired; i++) {
                    translatedEntryInfo = CatalogController.getEntryInfo(entryTranslationIds[i],
                            CatalogController.SHARED_RESOURCE | CatalogController.USER_RESOURCE,
                            context);
                    isAcquired = translatedEntryInfo != null
                            && translatedEntryInfo.acquisitionStatus == CatalogController.STATUS_ACQUIRED;
                }


                updateButtonsByStatus(isAcquired ? CatalogController.STATUS_ACQUIRED :
                        CatalogController.STATUS_NOT_ACQUIRED);


                //TODO: as this is bound to the activity - this might not be ready - lifecycle implication needs handled
                NetworkManagerCore manager  = UstadMobileSystemImpl.getInstance().getNetworkManager();
                /* $if umplatform != 2  $ */
                List<EntryCheckResponse> fileResponse = manager.getEntryResponsesWithLocalFile(entry.id);
                if(fileResponse != null) {
                    catalogEntryView.setLocallyAvailableStatus(CatalogEntryView.LOCAL_STATUS_AVAILABLE);
                }
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
                            thumbnailUrl = UMFileUtil.resolveLink(
                                    entryFeed.getAbsoluteSelfLink()[UstadJSOPDSItem.ATTR_HREF],
                                    thumbnailLink[UstadJSOPDSItem.ATTR_HREF]);
                        }

                        catalogEntryView.addSeeAlsoItem((String[])relatedLinks.elementAt(i),
                                thumbnailUrl);
                    }
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
                handleClickModify();
                break;
            case CatalogEntryView.BUTTON_OPEN:
                handleClickOpenEntry(entry);
                break;

        }
    }

    protected void handleClickModify() {
        modifyAcquiredEntries = getTranslatedAlternativesLangVectors(entry,
                CatalogController.STATUS_ACQUIRED);

        modifyUnacquiredEntries = getTranslatedAlternativesLangVectors(entry,
                CatalogController.STATUS_NOT_ACQUIRED);

        Vector modifyCommandsAvailableVector = new Vector();
        if(modifyAcquiredEntries[0].size() > 0)
            modifyCommandsAvailableVector.addElement(new Integer(MessageID.delete));

        if(modifyUnacquiredEntries[0].size() > 0)
            modifyCommandsAvailableVector.addElement(new Integer(MessageID.download_in_another_language));

        modifyCommandsAvailable = new Integer[modifyCommandsAvailableVector.size()];
        modifyCommandsAvailableVector.copyInto(modifyCommandsAvailable);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] modifyOptionsToShow = new String[modifyCommandsAvailable.length];
        for(int i = 0; i < modifyOptionsToShow.length; i++) {
            modifyOptionsToShow[i] = impl.getString(modifyCommandsAvailable[i].intValue(),
                    getContext());
        }

        impl.getAppView(getContext()).showChoiceDialog(impl.getString(MessageID.modify, getContext()),
                modifyOptionsToShow, CMD_MODIFY_ENTRY, this);
    }

    public void handleClickSeeAlsoItem(String[] link) {
        Vector relatedEntryMatch = entryFeed.getEntriesByLinkParams(
                UstadJSOPDSFeed.LINK_REL_ALTERNATE, null,
                link[UstadJSOPDSItem.ATTR_HREF], entry.getLanguage());
        if(relatedEntryMatch.size() > 0) {
            handleOpenEntryView((UstadJSOPDSEntry)relatedEntryMatch.elementAt(0));
        }
    }

    
    public void appViewChoiceSelected(int commandId, int choice) {
        switch(commandId) {
            case CMD_MODIFY_ENTRY:
                int cmdChosen = modifyCommandsAvailable[choice].intValue();

                switch(cmdChosen) {
                    case MessageID.download_in_another_language:
                        Vector selectedEntries = new Vector();
                        selectedEntries.addElement(entry);
                        String[] languageChoices = new String[modifyUnacquiredEntries[0].size()];
                        for(int i = 0; i < languageChoices.length; i++) {
                            languageChoices[i] = ((UstadJSOPDSEntry)
                                    modifyUnacquiredEntries[1].elementAt(i)).getLanguage();
                        }
                        handleClickDownload(entryFeed, selectedEntries, languageChoices, true);
                        break;

                    case MessageID.delete:
                        if(modifyAcquiredEntries[1].size() == 1) {
                            handleClickRemove(new UstadJSOPDSEntry[]{
                                    (UstadJSOPDSEntry) modifyAcquiredEntries[1].elementAt(0)});
                        }else {
                            String[] languagesToRemove = new String[modifyAcquiredEntries[0].size()];
                            modifyAcquiredEntries[0].copyInto(languagesToRemove);
                            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                            impl.getAppView(getContext()).showChoiceDialog(
                                    impl.getString(MessageID.delete, getContext()), languagesToRemove,
                                    CMD_REMOVE_PRESENTER_ENTRY,this);
                        }

                        break;
                }

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
        //check if any version is still acquired
        Hashtable acquiredVersions = getTranslatedAlternatives(entry, CatalogController.STATUS_ACQUIRED);
        if(acquiredVersions.size() == 0) {
            catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_DOWNLOAD, true);
            catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_MODIFY, false);
            catalogEntryView.setButtonDisplayed(CatalogEntryView.BUTTON_OPEN, false);
        }
    }

    
    public void setUIStrings() {

    }

    
    public void acquisitionProgressUpdate(String entryId, final AcquisitionTaskStatus status) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(entry != null && (entryId.equals(entry.id) || UMUtil.getIndexInArray(entryId, entryTranslationIds) != -1)) {
            catalogEntryView.runOnUiThread(new Runnable() {
                
                public void run() {
                    if(status.getTotalSize() == -1) {
                        catalogEntryView.setProgress(-1);
                    }else {
                        float completed = (float) status.getDownloadedSoFar() / (float) status.getTotalSize();
                        String statusText = impl.getString(MessageID.downloading, getContext())
                                + ": " + Math.round(completed * 100) + "%";
                        catalogEntryView.setProgress(completed);
                        catalogEntryView.setProgressStatusText(statusText);
                    }
                }
            });
        }
    }

    
    public void acquisitionStatusChanged(String entryId, AcquisitionTaskStatus status) {
        if(entryId.equals(entry.id) || UMUtil.getIndexInArray(entryId, entryTranslationIds) != -1) {
            switch(status.getStatus()) {
                case UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL:
                    catalogEntryView.runOnUiThread(new Runnable() {
                        
                        public void run() {
                            catalogEntryView.setProgressVisible(false);
                            updateButtonsByStatus(CatalogController.STATUS_ACQUIRED);
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
