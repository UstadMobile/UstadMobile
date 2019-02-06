package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
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

    private UmObserver<DownloadJob> downloadJobObserver;

    private UmLiveData<DownloadJob> downloadDownloadJobLive;

    private long downloadSetUid;

    private long downloadJobUid = 0L;

    private UstadMobileSystemImpl impl;

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
        new Thread(this::setup).run();
    }

    private void startObservingJob(){
        downloadDownloadJobLive = umAppDatabase.getDownloadJobDao().getLiveJobBySetUid(downloadSetUid);
        downloadJobObserver = this::handleDownloadJobStatusChange;
        downloadDownloadJobLive.observeForever(downloadJobObserver);
    }

    private  void handleDownloadJobStatusChange(DownloadJob downloadJob){
        if(downloadJob != null){

            downloadSetUid = downloadJob.getDjDsUid();

            downloadJobUid = downloadJob.getDjUid();

            view.runOnUiThread(() -> {
                int downloadStatus = downloadJob.getDjStatus();

                if(downloadStatus >= JobStatus.COMPLETE_MIN
                        && downloadStatus <= JobStatus.COMPLETE_MAX){
                    deleteFileOptions = true;
                    view.setStackOptionsVisible(false);
                    view.setBottomButtonsVisible(true);
                    view.setBottomButtonPositiveText(impl.getString(
                            MessageID.download_delete_btn_label,getContext()));
                    view.setBottomButtonPositiveText(impl.getString(
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
                    view.runOnUiThread(() -> view.setStackedOptions(view.getOptionIds(),
                            optionTexts));
                }else{

                    view.setStackOptionsVisible(false);
                    view.setBottomButtonsVisible(true);
                    view.setBottomButtonPositiveText(impl.getString(
                            MessageID.download_continue_btn_label,getContext()));
                    view.setBottomButtonPositiveText(impl.getString(
                            MessageID.download_cancel_label,getContext()));
                }
            });
        }
    }

    private void setup() {
        //check for an existing downloadset for this item
        downloadSetUid = umAppDatabase.getDownloadSetDao()
                .findDownloadSetUidByRootContentEntryUid(contentEntryUid);

        //see if there is any existing download setitem for this entryuid
        if(downloadSetUid == 0)
            downloadSetUid = umAppDatabase.getDownloadSetItemDao()
                    .findDownloadSetUidByContentEntryUid(contentEntryUid);

        if(downloadSetUid == 0)
            createDownloadSet();
        else
            startObservingJob();

    }

    private void createDownloadSet() {
        DownloadSet downloadSet = new DownloadSet();
        UMStorageDir[] storageDir = UstadMobileSystemImpl.getInstance()
                .getStorageDirs(UstadMobileSystemImpl.SHARED_RESOURCE, getContext());
        downloadSet.setDestinationDir(storageDir[0].getDirURI());
        downloadSet.setDsRootContentEntryUid(contentEntryUid);

        downloadSetUid = umAppDatabase.getDownloadSetDao().insert(downloadSet);
        List<DownloadSetItem> downloadSetItems = new ArrayList<>();

        startObservingJob();

        List<Long> currentChildUids;
        Set<Long> allChildUids = new HashSet<>();
        List<Long> parentUids = new ArrayList<>();
        parentUids.add(contentEntryUid);
        allChildUids.add(contentEntryUid);

        do {
            currentChildUids = umAppDatabase.getContentEntryParentChildJoinDao()
                    .findChildEntriesByParents(parentUids);
            parentUids.clear();
            for(long childUid : currentChildUids){
                if(!allChildUids.contains(childUid)) {
                    parentUids.add(childUid);
                    allChildUids.add(childUid);
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
        downloadJob.setDjStatus(JobStatus.QUEUED);
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
            jobItem.setDjiDsiUid(item.getDownloadSetItemUid());
            jobItems.add(jobItem);
        }

        umAppDatabase.getDownloadJobItemDao().insert(jobItems);
    }


    public void handleDismissDialog(){
        view.runOnUiThread(() -> view.cancelDialog());
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

            umAppDatabase.getDownloadJobDao().delete(downloadSetUid, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if(result != 0){
                        for(DownloadJobItem item : downloadSetItemList){
                            new File(item.getDestinationFile()).delete();
                        }
                    }
                }

                @Override
                public void onFailure(Throwable exception) {}
            });
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
        downloadDownloadJobLive.removeObserver(downloadJobObserver);
    }
}
