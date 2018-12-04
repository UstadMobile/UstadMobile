package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class TestSyncableDb  {

    @Test
    public void givenSyncableEntityCreatedOnClient_whenSynced_shouldBeRetrievableOnMaster() {
        ExampleDatabase db1 = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase db2 = ExampleDatabase.getInstance(null, "db2");
        db1.clearAll();
        db2.clearAll();

        db2.setMaster(true);

        ExampleSyncableEntity syncableEntity1 = new ExampleSyncableEntity();
        syncableEntity1.setTitle("Syncable 1");
        syncableEntity1.setLocalChangeSeqNum(
                db1.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(
                        ExampleSyncableEntity.TABLE_ID, 1));
        long insertedUid = db1.getExampleSyncableDao().insert(syncableEntity1);

        ExampleSyncableDao dao1 = db1.getExampleSyncableDao();
        ExampleSyncableDao dao2 = db2.getExampleSyncableDao();
        dao1.syncWith(dao2, 0);

        ExampleSyncableEntity syncableEntity2 = db2.getExampleSyncableDao().findByUid(insertedUid);
        Assert.assertNotNull("Syncable entity was transferred to db2", syncableEntity2);
    }

    @Test
    public void givenSyncableEntityUpdatedOnMaster_whenSynced_shouldBeUpdatedOnClient() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        ExampleSyncableDao clientDao = clientDb.getExampleSyncableDao();
        ExampleSyncableDao serverDao = serverDb.getExampleSyncableDao();

        clientDb.clearAll();
        serverDb.clearAll();

        serverDb.setMaster(true);

        ExampleSyncableEntity syncableEntity1 = new ExampleSyncableEntity();
        syncableEntity1.setTitle("Syncable 1");
        syncableEntity1.setLocalChangeSeqNum(
                clientDb.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(
                        ExampleSyncableEntity.TABLE_ID, 1));
        long insertedUid = clientDb.getExampleSyncableDao().insert(syncableEntity1);
        clientDao.syncWith(serverDao, 0);
        ExampleSyncableEntity syncableEntity2 = serverDao.findByUid(insertedUid);
        syncableEntity2.setTitle("Updated");
        syncableEntity2.setMasterChangeSeqNum(serverDb.getSyncStatusDao()
                .getAndIncrementNextMasterChangeSeqNum(ExampleSyncableEntity.TABLE_ID, 1));
        serverDao.updateList(Arrays.asList(syncableEntity2));

        clientDao.syncWith(serverDao, 0);

        syncableEntity1 = clientDao.findByUid(insertedUid);;
        Assert.assertEquals("After sync - entity has been updated on client", "Updated",
                syncableEntity1.getTitle());
    }


    @Test
    public void givenSyncableEntityInserted_whenUpdated_syncStatusShouldIncrementByOne() {
        ExampleDatabase masterDb = ExampleDatabase.getInstance(null);
        masterDb.setMaster(true);
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        clientDb.setMaster(false);
        ExampleDatabase clientRepo = clientDb.getRepository("http://localhost/", "");
        masterDb.clearAll();
        clientDb.clearAll();

        ExampleSyncableEntity entity1 = new ExampleSyncableEntity();
        entity1.setTitle("Syncable 1 inserted");
        clientRepo.getExampleSyncableDao().insert(entity1);
        long localChangeSeqNumAfterInsert = clientRepo.getExampleSyncableDao().findByUid(
                entity1.getExampleSyncableUid()).getLocalChangeSeqNum();

        entity1.setTitle("Syncable 1 updated");
        clientRepo.getExampleSyncableDao().updateEntity(entity1);
        SyncStatus status = clientDb.getSyncStatusDao().getByUid(ExampleSyncableEntity.TABLE_ID);
        long localChangeSeqNumAfterUpdate = clientRepo.getExampleSyncableDao().findByUid(
                entity1.getExampleSyncableUid()).getLocalChangeSeqNum();


        Assert.assertEquals("After insert of first entity, entity's local change sequence " +
                        "number = 1", 1, localChangeSeqNumAfterInsert);
        Assert.assertEquals("After update of entity, entity's local change sequence number = 2",
                2, localChangeSeqNumAfterUpdate);
        Assert.assertEquals("Next local change seq num = 3", 3,
                status.getLocalChangeSeqNum());
    }



    @Test
    public void givenEntityList_whenAsyncListInsertedByRepo_shouldBeInsertedAndReturnSyncablePksGenerated() {
        ExampleDatabase db1 = ExampleDatabase.getInstance(null);
        db1.clearAll();
        ExampleDatabase repo1 = db1.getRepository("http://localhost", "");
        CountDownLatch latch = new CountDownLatch(1);

        ExampleSyncableEntity entity1 = new ExampleSyncableEntity();
        entity1.setTitle("Entity 1");
        ExampleSyncableEntity entity2 = new ExampleSyncableEntity();
        entity2.setTitle("Entity 2");
        AtomicReference<Long[]> resultRef = new AtomicReference<>();

        repo1.getExampleSyncableDao().insertListAsyncArr(Arrays.asList(entity1, entity2),
                new UmCallback<Long[]>() {
            @Override
            public void onSuccess(Long[] result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable exception) {
                latch.countDown();
            }
        });

        try { latch.await(2, TimeUnit.MINUTES); }
        catch(InterruptedException e) {
            // will not be interrupted
        }

        ExampleSyncableEntity entity1Retrieved = db1.getExampleSyncableDao().findByUid(
                resultRef.get()[0]);
        ExampleSyncableEntity entity2Retrieved = db1.getExampleSyncableDao().findByUid(
                resultRef.get()[1]);
        Assert.assertNotNull("Entity 1 lookup by uid OK", entity1Retrieved);
        Assert.assertNotNull("Entity 2 lookup by uid OK", entity2Retrieved);
    }


}
