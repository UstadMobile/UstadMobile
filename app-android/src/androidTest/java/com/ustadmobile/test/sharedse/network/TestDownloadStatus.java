package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryDownloadStatus;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;
import com.ustadmobile.test.core.annotation.UmSmallTest;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 3/13/18.
 */

@UmSmallTest
public class TestDownloadStatus {

    static final int ENTRY_SIZE = 1000;

    static final int NUM_ENTRIES_IN_SUBSECTION = 5;

    /**
     * If all entries have been requested, then
     */
    @Test
    public void testAcquisitionStatus_inProgress() {
        DbManager dbManager = DbManager.getInstance(PlatformTestUtil.getTargetContext());

        List<OpdsEntry> entryList = new ArrayList<>();
        List<OpdsLink> linkList = new ArrayList<>();
        List<DownloadJobItem> jobItemList = new ArrayList<>();
        List<OpdsEntryParentToChildJoin> parentToChildJoins = new ArrayList<>();
        DownloadJob downloadJob = new DownloadJob(System.currentTimeMillis());
        downloadJob.setStatus(NetworkTask.STATUS_RUNNING);
        downloadJob.setId((int)dbManager.getDownloadJobDao().insert(downloadJob));


        OpdsEntry subsectionParent = new OpdsEntry(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                UUID.randomUUID().toString(),"subsection parent");
        entryList.add(subsectionParent);
        for(int i = 0; i < NUM_ENTRIES_IN_SUBSECTION; i++) {
            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), UUID.randomUUID().toString(),
                            "Test Entry " + i);
            OpdsLink newEntryLink = new OpdsLink(newEntry.getUuid(), "application/zip+epub",
                    "/some/path"+ i + ".epub", OpdsEntry.LINK_REL_ACQUIRE);
            OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(subsectionParent.getUuid(),
                    newEntry.getUuid(), i);
            DownloadJobItem downloadJobItem = new DownloadJobItem(newEntry, downloadJob);
            downloadJobItem.setDownloadLength(ENTRY_SIZE);
            newEntryLink.setLength(ENTRY_SIZE);

            entryList.add(newEntry);
            linkList.add(newEntryLink);
            parentToChildJoins.add(join);
            jobItemList.add(downloadJobItem);
            downloadJobItem.setId((int)dbManager.getDownloadJobItemDao().insert(downloadJobItem));
        }

        dbManager.getOpdsEntryDao().insertList(entryList);
        dbManager.getOpdsEntryParentToChildJoinDao().insertAll(parentToChildJoins);
        dbManager.getOpdsLinkDao().insert(linkList);

        OpdsEntryDownloadStatus status = dbManager.getOpdsEntryWithRelationsDao()
                .getEntryDownloadStatus(subsectionParent.getEntryId());
        Assert.assertEquals("Total size matches", ENTRY_SIZE * NUM_ENTRIES_IN_SUBSECTION,
                status.getTotalSize());
        Assert.assertEquals("Amount downloaded is 0", 0, status.getTotalBytesDownloaded());
        Assert.assertEquals("Number of entries with container is 5", NUM_ENTRIES_IN_SUBSECTION,
                status.getEntriesWithContainer());



        //now set progress of the first entry to 50%, test value
        dbManager.getDownloadJobItemDao().updateDownloadJobItemStatus(jobItemList.get(0).getId(),
                NetworkTask.STATUS_RUNNING, (int)(0.5 * ENTRY_SIZE), ENTRY_SIZE, 5000);
        status = dbManager.getOpdsEntryWithRelationsDao().getEntryDownloadStatus(
                subsectionParent.getEntryId());
        Assert.assertEquals("Partial download is reported", (int)(0.5* ENTRY_SIZE),
                status.getTotalBytesDownloaded());



        //now set the first two entries as having been downloaded



    }




}
