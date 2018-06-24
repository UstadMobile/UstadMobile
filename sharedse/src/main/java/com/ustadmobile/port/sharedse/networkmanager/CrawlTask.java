package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mike on 3/6/18.
 */

public class CrawlTask extends NetworkTask {

    private Vector<DownloadCrawlWorker> crawlWorkers = new Vector();

    private UmAppDatabase dbManager;

    private NetworkManager networkManager;

    private CrawlJob crawlJob;

    public static final int CRAWL_NUM_THREADS = 4;

    private AtomicInteger crawlWorkerAtomicInteger = new AtomicInteger();

    private int containerDownloadSetId;


    public CrawlTask(CrawlJob crawlJob, UmAppDatabase dbManager, NetworkManager networkManager) {
        super(networkManager);
        this.dbManager = dbManager;
        this.networkManager = networkManager;
        this.crawlJob = crawlJob;
    }

    @Override
    public void start() {
        UstadMobileSystemImpl.l(UMLog.INFO, 0, mkLogPrefix() + " starting");
        dbManager.getCrawlJobDao().setStatusById(crawlJob.getCrawlJobId(), STATUS_RUNNING);
        containerDownloadSetId = dbManager.getDownloadJobDao().findDownloadSetId(
                crawlJob.getContainersDownloadJobId());
        for(int i = 0; i < CRAWL_NUM_THREADS; i++) {
            DownloadCrawlWorker worker = new DownloadCrawlWorker();
            crawlWorkers.add(worker);
            worker.start();
        }
    }

    @Override
    public int getQueueId() {
        return 0;
    }

    @Override
    public int getTaskType() {
        return 0;
    }

    private class DownloadCrawlWorker extends Thread {

        private int crawlWorkerId;

        public DownloadCrawlWorker(){
            crawlWorkerId = crawlWorkerAtomicInteger.getAndIncrement();
        }

        public void run() {
            UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() + " run started");
            final UmAppDatabase dbManager = UmAppDatabase.getInstance(networkManager.getContext());
            final DownloadSetItemDao downloadSetItemDao = dbManager.getDownloadSetItemDao();
            CrawlJobItem item;
            while((item = dbManager.getDownloadJobCrawlItemDao().findNextItemAndUpdateStatus(crawlJob.getCrawlJobId(), STATUS_RUNNING))
                    != null) {
                OpdsEntryWithRelations itemEntry = null;

                List<OpdsEntryWithRelations> allItems = new ArrayList<>();
                List<DownloadJobItem> downloadJobItems = new ArrayList<>();
                if(item.getOpdsEntryUuid() != null) {
                    itemEntry = dbManager.getOpdsEntryWithRelationsDao().findByUuid(item.getOpdsEntryUuid());
                    allItems.add(itemEntry);
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() +
                        " discover item by opds uuid: " + itemEntry.getUuid());

                    if(crawlJob.isRecursive()){
                        List<OpdsEntryWithRelations> childEntries =dbManager.getOpdsEntryWithRelationsDao()
                                .getEntriesByParentAsListStatic(itemEntry.getUuid());
                        allItems.addAll(childEntries);
                        UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() +
                                " add child " + childEntries.size() + " item(s) from opds uuid: " +
                                itemEntry.getUuid());
                    }
                }else if(item.getUri() != null && (item.getUri().startsWith("http://") || item.getUri().startsWith("https://"))) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() + " load " +
                            item.getUri());
                    itemEntry = UstadMobileSystemImpl.getInstance()
                            .getOpdsAtomFeedRepository(networkManager.getContext())
                            .getEntryByUrlStatic(item.getUri());
                    dbManager.getDownloadJobCrawlItemDao().updateOpdsEntryUuid(item.getId(), itemEntry.getUuid());

                    if (itemEntry.getEntryType() == OpdsEntry.ENTRY_TYPE_OPDS_ENTRY_STANDALONE) {
                        UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() +
                                " add " + item.getUri() + " (single entry) uuid " + itemEntry.getUuid());
                        allItems.add(itemEntry);
                    } else if (itemEntry.getEntryType() == OpdsEntry.ENTRY_TYPE_OPDS_FEED) {
                        List<OpdsEntryWithRelations> feedEntries = dbManager.getOpdsEntryWithRelationsDao()
                                .getEntriesByParentAsListStatic(itemEntry.getUuid());
                        UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix() +
                                " add " + item.getUri() + " (feed - " + feedEntries.size() +
                                " entries) uuid " + itemEntry.getUuid());
                        allItems.addAll(feedEntries);
                    }
                }

                for(OpdsEntryWithRelations entry : allItems) {
                    OpdsLink opdsLink = entry.getAcquisitionLink(null, false);
                    if(crawlJob.getContainersDownloadJobId() != -1 && opdsLink != null) {
                        //see if there is an existing DownloadSetItem for this entry
                        DownloadSetItem downloadSetItem = dbManager.getDownloadSetItemDao()
                                .findByEntryId(entry.getEntryId(), containerDownloadSetId);
                        if(downloadSetItem == null){
                            downloadSetItem = new DownloadSetItem(entry, containerDownloadSetId);
                            downloadSetItem.setId((int)downloadSetItemDao.insert(downloadSetItem));
                        }

                        //add this as an item that needs to be downloaded - downloadjobitem
                        DownloadJobItem jobItem = new DownloadJobItem(downloadSetItem.getId(),
                                crawlJob.getContainersDownloadJobId());
                        if(opdsLink.getLength() > 0){
                            jobItem.setDownloadLength(opdsLink.getLength());
                        }else {
                            try {
                                String baseHref;
                                if(itemEntry.getUrl() != null) {
                                    baseHref= itemEntry.getUrl();
                                }else {
                                    baseHref = dbManager.getOpdsEntryWithRelationsDao()
                                            .findParentUrlByChildUuid(itemEntry.getUuid());
                                }

                                String acquisitionUrl = UMFileUtil.resolveLink(baseHref,
                                        opdsLink.getHref());
                                UmHttpRequest request = new UmHttpRequest(dbManager.getContext(),
                                        acquisitionUrl);
                                request.setMethod(UmHttpRequest.METHOD_HEAD);
                                UmHttpResponse response = UstadMobileSystemImpl.getInstance()
                                        .makeRequestSync(request);
                                if(response.isSuccessful() && response.getHeader("content-length") != null)
                                    jobItem.setDownloadLength(Long.parseLong(response.getHeader(
                                            "content-length")));

                            }catch(IOException|NumberFormatException e) {
                                UstadMobileSystemImpl.l(UMLog.ERROR, 0,
                                        "Exception attempting to find download size", e);
                            }

                        }

                        downloadJobItems.add(jobItem);
                    }

                    List<OpdsLink> subsectionLinks = entry.getLinks(OpdsEntry.LINK_REL_SUBSECTION,
                            null, null, false, false, false, 0);

                    for(OpdsLink subLink : subsectionLinks) {
                        String baseUrl = entry.getUrl() != null ? entry.getUrl() : itemEntry.getUrl();
                        String linkPath = UMFileUtil.resolveLink(baseUrl, subLink.getHref());
                        CrawlJobItem crawlItem = new CrawlJobItem(
                                item.getDownloadJobId(), linkPath, NetworkTask.STATUS_QUEUED,
                                item.getDepth() + 1);
                        dbManager.getDownloadJobCrawlItemDao().insert(crawlItem);
                        synchronized (crawlWorkers) {
                            if(crawlWorkers.size() < CRAWL_NUM_THREADS) {
                                DownloadCrawlWorker newWorker = new DownloadCrawlWorker();
                                crawlWorkers.add(newWorker);
                                newWorker.start();
                            }
                        }
                    }
                }

                dbManager.getDownloadJobItemDao().insertList(downloadJobItems);
                dbManager.getDownloadJobCrawlItemDao().updateStatus(item.getId(), STATUS_COMPLETE);

            }

            //no work left - remove from the crawlworkers
            synchronized (crawlWorkers) {
                UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix()
                        + " no more work available");
                crawlWorkers.remove(this);
                if(crawlWorkers.isEmpty()){
                    //The task itself is done
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix()
                            + " no more work and no more active workers: task is done");
                    int updateCount = dbManager.getCrawlJobDao().setStatusById(crawlJob.getCrawlJobId(),
                            NetworkTask.STATUS_COMPLETE);
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkWorkerLogPrefix() +
                        " set " + crawlJob.getCrawlJobId() + " to status STATUS_COMPLETE (Update " +
                        updateCount + ")");
                    if(dbManager.getCrawlJobDao().findQueueOnDownloadJobDoneById(crawlJob.getCrawlJobId())){
                        UstadMobileSystemImpl.l(UMLog.INFO, 0, mkWorkerLogPrefix()
                                + " queueing generated download job #" +
                                crawlJob.getContainersDownloadJobId());
                        UstadMobileSystemImpl.getInstance().getNetworkManager().queueDownloadJob(
                                crawlJob.getContainersDownloadJobId());
                    }
                }
            }
        }

        private String mkWorkerLogPrefix(){
            return mkLogPrefix() + " Worker #" + crawlWorkerId + ": ";
        }

    }

    private String mkLogPrefix(){
        return "CrawlTask ID #" + crawlJob.getCrawlJobId() + " ";
    }

}
