package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_CONTENT_ENTRY_UID = "contentEntryUid";

    private boolean deleteFileOptions = false;

    private UmAppDatabase umAppDatabase;

    private long contentEntryUid = 0L;

    private UmLiveData<DownloadJob> downloadDownloadJobLive;

    private UmLiveData<Boolean> allowedMeteredLive;

    private long downloadSetUid;

    private long downloadJobUid = 0L;

    private UstadMobileSystemImpl impl;

    private String statusMessage = null;

    public DownloadDialogPresenter(Object context, Hashtable arguments, DownloadDialogView view) {
        super(context, arguments, view);

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        umAppDatabase = UmAppDatabase.getInstance(context);
        impl = UstadMobileSystemImpl.getInstance();
        contentEntryUid = Long.parseLong(String.valueOf(getArguments()
                .get(ARG_CONTENT_ENTRY_UID)));
        new Thread(this::setup).start();
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

    private void handleDownloadSetMeteredStateChange(boolean meteredConnection){
        view.setDownloadOverWifiOnly(!meteredConnection);
    }

    private void handleDownloadJobStatusChange(DownloadJob downloadJob){
        if(downloadJob != null){
            int downloadStatus = downloadJob.getDjStatus();
            view.setCalculatingViewVisible(false);
            if(downloadStatus >= JobStatus.COMPLETE_MIN
                    && downloadStatus <= JobStatus.COMPLETE_MAX){
                deleteFileOptions = true;
                view.setStackOptionsVisible(false);
                view.setBottomButtonsVisible(true);
                statusMessage = impl.getString(MessageID.download_state_downloaded,
                        getContext());
                view.setBottomButtonPositiveText(impl.getString(
                        MessageID.download_delete_btn_label,getContext()));
                view.setBottomButtonNegativeText(impl.getString(
                        MessageID.download_cancel_label,getContext()));
            }else if(downloadStatus >= JobStatus.RUNNING_MIN
                    && downloadStatus <= JobStatus.RUNNING_MAX){
                view.setStackOptionsVisible(true);
                view.setBottomButtonsVisible(false);
                String [] optionTexts = new String[]{
                        impl.getString(MessageID.download_pause_stacked_label,getContext()),
                        impl.getString(MessageID.download_cancel_stacked_label,getContext()),
                        impl.getString(MessageID.download_continue_stacked_label,getContext())
                };
                statusMessage = impl.getString(MessageID.download_state_downloading,
                        getContext());
                view.setStackedOptions(view.getOptionIds(), optionTexts);
            }else{
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
                DownloadJobItemDao.DownloadJobInfo jobInfo = umAppDatabase.getDownloadJobItemDao()
                        .getDownloadJobInfoByJobUid(downloadJobUid);
                view.runOnUiThread(() -> view.setStatusText(statusMessage,
                        jobInfo.getTotalDownloadItems(),
                        UMFileUtil.formatFileSize(jobInfo.getTotalSize())));
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
        UMStorageDir[] storageDir = UstadMobileSystemImpl.getInstance()
                .getStorageDirs(UstadMobileSystemImpl.SHARED_RESOURCE, getContext());
        downloadSet.setDestinationDir(storageDir[0].getDirURI());
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
        downloadJob.setDjDsUid(downloadSetUid);

        long downloadJobId = umAppDatabase.getDownloadJobDao().insert(downloadJob);

        List<DownloadJobItemDao.DownloadJobItemToBeCreated> itemToBeCreated =
                umAppDatabase.getDownloadJobItemDao()
                        .findJobItemsToBeCreatedForDownloadSet(downloadSetUid);

        List<DownloadJobItem> jobItems = new ArrayList<>();

        for(DownloadJobItemDao.DownloadJobItemToBeCreated item: itemToBeCreated){
            DownloadJobItem jobItem = new DownloadJobItem();
            jobItem.setDjiContentEntryFileUid(item.getContentEntryFileUid());
            jobItem.setDjiDjUid(downloadJobId);
            jobItem.setDownloadLength(item.getFileSize());
            jobItem.setDjiDsiUid(item.getDownloadSetItemUid());
            jobItems.add(jobItem);
        }


        umAppDatabase.getDownloadJobItemDao().insert(jobItems);
    }


    public void handleDismissDialog(){
        view.runOnUiThread(() -> view.cancelDialog());
    }

    public void handleContinueDownloading(){
        new Thread(() -> umAppDatabase.getDownloadJobDao()
                .update(downloadSetUid,JobStatus.QUEUED)).start();
    }

    public void handleCancelDownload(){
        new Thread(() -> umAppDatabase.getDownloadJobDao()
                .update(downloadSetUid,JobStatus.CANCELED)).start();
    }

    public void handlePauseDownload(){
        new Thread(() -> umAppDatabase.getDownloadJobDao()
                .update(downloadSetUid,JobStatus.PAUSED)).start();
    }

    public void handleDeleteDownloadFile(){
        new Thread(() -> {
            List<DownloadJobItem> downloadSetItemList = umAppDatabase.getDownloadJobItemDao()
                            .findByJobUid(downloadJobUid);

            if(umAppDatabase.getDownloadSetDao().deleteByUid(downloadSetUid) != 0
                && umAppDatabase.getDownloadSetItemDao().deleteByDownloadSetUid(downloadSetUid) != 0
                && umAppDatabase.getDownloadJobDao().deleteByDownloadSetUid(downloadSetUid) != 0
                && umAppDatabase.getDownloadJobItemDao().deleteByDownloadSetUid(downloadSetUid) != 0){
                for(DownloadJobItem item : downloadSetItemList){
                    File file = new File(item.getDestinationFile());
                    if(file.exists()){
                        file.delete();
                    }
                }
            }



        }).start();
    }

    public boolean isDeleteFileOptions(){
        return deleteFileOptions;
    }

    public void handleWiFiOnlyOption(boolean wifiOnly){
        new Thread(() -> umAppDatabase.getDownloadSetDao()
                .setMeteredConnectionBySetUid(downloadSetUid,!wifiOnly)).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
