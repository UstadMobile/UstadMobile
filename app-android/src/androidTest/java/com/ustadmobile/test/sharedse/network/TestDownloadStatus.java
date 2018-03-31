package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.networkmanager.NetworkTask;
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
public class TestDownloadStatus {

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
    public void testAcquisitionStatus() {
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        System.out.println(OpdsEntryWithRelationsDao.GET_DOWNLOAD_STATUS_SQL);
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
        dbManager.getOpdsEntryStatusCacheDao().handleOpdsEntriesLoaded(dbManager, entryWithRelationsList,
                null);


        OpdsEntryStatusCache status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("Subsection total size includes all child entries",
                ENTRY_SIZE_LINK_LENGTH * NUM_ENTRIES_IN_SUBSECTION, status.getTotalSize());

        //now mark a download as in progress
        dbManager.getDownloadJobItemDao().insertList(jobItemList);
        dbManager.getDownloadJobDao().updateJobStatus(downloadJob.getId(), NetworkTask.STATUS_RUNNING);


        for(DownloadJobItem jobItem : dbManager.getDownloadJobItemDao().findAllByDownloadJob(downloadJob.getId())) {
            dbManager.getOpdsEntryStatusCacheDao().handleDownloadJobQueued(jobItem.getId());
        }

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("After queueing download job items, total size should be readjusted",
                ENTRY_SIZE_DOWNLOAD_LENGTH * NUM_ENTRIES_IN_SUBSECTION, status.getTotalSize());


        //now mark progress on a download
        dbManager.getOpdsEntryStatusCacheDao().updateSumActiveBytesDownloadedSoFarByEntryId(jobItemList.get(0).getEntryId(),
                500);

        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("When download progress is logged on a child entry, that is reflected on the parent",
                500, status.getSumActiveDownloadsBytesSoFar());

        //now mark one entry as complete, with a downloaded container
        dbManager.getOpdsEntryStatusCacheDao().updateOnContainerAcquired(jobItemList.get(0).getEntryId(),
                -500, -1,
                ENTRY_SIZE_CONTAINER_LENGTH, 1,
                (ENTRY_SIZE_CONTAINER_LENGTH - ENTRY_SIZE_DOWNLOAD_LENGTH));
        status = dbManager.getOpdsEntryStatusCacheDao().findByEntryId(subsectionParent.getEntryId());
        Assert.assertEquals("Downloaded bytes is updated after download finishes",
                ENTRY_SIZE_CONTAINER_LENGTH, status.getSumContainersDownloadedSize());
        Assert.assertEquals("After download finishes, bytes in progress is 0, ", 0,
                status.getSumActiveDownloadsBytesSoFar());
        Assert.assertEquals("After download item finishes, bytes downloaded equals 1 container length",
                ENTRY_SIZE_CONTAINER_LENGTH, status.getSumContainersDownloadedSize());

        Assert.assertEquals("After one download item finishes, num items pending is one fewer",
                NUM_ENTRIES_IN_SUBSECTION - 1  , status.getContainersDownloadPending());

    }


}
