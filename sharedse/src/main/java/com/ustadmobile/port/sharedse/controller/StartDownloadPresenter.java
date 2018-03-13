package com.ustadmobile.port.sharedse.controller;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.CrawlJobWithTotals;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.port.sharedse.view.StartDownloadView;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.port.sharedse.networkmanager.CrawlTask;

import java.util.ArrayList;
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

    public StartDownloadPresenter(Object context, StartDownloadView view, Hashtable args) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        CrawlJobWithTotals crawlJob = new CrawlJobWithTotals();
        DownloadJobWithRelations downloadJob = new DownloadJobWithRelations(System.currentTimeMillis());
        
        dbManager = DbManager.getInstance(getContext());
        new Thread(() -> {
            downloadJob.setId((int)dbManager.getDownloadJobDao().insert(downloadJob));
            crawlJob.setContainersDownloadJobId(downloadJob.getId());
            crawlJob.setCrawlJobId((int)dbManager.getCrawlJobDao().insert(crawlJob));

            String[] rootUris = (String[])getArguments().get(ARG_ROOT_URIS);
            ArrayList<CrawlJobItem> initialJobItems = new ArrayList<>(rootUris.length);
            for(String uri : rootUris) {
                initialJobItems.add(new CrawlJobItem(crawlJob.getCrawlJobId(), uri, NetworkTask.STATUS_QUEUED,
                        0));
            }



            dbManager.getDownloadJobCrawlItemDao().insertAll(initialJobItems);
            task = new CrawlTask(crawlJob, dbManager,
                    (NetworkManager)UstadMobileSystemImpl.getInstance().getNetworkManager());
            task.start();

            crawlJobLiveData =dbManager.getCrawlJobDao().findWithTotalsByIdLive(crawlJob.getCrawlJobId());
            crawlJobLiveData.observe(StartDownloadPresenter.this,
                    StartDownloadPresenter.this::handleCrawlJobChanged);
            downloadJobLiveData = dbManager.getDownloadJobDao().findByIdWithTotals(downloadJob.getId());
            downloadJobLiveData.observe(StartDownloadPresenter.this,
                    StartDownloadPresenter.this::handleDownloadJobChanged);
        }).start();
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
        //
    }


    public void handleClickCancel() {

    }
}
