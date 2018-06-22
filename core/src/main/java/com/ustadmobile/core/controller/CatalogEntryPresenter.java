package com.ustadmobile.core.controller;

import com.ustadmobile.core.catalog.ContentTypeManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.BaseUmCallback;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest;
import com.ustadmobile.core.networkmanager.NetworkManagerCore;
import com.ustadmobile.core.networkmanager.NetworkManagerListener;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static com.ustadmobile.lib.db.entities.OpdsEntry.ENTRY_PROTOCOL;

/* $if umplatform != 2 $ */
/* $endif */

/**
 * Created by mike on 4/17/17.
 */

public class CatalogEntryPresenter extends BaseCatalogPresenter implements NetworkManagerListener,
        DialogResultListener{

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

    private NetworkManagerCore manager;

    private long entryCheckTaskId = -1;


    private static final int CMD_REMOVE_PRESENTER_ENTRY = 60;

    private static final int CMD_SHARE_ENTRY = 63;

    protected AvailabilityMonitorRequest availabilityMonitorRequest;

    private boolean openAfterLoginOrRegister = false;

    private boolean entryLoaded = false;


    private UmLiveData<OpdsEntryWithRelations> entryLiveData;

    private UmObserver<DownloadJobItemWithDownloadSetItem> entryDownloadJobItemObserver;

    private UmLiveData<DownloadJobItemWithDownloadSetItem> entryDownloadJobLiveData;

    private String baseHref;

    private String currentEntryId;

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

        if(args.containsKey(ARG_BASE_HREF)) {
            baseHref = (String)args.get(ARG_BASE_HREF);
        }

        String entryUri = (String)args.get(ARG_URL);
        if(entryUri.startsWith(ENTRY_PROTOCOL)) {
            String entryUuid = entryUri.substring(ENTRY_PROTOCOL.length());
            entryLiveData = UmAppDatabase.getInstance(getContext()).getOpdsEntryWithRelationsDao()
                    .getEntryByUuid(entryUuid);
            entryLiveData.observe(this, this::handleEntryUpdated);
        }

        if(this.args.containsKey(ARG_TITLEBAR_TEXT))
            catalogEntryView.setTitlebarText((String)this.args.get(ARG_TITLEBAR_TEXT));

        catalogEntryView.setLearnerProgressVisible(false);
    }

    public void handleEntryUpdated(OpdsEntryWithRelations entry) {
        catalogEntryView.setEntryTitle(entry.getTitle());
        catalogEntryView.setDescription(entry.getContent(), entry.getContentType());
        OpdsLink acquisitionLink = entry.getAcquisitionLink(null, true);
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        long containerSize = -1;
        if(acquisitionLink != null && acquisitionLink.getLength() > 0)
            containerSize = acquisitionLink.getLength();

        OpdsLink thumbnailLink = entry.getThumbnailLink(true);


        if(entry.getUrl() != null){
            baseHref = entry.getUrl();
        }

        if(thumbnailLink != null && baseHref != null) {
            catalogEntryView.setThumbnail(UMFileUtil.resolveLink(baseHref, thumbnailLink.getHref()),
                    thumbnailLink.getMimeType());
        }else if(thumbnailLink != null){
            UmAppDatabase.getInstance(getContext()).getOpdsEntryWithRelationsDao()
                    .findParentUrlByChildUuid(entry.getUuid(), new UmCallback<String>() {
                        @Override
                        public void onSuccess(String parentUrl) {
                            if(parentUrl != null){
                                CatalogEntryPresenter.this.baseHref = parentUrl;
                                catalogEntryView.runOnUiThread(() -> catalogEntryView.setThumbnail(
                                        UMFileUtil.resolveLink(baseHref, thumbnailLink.getHref()),
                                        thumbnailLink.getMimeType()));
                            }
                        }

                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    });
        }

        int containerFileId = -1;
        if(entry.getContainerFileEntries() != null && entry.getContainerFileEntries().size() > 0) {
            updateButtonsByStatus(CatalogPresenter.STATUS_ACQUIRED);
            containerFileId = entry.getContainerFileEntries().get(0).getContainerFileId();
        }else {
            updateButtonsByStatus(CatalogPresenter.STATUS_NOT_ACQUIRED);
        }

        if(currentEntryId == null || !currentEntryId.equals(entry.getEntryId())){
            if(entryDownloadJobItemObserver != null)
                entryDownloadJobLiveData.removeObserver(entryDownloadJobItemObserver);
        }

        String sizePrefix =  impl.getString(MessageID.size, getContext()) +  ": ";

        if(containerSize <= 0 && containerFileId != -1){
            UmAppDatabase.getInstance(getContext()).getContainerFileDao().findContainerFileLengthAsync(
                    containerFileId, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            catalogEntryView.runOnUiThread(() -> catalogEntryView.setSize(sizePrefix +
                                    UMFileUtil.formatFileSize(result)));
                        }

                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    }
            );
        }else if(containerSize > 0){
            catalogEntryView.setSize(sizePrefix + UMFileUtil.formatFileSize(containerSize));
        }else{
            catalogEntryView.setSize("");
        }

        entryDownloadJobItemObserver = this::handleDownloadJobItemUpdated;
        entryDownloadJobLiveData = UmAppDatabase.getInstance(getContext()).getDownloadJobItemDao()
                .findDownloadJobItemByEntryIdAndStatusRangeLive(entry.getEntryId(),
                        NetworkTask.STATUS_WAITING_MIN, NetworkTask.STATUS_RUNNING_MAX);
        entryDownloadJobLiveData.observe(this, entryDownloadJobItemObserver);

        currentEntryId = entry.getEntryId();
    }

    public void handleDownloadJobItemUpdated(DownloadJobItemWithDownloadSetItem jobItem) {
        if(jobItem != null) {
            catalogEntryView.setProgressVisible(true);
            float completed = (float) jobItem.getDownloadedSoFar() / (float) jobItem.getDownloadLength();
            catalogEntryView.setProgress(completed);
            catalogEntryView.setProgressStatusText(formatDownloadStatusText(jobItem));
        }else {
            catalogEntryView.setProgressVisible(false);
        }
    }

    protected void updateLearnerProgress() {
//        CourseProgress progress = UstadMobileSystemImpl.getInstance().getCourseProgress(
//                new String[]{entry.getItemId()}, getContext());
//        if(progress == null || progress.getStatus() == CourseProgress.STATUS_NOT_STARTED) {
//            catalogEntryView.setLearnerProgressVisible(false);
//        }else {
//            catalogEntryView.setLearnerProgressVisible(true);
//            catalogEntryView.setLearnerProgress(progress);
//        }

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
            monitorIdSet.add(entryLiveData.getValue().getEntryId());
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
                handleClickDownload(Arrays.asList(entryLiveData.getValue()));
                break;

            case CatalogEntryView.BUTTON_MODIFY:
                handleClickRemove();
                break;
            case CatalogEntryView.BUTTON_OPEN:
                handleOpenEntry();
                break;

        }
    }

    public void handleOpenEntry() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //TODO: check if user login is required, and if so, is the user logged in?


        /**
         * Find if this entry is in a ContainerFile
         */
        List<ContainerFileEntry> containerFileEntries = entryLiveData.getValue().getContainerFileEntries();

        if(containerFileEntries != null && containerFileEntries.size() > 0) {
            int containerFileId = containerFileEntries.get(0).getContainerFileId();
            UmAppDatabase.getInstance(getContext()).getContainerFileDao()
                    .getContainerFileByIdAsync(containerFileId, new BaseUmCallback<ContainerFile>() {
                        @Override
                        public void onSuccess(ContainerFile result) {
                            Hashtable args = new Hashtable();
                            args.put(ContainerController.ARG_CONTAINERURI, result.getNormalizedPath());
                            args.put(ContainerController.ARG_MIMETYPE, result.getMimeType());
                            args.put(ContainerController.ARG_OPFINDEX, Integer.valueOf(0));
                            impl.go(ContentTypeManager.getViewNameForContentType(result.getMimeType()),
                                    args, getContext());
                        }
                    });
        }
    }

    public void handleClickShare() {
        Hashtable args = new Hashtable();
        args.put("title", entryLiveData.getValue().getTitle());
        args.put("entries", new String[]{entryLiveData.getValue().getUuid()});
        UstadMobileSystemImpl.getInstance().go("SendCourse", args, getContext());
    }

    protected void handleClickRemove() {
//        TODO: Re-implement for #dbarch2
//        handleClickRemove(new UstadJSOPDSEntry[]{entry});
    }

    public void handleClickAlternativeTranslationLink(int index) {
//        TODO: Re-implement for #dbarch2
    }

    public void handleClickSeeAlsoItem(String[] link) {
//        TODO: Re-implement for #dbarch2
    }

    public void handleClickStopDownload() {
//        TODO: Re-implement for #dbarch2
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


    public void onDestroy() {
        super.onDestroy();
        manager.removeNetworkManagerListener(this);
    }

    public void fileStatusCheckInformationAvailable(String[] fileIds) {
        if(UMUtil.getIndexInArray(currentEntryId, fileIds) != -1) {
            final boolean available = manager.isEntryLocallyAvailable(currentEntryId);
            updateViewLocallyAvailableStatus(available ?
                    CatalogEntryView.LOCAL_STATUS_AVAILABLE : CatalogEntryView.LOCAL_STATUS_NOT_AVAILABLE);
        }
    }

    
    public void networkTaskStatusChanged(NetworkTask task) {
        /* $if umplatform != 2  $ */
        if(task.getTaskId() == entryCheckTaskId && task.getStatus() == NetworkTask.STATUS_COMPLETE) {
            boolean available =
                UstadMobileSystemImpl.getInstance().getNetworkManager().getEntryResponsesWithLocalFile(currentEntryId) != null;
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
            handleOpenEntry();
        }
    }
}
