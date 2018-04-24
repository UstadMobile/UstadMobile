package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.port.sharedse.view.StartDownloadView;
import com.ustadmobile.port.sharedse.networkmanager.CrawlTask;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * Created by mike on 3/5/18.
 */

public class StartDownloadPresenter extends UstadBaseController<StartDownloadView> {

    public static final String ARG_ROOT_URIS = "r_uris";

    private DbManager dbManager;

    private CrawlTask task;

    private UmLiveData<CrawlJobWithTotals> crawlJobLiveData;

    private UmLiveData<DownloadJobWithTotals> downloadJobLiveData;

    private int crawlJobId;

    private int downloadJobId;

    public StartDownloadPresenter(Object context, StartDownloadView view, Hashtable args) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        CrawlJobWithTotals crawlJob = new CrawlJobWithTotals();
        DownloadSet downloadSet = new DownloadSet();
        UMStorageDir[] storageDirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogPresenter.SHARED_RESOURCE, getContext());
        downloadSet.setDestinationDir(storageDirs[0].getDirURI());
        
        dbManager = DbManager.getInstance(getContext());

        String[] rootUris = (String[])getArguments().get(ARG_ROOT_URIS);
        crawlJob.setRootEntryUri(rootUris[0]);

        UstadMobileSystemImpl.getInstance().getNetworkManager().prepareDownloadAsync(downloadSet,
                crawlJob, (insertedCrawlJob) -> {
                    crawlJobId =insertedCrawlJob.getCrawlJobId();
                    downloadJobId = insertedCrawlJob.getContainersDownloadJobId();
                    crawlJobLiveData = dbManager.getCrawlJobDao().findWithTotalsByIdLive(insertedCrawlJob.getCrawlJobId());
                    downloadJobLiveData = dbManager.getDownloadJobDao().findByIdWithTotals(
                            insertedCrawlJob.getContainersDownloadJobId());

                    crawlJobLiveData.observe(StartDownloadPresenter.this,
                            StartDownloadPresenter.this::handleCrawlJobChanged);
                    downloadJobLiveData.observe(StartDownloadPresenter.this,
                            StartDownloadPresenter.this::handleDownloadJobChanged);
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
        view.setDownloadText(downloadJob.getNumJobItems() + " items, total " +
                UMFileUtil.formatFileSize(downloadJob.getTotalDownloadSize()));
    }


    @Override
    public void setUIStrings() {

    }

    public void handleClickDownload() {
        dbManager.getCrawlJobDao().updateQueueDownloadOnDoneIfNotFinished(crawlJobId, (queueOnComplete) -> {
            if(queueOnComplete == 0){
                //the preparation is already done - so we need to queue this ourselves.
                UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(downloadJobId);
            }
        });

    }


    public void handleClickCancel() {

    }
}
