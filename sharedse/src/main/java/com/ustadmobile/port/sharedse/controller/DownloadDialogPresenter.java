package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadJobItemManager;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.Map;

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_CONTENT_ENTRY_UID = "contentEntryUid";

    private boolean deleteFileOptions = false;

    private UmAppDatabase appDatabase;

    private UmAppDatabase appDatabaseRepo;

    private long contentEntryUid = 0L;

    private UmLiveData<DownloadJob> downloadDownloadJobLive;

    private UmLiveData<Boolean> allowedMeteredLive;

    private volatile long downloadJobUid = 0L;

    private UstadMobileSystemImpl impl;

    private String statusMessage = null;

    private String destinationDir = null;

    public static final int STACKED_BUTTON_PAUSE = 0;

    public static final int STACKED_BUTTON_CANCEL = 1;

    public static final int STACKED_BUTTON_CONTINUE = 2;

    private NetworkManagerBle networkManagerBle;

    private Map<String , String>  args;

    private DownloadJobItemManager jobItemManager;

    public DownloadDialogPresenter(Object context, NetworkManagerBle networkManagerBle,
                                   Map<String , String>  arguments, DownloadDialogView view,
                                   UmAppDatabase appDatabase, UmAppDatabase appDatabaseRepo) {
        super(context, arguments, view);
        this.args = arguments;
        this.networkManagerBle = networkManagerBle;
        this.appDatabase = appDatabase;
        this.appDatabaseRepo = appDatabaseRepo;
    }

    @Override
    public void onCreate(Map<String , String> savedState) {
        super.onCreate(savedState);
        appDatabase = UmAppDatabase.getInstance(context);

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
            downloadDownloadJobLive = appDatabase.getDownloadJobDao().getJobLive(downloadJobUid);
            downloadDownloadJobLive.observe(DownloadDialogPresenter.this,
                    this::handleDownloadJobStatusChange);
        });
    }

    private void startObservingDownloadJobMeteredState(){
        view.runOnUiThread(() -> {
            allowedMeteredLive = appDatabase.getDownloadJobDao()
                    .getLiveMeteredNetworkAllowed((int)downloadJobUid);
            allowedMeteredLive.observe(DownloadDialogPresenter.this,
                    this::handleDownloadJobMeteredStateChange);
        });
    }

    private void handleDownloadJobMeteredStateChange(Boolean meteredConnection){
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
                int totalDownloadJobItems = appDatabase.getDownloadJobItemDao()
                        .getTotalDownloadJobItems(downloadJobUid);
                view.runOnUiThread(() -> view.setStatusText(statusMessage, totalDownloadJobItems,
                        UMFileUtil.formatFileSize(downloadJob.getTotalBytesToDownload())));
            }).start();

        }
    }

    private void setup() {
        downloadJobUid = appDatabase.getDownloadJobDao()
                .getLatestDownloadJobUidForContentEntryUid(contentEntryUid);
        if(downloadJobUid == 0) {
            createDownloadJobRecursive();
        }

        startObservingJob();

        startObservingDownloadJobMeteredState();

    }

    private void createDownloadJobRecursive() {
        jobItemManager = networkManagerBle.createNewDownloadJobItemManager(contentEntryUid);
        downloadJobUid = jobItemManager.getDownloadJobUid();
        new DownloadJobPreparer(jobItemManager, appDatabase, appDatabaseRepo).run();
    }





    public void handleClickPositive() {
        if(deleteFileOptions){
            new Thread(() -> networkManagerBle.cancelAndDeleteDownloadJob((int)downloadJobUid)).start();
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
        appDatabase.getDownloadJobDao().cleanupUnused((int)downloadJobUid);
        if(dismissAfter)
            dismissDialog();
    }

    public void handleClickNegative() {
        handleClickNegative(true);
    }

    public void handleClickStackedButton(int idClicked) {
        switch (idClicked) {
            case STACKED_BUTTON_PAUSE:
                new Thread(() -> appDatabase.getDownloadJobDao().updateJobAndItems(downloadJobUid,
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
        new Thread(() -> appDatabase.getDownloadJobDao().updateJobAndItems(downloadJobUid,
                JobStatus.QUEUED, -1)).start();
    }

    private void dismissDialog(){
        view.runOnUiThread(() -> view.dismissDialog());
    }

    private void cancelDownload(){
        new Thread(() -> appDatabase.getDownloadJobDao()
                .updateJobAndItems(downloadJobUid, JobStatus.CANCELED,
                        JobStatus.CANCELLING)).start();
    }

    public void handleWiFiOnlyOption(boolean wifiOnly){
        appDatabase.getDownloadJobDao().setMeteredConnectionAllowedByJobUid((int)downloadJobUid,
                !wifiOnly, null);
    }

    public void handleStorageOptionSelection(String selectedDir){
        appDatabase.getDownloadJobDao().updateDestinationDirectory(
                (int)downloadJobUid, selectedDir,null);
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
