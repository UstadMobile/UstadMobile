package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestDownloadJobItemManager {

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private ContentEntry parentEntry;

    private ContentEntry subLeaf;

    private ContentEntryParentChildJoin subLeafParentEntryJoin;

    private ContentEntry subCat;

    private ContentEntryParentChildJoin subCatParentEntryJoin;

    private ContentEntry subcatLeaf1;

    private ContentEntryParentChildJoin subcatLeaf1ParentEntryJoin;

    private ContentEntry subcatLeaf2;

    private ContentEntryParentChildJoin subcatLeaf2ParentEntryJoin;

    private Container subLeafContainer;

    private Container subcatLeaf1Container;

    private Container subcatLeaf2Container;

    private DownloadJob downloadJob;


    public void setupDb() {
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        ContentEntryDao entryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao entryParentDao = repo.getContentEntryParentChildJoinDao();
        parentEntry = new ContentEntry("Parent entry", "parent", false, true);
        parentEntry.setContentEntryUid(entryDao.insert(parentEntry));

        subLeaf = new ContentEntry("Sub Leaf", "subleaf", true, true);
        subLeaf.setContentEntryUid(entryDao.insert(subLeaf));
        subLeafParentEntryJoin = new ContentEntryParentChildJoin(parentEntry, subLeaf, 0);
        subLeafParentEntryJoin.setCepcjUid(entryParentDao.insert(subLeafParentEntryJoin));

        subCat = new ContentEntry("Sub cat", "sub cat", false, true);
        subCat.setContentEntryUid(entryDao.insert(subCat));
        subCatParentEntryJoin = new ContentEntryParentChildJoin(parentEntry, subCat, 0);
        subCatParentEntryJoin.setCepcjUid(entryParentDao.insert(subCatParentEntryJoin));

        subcatLeaf1 = new ContentEntry("SubCat Leaf1", "subcatleaf1", true, true);
        subcatLeaf1.setContentEntryUid(entryDao.insert(subcatLeaf1));
        subcatLeaf1ParentEntryJoin = new ContentEntryParentChildJoin(subCat, subcatLeaf1, 0);
        subcatLeaf1ParentEntryJoin.setCepcjUid(entryParentDao.insert(subcatLeaf1ParentEntryJoin));

        subcatLeaf2 = new ContentEntry("SubCat Leaf2", "SubCat Leaf2", true, true);
        subcatLeaf2.setContentEntryUid(entryDao.insert(subcatLeaf2));
        subcatLeaf2ParentEntryJoin = new ContentEntryParentChildJoin(subCat, subcatLeaf2, 1);
        subcatLeaf2ParentEntryJoin.setCepcjUid(entryParentDao.insert(subcatLeaf2ParentEntryJoin));

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


        downloadJob = new DownloadJob();
        downloadJob.setDjRootContentEntryUid(parentEntry.getContentEntryUid());
        downloadJob.setDjUid(db.getDownloadJobDao().insert(downloadJob));
    }


    protected void setupRootAndSubleaf(DownloadJobItemManager manager) throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(2);
        DownloadJobItem rootDjItem = new DownloadJobItem(downloadJob,
                parentEntry.getContentEntryUid(), 0);
        manager.insertDownloadJobItems(Arrays.asList(rootDjItem), (aVoid) -> latch.countDown());

        DownloadJobItem subLeafDjItem = new DownloadJobItem(downloadJob,
                subLeaf.getContentEntryUid(), subLeafContainer.getFileSize());
        manager.insertDownloadJobItems(Arrays.asList(subLeafDjItem), (aVoid) -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);

        CountDownLatch latch2 = new CountDownLatch(1);
        manager.insertParentChildJoins(Arrays.asList(new DownloadJobItemParentChildJoin(
                        (int)rootDjItem.getDjiUid(), (int)subLeafDjItem.getDjiUid(), 0)),
                (aVoid) -> latch2.countDown());
        latch2.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void givenDownloadWithChildren_whenItemAdded_thenShouldIncreaseTotalLength()
            throws InterruptedException{
        setupDb();
        DownloadJobItemManager manager = new DownloadJobItemManager(db, (int)downloadJob.getDjUid());
        AtomicReference<DownloadJobItemStatus> statusRef = new AtomicReference<>();
        manager.setOnDownloadJobItemChangeListener((status) -> {
            if(status.getContentEntryUid() == downloadJob.getDjRootContentEntryUid()
                && status.getTotalBytes() == subLeafContainer.getFileSize())
                statusRef.set(status);
        });

        setupRootAndSubleaf(manager);

        Assert.assertNotNull(statusRef.get());
    }

    @Test
    public void givenDownloadWithChildren_whenCommited_thenDatabaseShouldMatch()
            throws InterruptedException{
        setupDb();
        DownloadJobItemManager manager = new DownloadJobItemManager(db, (int)downloadJob.getDjUid());
        setupRootAndSubleaf(manager);

        manager.commit();

        Assert.assertEquals("Download root item size in database matches expected size",
                subLeafContainer.getFileSize(),
                db.getDownloadJobItemDao().findByContentEntryUid2(parentEntry.getContentEntryUid())
                        .getDownloadLength());
    }


    @Test
    public void given50000ObjectsCreated_whenMemoryCounted_memoryUsageShouldBeReasonable() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        Map<Integer, DownloadJobItemStatus> statusMap = new HashMap<>();

        DownloadJobItemStatus lastItem = null;
        for(int i = 0 ; i < 50000; i++) {
            DownloadJobItemStatus status = new DownloadJobItemStatus();

            if(lastItem != null) {
                status.addParent(lastItem);
            }

            statusMap.put(i, status);
            lastItem = status;
        }

        System.gc();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) - memoryBefore;
        Assert.assertTrue(memoryUsed < 1000 * 1000 * 1000 * 10);//10MB?
    }


}
