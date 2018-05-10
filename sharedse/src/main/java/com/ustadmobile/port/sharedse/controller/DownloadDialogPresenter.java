package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
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

    private DbManager dbManager;

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

    public DownloadDialogPresenter(Object context, DownloadDialogView view, Hashtable args) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        dbManager = DbManager.getInstance(getContext());


        if(getArguments().containsKey(ARG_ROOT_UUID)){
            rootEntryUuid = (String[])getArguments().get(ARG_ROOT_UUID);
            rootEntryLiveData = dbManager.getOpdsEntryWithRelationsDao()
                    .findWithStatusCacheByUuidLive(rootEntryUuid[0]);
        }else{
            //we need to load the entry from the given URI
            rootEntryUuid = new String[1];
            String rootEntryUri = ((String[])getArguments().get(ARG_ROOT_URIS))[0];
            rootEntryLiveData = dbManager.getOpdsAtomFeedRepository().getEntryWithStatusCacheByUrl(
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

        boolean canPause = entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_PAUSED;
        boolean canCancel = canPause || entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS;


        if(!canCancel && entry.getStatusCache().getContainersDownloadedIncDescendants() > 0) {
            optionsAvailable = optionsAvailable | OPTION_DELETE;
        }

        if(status.getContainersDownloadedIncDescendants() == 0 ||
                (status.getContainersDownloadedIncDescendants() + status.getContainersDownloadPendingIncAncestors()) < status.getEntriesWithContainerIncDescendants()){
            optionsAvailable = optionsAvailable | OPTION_START_DOWNLOAD;
        }

        if(canCancel) {
            optionsAvailable = optionsAvailable | OPTION_CANCEL_DOWNLOAD;
        }

        if(entry.getDownloadDisplayState() == OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_PAUSED) {
            optionsAvailable = optionsAvailable | OPTION_RESUME_DOWNLOAD;
        }

        if(canPause) {
            optionsAvailable = optionsAvailable | OPTION_PAUSE_DOWNLOAD;
        }

        int numOptions = Integer.bitCount(optionsAvailable);
        view.setAvailableOptions(optionsAvailable, numOptions > 1);
        if(numOptions == 1)
            handleSelectOption(optionsAvailable);
    }

    public void handleSelectOption(int option){
        selectedOption = option;

        switch(option) {
            case OPTION_START_DOWNLOAD:
                if(crawlJob == null)
                    startCrawlJob();

                break;

            case OPTION_DELETE:
                view.setMainText("Delete, are you sure?");
                view.setProgressVisible(false);
                break;

        }
    }

    private void startCrawlJob(){
        crawlJob = new CrawlJobWithTotals();
        String[] rootUris = (String[])getArguments().get(ARG_ROOT_URIS);
        crawlJob.setRootEntryUri(rootUris[0]);
        DownloadSet downloadSet = new DownloadSet();
        UMStorageDir[] storageDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, getContext());
        downloadSet.setDestinationDir(storageDirs[0].getDirURI());

        UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownloadAsync(downloadSet,
                crawlJob, (insertedCrawlJob) -> {
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


    @Override
    public void setUIStrings() {

    }

    public void handleClickConfirm() {
        switch(selectedOption) {
            case OPTION_START_DOWNLOAD:
                dbManager.getCrawlJobDao().updateQueueDownloadOnDoneIfNotFinished(crawlJobId, (queueOnComplete) -> {
                    if(queueOnComplete == 0){
                        //the preparation is already done - so we need to queue this ourselves.
                        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);
                    }
                });
                break;

            case OPTION_DELETE:
                UstadMobileSystemImpl.getInstance().deleteEntries(getContext(),
                        Arrays.asList(rootEntryId), true,
                        new UmCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {

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
