package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
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
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());

        List<OpdsEntry> entryList = new ArrayList<>();
        List<OpdsEntryWithRelations> entryWithRelationsList = new ArrayList<>();
        List<OpdsLink> linkList = new ArrayList<>();
        List<DownloadSetItem> setItemList = new ArrayList<>();
        List<DownloadJobItem> jobItemList = new ArrayList<>();
        List<OpdsEntryParentToChildJoin> parentToChildJoins = new ArrayList<>();

        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setId((int)dbManager.getDownloadSetDao().insert(downloadSet));
        DownloadJob downloadJob = new DownloadJob(downloadSet, System.currentTimeMillis());
        downloadJob.setStatus(0);
        downloadJob.setDownloadJobId((int)dbManager.getDownloadJobDao().insert(downloadJob));



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
            DownloadSetItem downloadSetItem = new DownloadSetItem(newEntry, downloadSet);
            downloadSetItem.setId((int)dbManager.getDownloadSetItemDao().insert(downloadSetItem));

            DownloadJobItem downloadJobItem = new DownloadJobItem(downloadSetItem.getId(),
                    downloadJob.getDownloadJobId());
            downloadJobItem.setDownloadLength(ENTRY_SIZE_DOWNLOAD_LENGTH);

            entryList.add(newEntry);
            entryWithRelationsList.add(newEntry);
            newEntry.setLinks(Arrays.asList(newEntryLink));
            linkList.add(newEntryLink);
            parentToChildJoins.add(join);
            setItemList.add(downloadSetItem);
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
        dbManager.getDownloadJobDao().updateJobStatus(downloadJob.getDownloadJobId(),
                NetworkTask.STATUS_RUNNING);


        for(DownloadJobItem jobItem : dbManager.getDownloadJobItemDao().findAllByDownloadJobId(downloadJob.getDownloadJobId())) {
            dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobQueued(jobItem.getDownloadJobItemId());
        }

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("After queueing download job items, total size should be readjusted",
                ENTRY_SIZE_DOWNLOAD_LENGTH * NUM_ENTRIES_IN_SUBSECTION, status.getSizeIncDescendants());

        //mark that the first download is now active
        int statusCacheUid = dbManager.getOpdsEntryStatusCacheDao().findUidByEntryId(
                setItemList.get(0).getEntryId());

        dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobStarted(statusCacheUid);
        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("After marking download job as started, number of active inc ancestors downloads = 1",
                1, status.getActiveDownloadsIncAncestors());

        //now mark progress on a download
        jobItemList.get(0).setStatus(NetworkTask.STATUS_RUNNING);
        jobItemList.get(0).setDownloadedSoFar(500);
        int jobItemId = dbManager.getDownloadJobItemDao().findDownloadJobItemByEntryIdAndStatusRange(
                setItemList.get(0).getEntryId(), 0, NetworkTask.STATUS_COMPLETE).get(0)
                .getDownloadJobItemId();
        jobItemList.get(0).setDownloadJobItemId(jobItemId);

        dbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(jobItemList.get(0));


        dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobProgress(statusCacheUid,
                jobItemList.get(0).getDownloadJobItemId());

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
        Assert.assertEquals("After download finishes, number of active downloads is 0", 0,
                status.getActiveDownloadsIncAncestors());
        Assert.assertEquals("After download item finishes, bytes downloaded equals 1 container length",
                ENTRY_SIZE_CONTAINER_LENGTH, status.getContainersDownloadedSizeIncDescendants());

        Assert.assertEquals("After one download item finishes, num items pending is one fewer",
                NUM_ENTRIES_IN_SUBSECTION - 1  , status.getContainersDownloadPendingIncAncestors());

        //For the second entry - set it as in progress downloading, and then test what happens if the download is aborted
        int jobItemId2 = dbManager.getDownloadJobItemDao().findDownloadJobItemByEntryIdAndStatusRange(
                setItemList.get(1).getEntryId(), 0, NetworkTask.STATUS_COMPLETE).get(0)
                .getDownloadJobItemId();
        int statusCacheUid2 = dbManager.getOpdsEntryStatusCacheDao().findUidByEntryId(
                setItemList.get(1).getEntryId());
        jobItemList.get(1).setDownloadJobItemId(jobItemId2);

        jobItemList.get(1).setStatus(NetworkTask.STATUS_RUNNING);
        jobItemList.get(1).setDownloadedSoFar(500);
        dbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(jobItemList.get(1));
        dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobProgress(statusCacheUid2, jobItemId2);

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("On setting second item to be in progress, bytes in progress = 500",
                500, status.getPendingDownloadBytesSoFarIncDescendants());

        OpdsEntryStatusCache abortedEntryStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(setItemList.get(1).getEntryId());
        //mark the download as aborted
        dbManager.getOpdsEntryStatusCacheDao().handleContainerDownloadAborted(abortedEntryStatusCache);
        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("After download aborted, pending download bytes = 0",
                0, status.getPendingDownloadBytesSoFarIncDescendants());
        Assert.assertEquals("After download aborted, number of containers download pending = 98",
                NUM_ENTRIES_IN_SUBSECTION - 2, status.getContainersDownloadPendingIncAncestors());
    }


    @Test
    public void testStatusFromAtomFeedRepositoryLoad() {
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        OpdsAtomFeedRepository repo = UstadMobileSystemImpl.getInstance()
                .getOpdsAtomFeedRepository(PlatformTestUtil.getTargetContext());
        final Object loadLock = new Object();
        final Object observerLock = new Object();
        final boolean[] entryLoaded = new boolean[1];

        UmLiveData<OpdsEntryWithRelations> parentLiveData = repo.getEntryByUrl(
                UMFileUtil.joinPaths(ResourcesHttpdTestServer.getHttpRoot(),
                        "com/ustadmobile/test/core/acquire-multi.opds"), null,
                new OpdsEntry.OpdsItemLoadCallback() {
                    @Override
                    public void onDone(OpdsEntry item) {
                        synchronized (loadLock){
                            entryLoaded[0] = true;
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

        if(!entryLoaded[0]) {
            synchronized (loadLock) {
                try { loadLock.wait(100000); }
                catch(InterruptedException e) {}
            }
        }

        OpdsEntryStatusCache statusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(parentLiveData.getValue().getEntryId());
        Assert.assertNotNull("Parent has status cache value", statusCache);
        Assert.assertEquals("Parent has child entries", 4,
                statusCache.getEntriesWithContainerIncDescendants());
        Assert.assertEquals("Total recursive size of containers is 14700", 14700,
                statusCache.getSizeIncDescendants());
    }

    @Test
    public void testEntryStatus_childDiscoveredBeforeParent(){
        String childEntryId = UUID.randomUUID().toString() + "-child";
        String parentEntryId = UUID.randomUUID().toString() + "-parent";

        OpdsEntryWithRelations childEntry = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                childEntryId, "Child 1");
        OpdsLink childAcquisitionLink = new OpdsLink(childEntry.getUuid(), "application/epub+zip",
                "file.epub", OpdsEntry.LINK_REL_ACQUIRE);
        childAcquisitionLink.setLength(ENTRY_SIZE_LINK_LENGTH);
        childEntry.setLinks(Arrays.asList(childAcquisitionLink));
        UmAppDatabase dbManager = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        dbManager.getOpdsEntryDao().insert(childEntry);
        dbManager.getOpdsLinkDao().insert(Arrays.asList(childAcquisitionLink));
        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager, Arrays.asList(childEntry));

        OpdsEntryStatusCache entryStatusCache = dbManager.getOpdsEntryStatusCacheDao()
                .findByEntryId(childEntryId);
        Assert.assertNotNull("entry status cache created for child entry", entryStatusCache);
        Assert.assertEquals("Entry status cache for entry size is as per link", ENTRY_SIZE_LINK_LENGTH,
                entryStatusCache.getEntrySize());


        //nwo create the parent, discover it, and make sure that it includes the size of the child
        OpdsEntryWithRelations parentEntry = new OpdsEntryWithRelations(UmUuidUtil.encodeUuidWithAscii85(
                UUID.randomUUID()), parentEntryId, "Parent 1");
        dbManager.getOpdsEntryDao().insert(parentEntry);
        OpdsEntryParentToChildJoin parentChildJoin = new OpdsEntryParentToChildJoin(parentEntry.getUuid(),
                childEntry.getUuid(), 0);
        dbManager.getOpdsEntryParentToChildJoinDao().insert(parentChildJoin);

        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager, Arrays.asList(parentEntry));

        OpdsEntryStatusCache parentStatusCache = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(parentEntryId);
        Assert.assertEquals("Entry size of parent inc descendants includes child entry", ENTRY_SIZE_LINK_LENGTH,
                parentStatusCache.getSizeIncDescendants());
    }


}
