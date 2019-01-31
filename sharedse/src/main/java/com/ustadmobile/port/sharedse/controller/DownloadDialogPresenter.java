package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.port.sharedse.networkmanager.CrawlTask;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * Created by mike on 3/5/18.
 */

public class DownloadDialogPresenter extends UstadBaseController<DownloadDialogView> {

    public static final String ARG_ROOT_URIS = "r_uris";

    public static final String ARG_ROOT_UUID = "r_uuids";

    private UmAppDatabase dbManager;

    private UmLiveData<OpdsEntryWithStatusCache> rootEntryLiveData;

    private String[] rootEntryUuid;

    private String rootEntryId;

    private CrawlTask task;

    private UmLiveData<OpdsEntryWithStatusCache> entryLiveData;

    private UmLiveData<CrawlJobWithTotals> crawlJobLiveData;

    private UmLiveData<DownloadJobWithTotals> downloadJobLiveData;

    CrawlJobWithTotals crawlJob;

    private int crawlJobId;

    private int downloadJobId;

    public static final int OPTION_START_DOWNLOAD = 1;

    public static final int OPTION_PAUSE_DOWNLOAD = 2;

    public static final int OPTION_CANCEL_DOWNLOAD = 4;

    public static final int OPTION_RESUME_DOWNLOAD = 8;

    public static final int OPTION_DELETE = 16;

    private int selectedOption = 0;

    private boolean wifiOnly = true;

    private int optionsAvailable;

    public DownloadDialogPresenter(Object context, DownloadDialogView view, Hashtable args) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        dbManager = UmAppDatabase.getInstance(getContext());


        if(getArguments().containsKey(ARG_ROOT_UUID)){
            rootEntryUuid = (String[])getArguments().get(ARG_ROOT_UUID);
            rootEntryLiveData = dbManager.getOpdsEntryWithRelationsDao()
                    .findWithStatusCacheByUuidLive(rootEntryUuid[0]);
        }else{
            //we need to load the entry from the given URI
            rootEntryUuid = new String[1];
            String rootEntryUri = ((String[])getArguments().get(ARG_ROOT_URIS))[0];
            rootEntryLiveData = UstadMobileSystemImpl.getInstance()
                    .getOpdsAtomFeedRepository(getContext()).getEntryWithStatusCacheByUrl(
                        rootEntryUri, null, null);
        }

        rootEntryLiveData.observe(this, this::onEntryChanged);

    }

    private void observeRootUuid() {

    }


    public void onEntryChanged(OpdsEntryWithStatusCache entry) {
        int optionsAvailable = 0;

        if(entry == null)
            return;

        rootEntryUuid[0] = entry.getUuid();
        rootEntryId = entry.getEntryId();
        OpdsEntryStatusCache status = entry.getStatusCache();

        if(status == null)
            return;//has not really loaded yet

        boolean inProgress = entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS;
        boolean paused = entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_PAUSED;

        if(!inProgress && entry.getStatusCache().getContainersDownloadedIncDescendants() > 0) {
            optionsAvailable = optionsAvailable | OPTION_DELETE;
        }

        if(!(inProgress || paused) && (status.getContainersDownloadedIncDescendants() == 0 ||
                (status.getContainersDownloadedIncDescendants() + status.getContainersDownloadPendingIncAncestors()) < status.getEntriesWithContainerIncDescendants())){
            optionsAvailable = optionsAvailable | OPTION_START_DOWNLOAD;
        }

        if(inProgress) {
            optionsAvailable = optionsAvailable | OPTION_CANCEL_DOWNLOAD;
            optionsAvailable = optionsAvailable | OPTION_PAUSE_DOWNLOAD;
        }

        if(paused) {
            optionsAvailable = optionsAvailable | OPTION_RESUME_DOWNLOAD;
            optionsAvailable = optionsAvailable | OPTION_CANCEL_DOWNLOAD;
        }

        this.optionsAvailable = optionsAvailable;
        int numOptions = Integer.bitCount(optionsAvailable);
        view.setAvailableOptions(optionsAvailable, numOptions > 1);
        if(numOptions == 1)
            handleSelectOption(optionsAvailable);
    }

    public void handleSelectOption(int option){
        selectedOption = option;

        switch(option) {
            case OPTION_START_DOWNLOAD:
                view.setProgressVisible(true);
                if(crawlJob == null)
                    startCrawlJob();

                break;

            case OPTION_DELETE:
                view.setMainText("Delete, are you sure?");
                view.setProgressVisible(false);
                break;

            case OPTION_PAUSE_DOWNLOAD:
                view.setMainText("Pause this download");
                view.setProgressVisible(false);
                break;

            case OPTION_CANCEL_DOWNLOAD:
                view.setMainText("Cancel this download");
                view.setProgressVisible(false);
                break;

        }
    }

    private void startCrawlJob(){
        crawlJob = new CrawlJobWithTotals();

        if(rootEntryUuid == null) {
            String[] rootUris = (String[])getArguments().get(ARG_ROOT_URIS);
            crawlJob.setRootEntryUri(rootUris[0]);
        }else {
            crawlJob.setRootEntryUuid(rootEntryUuid[0]);
        }

        DownloadSet downloadSet = new DownloadSet();
        UMStorageDir[] storageDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                UstadMobileSystemImpl.SHARED_RESOURCE, getContext());
        downloadSet.setDestinationDir(storageDirs[0].getDirURI());

        UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownloadAsync(downloadSet,
                crawlJob, !wifiOnly, (insertedCrawlJob) -> {
                    crawlJobId =insertedCrawlJob.getCrawlJobId();
                    downloadJobId = insertedCrawlJob.getContainersDownloadJobId();
                    crawlJobLiveData = dbManager.getCrawlJobDao().findWithTotalsByIdLive(insertedCrawlJob.getCrawlJobId());
                    downloadJobLiveData = dbManager.getDownloadJobDao().findByIdWithTotals(
                            insertedCrawlJob.getContainersDownloadJobId());

                    crawlJobLiveData.observe(DownloadDialogPresenter.this,
                            DownloadDialogPresenter.this::handleCrawlJobChanged);
                    downloadJobLiveData.observe(DownloadDialogPresenter.this,
                            DownloadDialogPresenter.this::handleDownloadJobChanged);
                });
    }


    public void handleCrawlJobChanged(CrawlJobWithTotals crawlJob){
        float progress = (float) crawlJob.getNumItemsCompleted() / crawlJob.getNumItems();
        view.setProgress(progress);
        view.setProgressStatusText("Indexed " + crawlJob.getNumItemsCompleted() + "/" +
            crawlJob.getNumItems());
        if(crawlJob.getNumItemsCompleted() == crawlJob.getNumItems()) {
            view.setProgressVisible(false);
        }
    }

    public void handleDownloadJobChanged(DownloadJobWithTotals downloadJob) {
        view.setMainText(downloadJob.getNumJobItems() + " items, total " +
                UMFileUtil.formatFileSize(downloadJob.getTotalDownloadSize()));
    }


    public void handleClickConfirm() {
        switch(selectedOption) {
            case OPTION_START_DOWNLOAD:
                dbManager.getCrawlJobDao().updateQueueDownloadOnDoneIfNotFinished(crawlJobId, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer queueOnComplete) {
                        if(queueOnComplete == 0){
                            //the preparation is already done - so we need to queue this ourselves.
                            UstadMobileSystemImpl.getInstance().getNetworkManager()
                                    .queueDownloadJob(downloadJobId);
                        }

                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
                break;

            case OPTION_DELETE:
                UstadMobileSystemImpl.getInstance().deleteEntriesAsync(getContext(),
                        Arrays.asList(rootEntryId), true,
                        new UmCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {

                            }

                            @Override
                            public void onFailure(Throwable exception) {

                            }
                        });
            case OPTION_PAUSE_DOWNLOAD:
                dbManager.getDownloadJobDao().findLastDownloadJobId(rootEntryId, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer runningDownloadJobId) {
                        if(runningDownloadJobId > 0){
                            UstadMobileSystemImpl.getInstance().getNetworkManager().pauseDownloadJobAsync(
                                    runningDownloadJobId, (pausedOK) -> {
                                        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Paused download: "
                                                + runningDownloadJobId);
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
                break;

            case OPTION_RESUME_DOWNLOAD:
                dbManager.getDownloadJobDao().findLastDownloadJobId(rootEntryId, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer runningDownloadJobId) {
                        if(runningDownloadJobId > 0){
                            UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(runningDownloadJobId);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
                break;

            case OPTION_CANCEL_DOWNLOAD:
                dbManager.getDownloadJobDao().findLastDownloadJobId(rootEntryId, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer runningDownloadJobId) {
                        if(runningDownloadJobId > 0){
                            UstadMobileSystemImpl.getInstance().getNetworkManager().cancelDownloadJob(
                                    runningDownloadJobId);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
                break;

        }

    }

    public void handleSetWifiOnly(boolean wifiOnly) {
        this.wifiOnly = wifiOnly;
        if(crawlJob != null) {
            dbManager.getDownloadJobDao().updateAllowMeteredDataUsage(
                    crawlJob.getContainersDownloadJobId(), !wifiOnly, null);
        }else if((optionsAvailable
                & (OPTION_PAUSE_DOWNLOAD | OPTION_RESUME_DOWNLOAD | OPTION_CANCEL_DOWNLOAD)) > 0) {
            dbManager.getDownloadJobDao().findLastDownloadJobId(rootEntryId, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer jobId) {
                    dbManager.getDownloadJobDao().updateAllowMeteredDataUsage(jobId, !wifiOnly, null);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }
    }


    public void handleClickCancel() {

    }
}
