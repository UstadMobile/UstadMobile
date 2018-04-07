package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.CrawlJobItem;
import com.ustadmobile.lib.db.entities.CrawlJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by mike on 3/6/18.
 */

public class CrawlTask extends NetworkTask {

    private Vector<DownloadCrawlWorker> crawlWorkers = new Vector();

    private DbManager dbManager;

    private NetworkManager networkManager;

    private CrawlJob crawlJob;

    public static final int CRAWL_NUM_THREADS = 4;


    public CrawlTask(CrawlJob crawlJob, DbManager dbManager, NetworkManager networkManager) {
        super(networkManager);
        this.dbManager = dbManager;
        this.networkManager = networkManager;
        this.crawlJob = crawlJob;
    }

    @Override
    public void start() {
        dbManager.getCrawlJobDao().setStatusById(crawlJob.getCrawlJobId(), STATUS_RUNNING);
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

        public void run() {
            final DbManager dbManager = DbManager.getInstance(networkManager.getContext());
            CrawlJobItem item;
            while((item = dbManager.getDownloadJobCrawlItemDao().findNextItemAndUpdateStatus(crawlJob.getCrawlJobId(), STATUS_RUNNING))
                    != null) {
                OpdsEntryWithRelations itemEntry;
                if(item.getUri().startsWith("http://") || item.getUri().startsWith("https://")){
                    List<OpdsEntryWithRelations> allItems = new ArrayList<>();
                    List<DownloadJobItem> downloadJobItems = new ArrayList<>();
                    itemEntry = dbManager.getOpdsAtomFeedRepository().getEntryByUrlStatic(
                            item.getUri());
                    if(itemEntry.getEntryType() == OpdsEntry.ENTRY_TYPE_OPDS_ENTRY_STANDALONE) {
                        allItems.add(itemEntry);
                    }else if(itemEntry.getEntryType() == OpdsEntry.ENTRY_TYPE_OPDS_FEED) {
                        allItems.addAll(dbManager.getOpdsEntryWithRelationsDao()
                                .getEntriesByParentAsListStatic(itemEntry.getUuid()));
                    }

                    for(OpdsEntryWithRelations entry : allItems) {
                        OpdsLink opdsLink = entry.getAcquisitionLink(null, false);
                        if(crawlJob.getContainersDownloadJobId() != -1 && opdsLink != null) {
                            //add this as an item that needs to be downloaded - downloadjobitem
                            DownloadJobItem jobItem = new DownloadJobItem(entry,
                                    crawlJob.getContainersDownloadJobId());
                            if(opdsLink.getLength() > 0){
                                jobItem.setDownloadLength(opdsLink.getLength());
                            }

                            downloadJobItems.add(jobItem);
                        }

                        List<OpdsLink> subsectionLinks = entry.getLinks(OpdsEntry.LINK_REL_SUBSECTION,
                                null, null, false, false, false, 0);

                        for(OpdsLink subLink : subsectionLinks) {
                            String linkPath = UMFileUtil.resolveLink(item.getUri(), subLink.getHref());
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
            }

            //no work left - remove from the crawlworkers
            synchronized (crawlWorkers) {
                crawlWorkers.remove(this);
                if(crawlWorkers.isEmpty()){
                    //The task itself is done
                    dbManager.getCrawlJobDao().setStatusById(crawlJob.getCrawlJobId(),
                            NetworkTask.STATUS_COMPLETE);
                }
            }
        }

    }

}
