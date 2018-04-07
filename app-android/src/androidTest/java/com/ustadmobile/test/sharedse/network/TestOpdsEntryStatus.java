package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.annotation.UmSmallTest;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 3/13/18.
 */

@UmSmallTest
public class TestOpdsEntryStatus {

    static final int ENTRY_SIZE_LINK_LENGTH = 1000;

    static final int ENTRY_SIZE_DOWNLOAD_LENGTH = 1005;

    static final int ENTRY_SIZE_CONTAINER_LENGTH = 1010;

    static final int NUM_ENTRIES_IN_SUBSECTION = 100;

    static final int NUM_OTHER_ENTRIES = 15000;

    @Before
    public void startServer() throws IOException{
        ResourcesHttpdTestServer.startServer();
    }

    @Test
    public void testEntryStatusCacheLifecycle() {
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<OpdsEntry> entryList = new ArrayList<>();
        List<OpdsEntryWithRelations> entryWithRelationsList = new ArrayList<>();
        List<OpdsLink> linkList = new ArrayList<>();
        List<DownloadJobItem> jobItemList = new ArrayList<>();
        List<OpdsEntryParentToChildJoin> parentToChildJoins = new ArrayList<>();

        DownloadJob downloadJob = new DownloadJob(System.currentTimeMillis());
        downloadJob.setStatus(0);
        downloadJob.setId((int)dbManager.getDownloadJobDao().insert(downloadJob));


        OpdsEntryWithRelations subsectionParent = new OpdsEntryWithRelations(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                UUID.randomUUID().toString(),"subsection parent");
        entryList.add(subsectionParent);
        entryWithRelationsList.add(subsectionParent);
        for(int i = 0; i < NUM_ENTRIES_IN_SUBSECTION; i++) {
            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), UUID.randomUUID().toString(),
                            "Test Entry " + i);
            OpdsLink newEntryLink = new OpdsLink(newEntry.getUuid(), "application/zip+epub",
                    "/some/path"+ i + ".epub", OpdsEntry.LINK_REL_ACQUIRE);
            newEntryLink.setLength(ENTRY_SIZE_LINK_LENGTH);
            OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(subsectionParent.getUuid(),
                    newEntry.getUuid(), i);
            DownloadJobItem downloadJobItem = new DownloadJobItem(newEntry, downloadJob);
            downloadJobItem.setDownloadLength(ENTRY_SIZE_DOWNLOAD_LENGTH);


            entryList.add(newEntry);
            entryWithRelationsList.add(newEntry);
            newEntry.setLinks(Arrays.asList(newEntryLink));
            linkList.add(newEntryLink);
            parentToChildJoins.add(join);
            jobItemList.add(downloadJobItem);
        }


        dbManager.getOpdsEntryDao().insertList(entryList);
        dbManager.getOpdsLinkDao().insert(linkList);
        dbManager.getOpdsEntryParentToChildJoinDao().insertAll(parentToChildJoins);

//        TODO: Add the link to the entry
        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager, entryWithRelationsList);


        OpdsEntryStatusCache status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("Subsection total size includes all child entries",
                ENTRY_SIZE_LINK_LENGTH * NUM_ENTRIES_IN_SUBSECTION, status.getSizeIncDescendants());

        //now mark a download as in progress
        dbManager.getDownloadJobItemDao().insertList(jobItemList);
        dbManager.getDownloadJobDao().updateJobStatus(downloadJob.getId(), NetworkTask.STATUS_RUNNING);


        for(DownloadJobItem jobItem : dbManager.getDownloadJobItemDao().findAllByDownloadJob(downloadJob.getId())) {
            dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobQueued(jobItem.getId());
        }

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("After queueing download job items, total size should be readjusted",
                ENTRY_SIZE_DOWNLOAD_LENGTH * NUM_ENTRIES_IN_SUBSECTION, status.getSizeIncDescendants());


        //now mark progress on a download
        jobItemList.get(0).setStatus(NetworkTask.STATUS_RUNNING);
        jobItemList.get(0).setDownloadedSoFar(500);
        int jobItemId = dbManager.getDownloadJobItemDao().findDownloadJobItemByEntryIdAndStatusRange(
                jobItemList.get(0).getEntryId(), 0, NetworkTask.STATUS_COMPLETE).get(0)
                .getDownloadJobId();
        jobItemList.get(0).setId(jobItemId);
        int statusCacheUid = dbManager.getOpdsEntryStatusCacheDao().findUidByEntryId(
                jobItemList.get(0).getEntryId());
        dbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(jobItemList.get(0));


        dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobProgress(statusCacheUid,
                jobItemList.get(0).getDownloadJobId());

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("When download progress is logged on a child entry, that is reflected on the parent",
                500, status.getPendingDownloadBytesSoFarIncDescendants());

        //now mark one entry as complete, with a downloaded container
        ContainerFile containerFile = new ContainerFile();
        containerFile.setFileSize(ENTRY_SIZE_CONTAINER_LENGTH);
        dbManager.getOpdsEntryStatusCacheDao().handleContainerDownloadedOrDiscovered(statusCacheUid,
                containerFile);

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("Downloaded bytes is updated after download finishes",
                ENTRY_SIZE_CONTAINER_LENGTH, status.getContainersDownloadedSizeIncDescendants());
        Assert.assertEquals("After download finishes, bytes in progress is 0, ", 0,
                status.getPendingDownloadBytesSoFarIncDescendants());
        Assert.assertEquals("After download item finishes, bytes downloaded equals 1 container length",
                ENTRY_SIZE_CONTAINER_LENGTH, status.getContainersDownloadedSizeIncDescendants());

        Assert.assertEquals("After one download item finishes, num items pending is one fewer",
                NUM_ENTRIES_IN_SUBSECTION - 1  , status.getContainersDownloadPendingIncAncestors());

    }


    @Test
    public void testStatusFromAtomFeedRepositoryLoad() {
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());
        OpdsAtomFeedRepository repo = dbManager.getOpdsAtomFeedRepository();
        final Object loadLock = new Object();
        final Object observerLock = new Object();

        UmLiveData<OpdsEntryWithRelations> parentLiveData = repo.getEntryByUrl(
                UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                        "com/ustadmobile/test/core/acquire-multi.opds"), null,
                new OpdsEntry.OpdsItemLoadCallback() {
                    @Override
                    public void onDone(OpdsEntry item) {
                        synchronized (loadLock){
                            loadLock.notifyAll();
                        }
                    }

                    @Override
                    public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {

                    }

                    @Override
                    public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {

                    }

                    @Override
                    public void onError(OpdsEntry item, Throwable cause) {

                    }
                });

        UmObserver<OpdsEntryWithRelations> parentObserver = (parentEntry) -> {
            if(parentEntry != null) {
                synchronized (observerLock){
                    observerLock.notifyAll();
                }
            }
        };
        parentLiveData.observeForever(parentObserver);

        if(parentLiveData.getValue() == null){
            synchronized (observerLock) {
                try {observerLock.wait(2000); }
                catch(InterruptedException e) {}
            }
        }

        Assert.assertNotNull("Parent loaded", parentLiveData.getValue());

        synchronized (loadLock) {
            try { loadLock.wait(100000); }
            catch(InterruptedException e) {}
        }
        OpdsEntryStatusCache statusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(parentLiveData.getValue().getEntryId());
        Assert.assertNotNull("Parent has status cache value", statusCache);
        Assert.assertEquals("Parent has child entries", 4,
                statusCache.getEntriesWithContainerIncDescendants());
        Assert.assertEquals("Total recursive size of containers is 14700", 14700,
                statusCache.getSizeIncDescendants());
    }


}
