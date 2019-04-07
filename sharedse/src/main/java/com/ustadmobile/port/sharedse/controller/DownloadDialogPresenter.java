package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_CONTENT_ENTRY_UID = "contentEntryUid";

    public static final String ARG_DOWNLOAD_SET_UID = "downoad_set_uid";

    private boolean deleteFileOptions = false;

    private UmAppDatabase umAppDatabase;

    private long contentEntryUid = 0L;

    private UmLiveData<DownloadJob> downloadDownloadJobLive;

    private UmLiveData<Boolean> allowedMeteredLive;

    private volatile long downloadSetUid;

    private volatile long downloadJobUid = 0L;

    private UstadMobileSystemImpl impl;

    private String statusMessage = null;

    private String destinationDir = null;

    public static final int STACKED_BUTTON_PAUSE = 0;

    public static final int STACKED_BUTTON_CANCEL = 1;

    public static final int STACKED_BUTTON_CONTINUE = 2;

    private NetworkManagerBle networkManagerBle;

    private Hashtable args;

    public DownloadDialogPresenter(Object context, NetworkManagerBle networkManagerBle,
                                   Hashtable arguments, DownloadDialogView view) {
        super(context, arguments, view);
        this.args = arguments;
        this.networkManagerBle = networkManagerBle;

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        umAppDatabase = UmAppDatabase.getInstance(context);

        impl = UstadMobileSystemImpl.getInstance();
        contentEntryUid = Long.parseLong(String.valueOf(getArguments()
                .get(ARG_CONTENT_ENTRY_UID)));
        view.setWifiOnlyOptionVisible(false);

        impl.getStorageDirs(context, result -> {
            destinationDir = result.get(0).getDirURI();
            view.runOnUiThread(() -> view.setUpStorageOptions(result));
            new Thread(this::setup).start();
        });

    }


    private void startObservingJob(){
        view.runOnUiThread(() -> {
            downloadDownloadJobLive = umAppDatabase.getDownloadJobDao().getJobLive(downloadJobUid);
            downloadDownloadJobLive.observe(DownloadDialogPresenter.this,
                    this::handleDownloadJobStatusChange);
        });
    }

    private void startObservingDownloadSetMeteredState(){
        view.runOnUiThread(() -> {
            allowedMeteredLive = umAppDatabase.getDownloadSetDao()
                    .getLiveMeteredNetworkAllowed(downloadSetUid);
            allowedMeteredLive.observe(DownloadDialogPresenter.this,
                    this::handleDownloadSetMeteredStateChange);
        });
    }

    private void handleDownloadSetMeteredStateChange(Boolean meteredConnection){
        view.setDownloadOverWifiOnly(meteredConnection != null && !meteredConnection);
    }

    private void handleDownloadJobStatusChange(DownloadJob downloadJob){
        if(downloadJob != null){
            int downloadStatus = downloadJob.getDjStatus();
            view.setCalculatingViewVisible(false);
            view.setWifiOnlyOptionVisible(true);
            if(downloadStatus >= JobStatus.COMPLETE_MIN){
                deleteFileOptions = true;
                view.setStackOptionsVisible(false);
                view.setBottomButtonsVisible(true);
                statusMessage = impl.getString(MessageID.download_state_downloaded,
                        getContext());
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download_delete_btn_label,getContext()));
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.download_cancel_label,getContext()));
            }else if(downloadStatus >= JobStatus.RUNNING_MIN){
                deleteFileOptions = false;
                view.setStackOptionsVisible(true);
                view.setBottomButtonsVisible(false);
                String [] optionTexts = new String[]{
                        impl.getString(MessageID.download_pause_stacked_label,getContext()),
                        impl.getString(MessageID.download_cancel_stacked_label,getContext()),
                        impl.getString(MessageID.download_continue_stacked_label,getContext())
                };
                statusMessage = impl.getString(MessageID.download_state_downloading,
                        getContext());
                view.setStackedOptions(
                        new int[]{STACKED_BUTTON_PAUSE, STACKED_BUTTON_CANCEL,
                                STACKED_BUTTON_CONTINUE},
                        optionTexts);
            }else{
                deleteFileOptions = false;
                statusMessage = impl.getString(MessageID.download_state_download,
                        getContext());
                view.setStackOptionsVisible(false);
                view.setBottomButtonsVisible(true);
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download_continue_btn_label,getContext()));
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.download_cancel_label,getContext()));
            }


            new Thread(() -> {
                int totalDownloadJobItems = umAppDatabase.getDownloadJobItemDao()
                        .getTotalDownloadJobItems(downloadJobUid);
                view.runOnUiThread(() -> view.setStatusText(statusMessage, totalDownloadJobItems,
                        UMFileUtil.formatFileSize(downloadJob.getTotalBytesToDownload())));
            }).start();

        }
    }

    private void setup() {
        downloadSetUid = umAppDatabase.getDownloadSetDao()
                .findDownloadSetUidByRootContentEntryUid(contentEntryUid);
        if(downloadSetUid == 0)
            downloadSetUid = umAppDatabase.getDownloadSetItemDao()
                    .findDownloadSetUidByContentEntryUid(contentEntryUid);

        if(downloadSetUid == 0)
            createDownloadSet();

        downloadJobUid = umAppDatabase.getDownloadJobDao().getLatestDownloadJobUidForDownloadSet(
                downloadSetUid);

        startObservingJob();

        startObservingDownloadSetMeteredState();

    }

    private void createDownloadSet() {
        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setDestinationDir(destinationDir);
        downloadSet.setDsRootContentEntryUid(contentEntryUid);

        downloadSetUid = umAppDatabase.getDownloadSetDao().insert(downloadSet);
        List<DownloadSetItem> downloadSetItems = new ArrayList<>();

        List<ContentEntryParentChildJoinDao.ContentEntryParentChildJoinSummary> currentChildUids;
        Set<Long> allChildUids = new HashSet<>();
        List<Long> parentUids = new ArrayList<>();
        parentUids.add(contentEntryUid);
        allChildUids.add(contentEntryUid);

        do {
            currentChildUids = umAppDatabase.getContentEntryParentChildJoinDao()
                    .findChildEntriesByParents(parentUids);
            parentUids.clear();
            for(ContentEntryParentChildJoinDao.ContentEntryParentChildJoinSummary child : currentChildUids){
                if(!allChildUids.contains(child.getChildContentEntryUid())) {
                    allChildUids.add(child.getChildContentEntryUid());
                    if(!child.isLeaf())
                        parentUids.add(child.getChildContentEntryUid());
                }
            }
        }while(!parentUids.isEmpty());

        for(long childUid : allChildUids) {
            downloadSetItems.add(new DownloadSetItem(downloadSetUid, childUid));
        }

        umAppDatabase.getDownloadSetItemDao().insert(downloadSetItems);

        createDownloadJob();
    }

    private void createDownloadJob() {
        DownloadJob downloadJob = new DownloadJob();
        downloadJob.setTimeRequested(System.currentTimeMillis());
        downloadJob.setTimeCreated(System.currentTimeMillis());
        downloadJob.setDjStatus(JobStatus.NOT_QUEUED);
        downloadJob.setDjDsUid(downloadSetUid);
        downloadJob.setDjUid(umAppDatabase.getDownloadJobDao().insert(downloadJob));
        downloadJobUid = downloadJob.getDjUid();


        List<DownloadJobItemDao.DownloadJobItemToBeCreated> itemToBeCreated =
                umAppDatabase.getDownloadJobItemDao()
                        .findJobItemsToBeCreatedDownloadSet(downloadSetUid);

        List<DownloadJobItem> jobItems = new ArrayList<>();
        List<ContentEntryStatus> statusList = new ArrayList<>();
        long totalSize = 0L;
        for(DownloadJobItemDao.DownloadJobItemToBeCreated item: itemToBeCreated){
            totalSize += item.getFileSize();
            DownloadJobItem jobItem = new DownloadJobItem();
            jobItem.setDjiContainerUid(item.getContainerUid());
            jobItem.setDjiDjUid(downloadJobUid);
            jobItem.setDjiStatus(JobStatus.NOT_QUEUED);
            jobItem.setDownloadLength(item.getFileSize());
            jobItem.setDjiDsiUid(item.getDownloadSetItemUid());
            jobItem.setDestinationFile(UMFileUtil.joinPaths(destinationDir,
                    String.valueOf(item.getContainerUid())));
            jobItems.add(jobItem);

            statusList.add(new ContentEntryStatus(item.getContentEntryUid(),
                    item.getFileSize() > 0, item.getFileSize()));
        }
        downloadJob.setTotalBytesToDownload(totalSize);
        umAppDatabase.getContentEntryStatusDao().insertOrAbort(statusList);
        umAppDatabase.getDownloadJobItemDao().insert(jobItems);
        umAppDatabase.getDownloadJobItemDao().update(downloadJob);
    }



    public void handleClickPositive() {
        if(deleteFileOptions){
            args.put(ARG_DOWNLOAD_SET_UID,String.valueOf(downloadSetUid));
            new Thread(() -> networkManagerBle.cancelAndDeleteDownloadSet(args)).start();
        }else{
            continueDownloading();
        }
    }

    /**
     * Handle negative click. If the underlying system is already dismissing the dialog
     * set dismissAfter to false to avoid a call to dismissDialog
     * @param dismissAfter flag to indicate if the dialog will be dismissed after the selection
     */
    public void handleClickNegative(boolean dismissAfter) {
        //if the download has not been started
        umAppDatabase.getDownloadSetDao().cleanupUnused(downloadSetUid);
        if(dismissAfter)
            dismissDialog();
    }

    public void handleClickNegative() {
        handleClickNegative(true);
    }

    public void handleClickStackedButton(int idClicked) {
        switch (idClicked) {
            case STACKED_BUTTON_PAUSE:
                new Thread(() -> umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJobUid,
                        JobStatus.PAUSED, JobStatus.PAUSING)).start();
                break;

            case STACKED_BUTTON_CONTINUE:
                continueDownloading();
                break;

            case STACKED_BUTTON_CANCEL:
                cancelDownload();
                break;
        }

        dismissDialog();
    }



    private void continueDownloading(){
        new Thread(() -> umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJobUid,
                JobStatus.QUEUED, -1)).start();
    }

    private void dismissDialog(){
        view.runOnUiThread(() -> view.dismissDialog());
    }

    private void cancelDownload(){
        new Thread(() -> umAppDatabase.getDownloadJobDao()
                .updateJobAndItems(downloadJobUid, JobStatus.CANCELED,
                        JobStatus.CANCELLING)).start();
    }

    public void handleWiFiOnlyOption(boolean wifiOnly){
        new Thread(() -> umAppDatabase.getDownloadSetDao()
                .setMeteredConnectionBySetUid(downloadSetUid,!wifiOnly)).start();
    }

    public void handleStorageOptionSelection(String selectedDir){
        new Thread(() -> umAppDatabase.getDownloadSetDao().updateDestinationDirectory(
                downloadSetUid, selectedDir,null)).start();
    }

    /**
     * Testing purpose
     */
    protected long getCurrentJobId(){
        return downloadJobUid;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
