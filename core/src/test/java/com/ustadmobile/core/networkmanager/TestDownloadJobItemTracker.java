package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestDownloadJobItemTracker {

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private ContentEntry parentEntry;

    private ContentEntry subLeaf;

    private ContentEntry subCat;

    private ContentEntry subcatLeaf1;

    private ContentEntry subcatLeaf2;

    private DownloadSetItem parentEntryDownloadSetItem;

    private DownloadSetItem subLeafDownloadSetItem;

    private DownloadSetItem subCatDownloadSetItem;

    private DownloadSetItem subcatLeaf1DownloadSetItem;

    private DownloadSetItem subcatLeaf2DownloadSetItem;

    private Container subLeafContainer;

    private Container subcatLeaf1Container;

    private Container subcatLeaf2Container;

    private DownloadSet downloadSet;

    private DownloadJob downloadJob;

    @Before
    public void setup() {
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        ContentEntryDao entryDao = repo.getContentEntryDao();
        parentEntry = new ContentEntry("Parent entry", "parent", false, true);
        parentEntry.setContentEntryUid(entryDao.insert(parentEntry));

        subLeaf = new ContentEntry("Sub Leaf", "subleaf", true, true);
        subLeaf.setContentEntryUid(entryDao.insert(subLeaf));

        subCat = new ContentEntry("Sub cat", "sub cat", false, true);
        subCat.setContentEntryUid(entryDao.insert(subCat));

        subcatLeaf1 = new ContentEntry("SubCat Leaf1", "subcatleaf1", true, true);
        subcatLeaf1.setContentEntryUid(entryDao.insert(subcatLeaf1));

        subcatLeaf2 = new ContentEntry("SubCat Leaf2", "SubCat Leaf2", true, true);
        subcatLeaf2.setContentEntryUid(entryDao.insert(subcatLeaf2));

        //create required parent-child joins
        repo.getContentEntryParentChildJoinDao().insertList(Arrays.asList(
                new ContentEntryParentChildJoin(parentEntry, subLeaf, 0),
                new ContentEntryParentChildJoin(subCat, subcatLeaf1, 0),
                new ContentEntryParentChildJoin(subCat, subcatLeaf2, 1)
        ));

        //Create containers
        subLeafContainer = new Container(subLeaf);
        subLeafContainer.setFileSize(1000);
        subLeafContainer.setContainerUid(repo.getContainerDao().insert(subLeafContainer));

        subcatLeaf1Container = new Container(subcatLeaf1);
        subcatLeaf1Container.setFileSize(1250);
        subcatLeaf1Container.setContainerUid(repo.getContainerDao().insert(subcatLeaf1Container));

        subcatLeaf2Container = new Container(subcatLeaf2);
        subcatLeaf2Container.setFileSize(1500);
        subcatLeaf2Container.setContainerUid(repo.getContainerDao().insert(subcatLeaf2Container));


        //Create the download set
        downloadSet = new DownloadSet(parentEntry);
        downloadSet.setDsUid(db.getDownloadSetDao().insert(downloadSet));

        parentEntryDownloadSetItem = new DownloadSetItem(downloadSet, parentEntry);
        parentEntryDownloadSetItem.setDsiUid(db.getDownloadSetItemDao().insert(parentEntryDownloadSetItem));

        subLeafDownloadSetItem = new DownloadSetItem(downloadSet, subLeaf);
        subLeafDownloadSetItem.setDsiUid(db.getDownloadSetItemDao().insert(subLeafDownloadSetItem));

        subCatDownloadSetItem = new DownloadSetItem(downloadSet, subCat);
        subCatDownloadSetItem.setDsiUid(db.getDownloadSetItemDao().insert(subCatDownloadSetItem));

        subcatLeaf1DownloadSetItem = new DownloadSetItem(downloadSet, subcatLeaf1);
        subcatLeaf1DownloadSetItem.setDsiUid(db.getDownloadSetItemDao().insert(subcatLeaf1DownloadSetItem));

        subcatLeaf2DownloadSetItem = new DownloadSetItem(downloadSet, subcatLeaf2);
        subcatLeaf2DownloadSetItem.setDsiUid(db.getDownloadSetItemDao().insert(subcatLeaf2DownloadSetItem));
    }


    @Test
    public void givenEmptyDownloadJob_whenChildItemsAreAdded_thenParentTotalsAreUpdated()
            throws InterruptedException{
        DownloadJobItemTracker tracker = new DownloadJobItemTracker(db);


        DownloadJobItemWithDownloadSetItem parentDownloadJobItem = new DownloadJobItemWithDownloadSetItem(
                parentEntryDownloadSetItem, 0);
        tracker.insertNewDownloadJobItem(parentDownloadJobItem);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DownloadJobItemWithDownloadSetItem> lastParentEntryVal = new AtomicReference<>();
        UmObserver<DownloadJobItemWithDownloadSetItem> observer = (newItem) -> {
            if(newItem.getDownloadLength() == subLeafContainer.getFileSize()) {
                lastParentEntryVal.set(newItem);
                latch.countDown();
            }
        };

        tracker.observeDownloadJobItem(parentEntry.getContentEntryUid(), observer);

        DownloadJobItemWithDownloadSetItem subLeafDownloadJobItem = new DownloadJobItemWithDownloadSetItem(
                subLeafDownloadSetItem, subLeafContainer.getFileSize());
        subLeafDownloadJobItem.setDjiParentDjiUid(parentDownloadJobItem.getDjiUid());
        tracker.insertNewDownloadJobItem(subLeafDownloadJobItem);

        latch.await(5000 * 1000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(subLeafContainer.getFileSize(),
                lastParentEntryVal.get().getDownloadLength());
    }

    @Test
    public void givenDownloadJobsAlreadyInDb_whenChildIsTrackedAndUpdatePosted_parentOnChangeShouldBeCalled() {
        DownloadJobItemWithDownloadSetItem parentItem = new DownloadJobItemWithDownloadSetItem(
                parentEntryDownloadSetItem, 1000);
        parentItem.setDjiUid(db.getDownloadJobItemDao().insert(parentItem));
        DownloadJobItemWithDownloadSetItem childItem = new DownloadJobItemWithDownloadSetItem(
                subLeafDownloadSetItem, 1000);
        childItem.setDjiUid(db.getDownloadJobItemDao().insert(childItem));



    }

    @Test
    public void givenNoMoreObserversLeft_whenCleanupIsCalled_updatesAreCommittedAndTrackerIsUnloadedFromMemory() {
        //should be unloaded when there are no trackers of the item or any child item

    }




}
