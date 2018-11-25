package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

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
        ExampleDatabase db1 = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase db2 = ExampleDatabase.getInstance(null, "db2");
        ExampleSyncableDao dao1 = db1.getExampleSyncableDao();
        ExampleSyncableDao dao2 = db2.getExampleSyncableDao();

        db1.clearAll();
        db2.clearAll();

        db2.setMaster(true);

        ExampleSyncableEntity syncableEntity1 = new ExampleSyncableEntity();
        syncableEntity1.setTitle("Syncable 1");
        syncableEntity1.setLocalChangeSeqNum(
                db1.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(
                        ExampleSyncableEntity.TABLE_ID, 1));
        long insertedUid = db1.getExampleSyncableDao().insert(syncableEntity1);
        dao1.syncWith(dao2, 0);
        ExampleSyncableEntity syncableEntity2 = dao2.findByUid(insertedUid);
        syncableEntity2.setTitle("Updated");
        syncableEntity2.setMasterChangeSeqNum(db2.getSyncStatusDao()
                .getAndIncrementNextMasterChangeSeqNum(ExampleSyncableEntity.TABLE_ID, 1));
        dao2.updateList(Arrays.asList(syncableEntity2));

        dao1.syncWith(dao2, 0);

        syncableEntity1 = dao1.findByUid(insertedUid);;
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
        Assert.assertEquals("Next local change seq num = 2", 2,
                status.getLocalChangeSeqNum());
        Assert.assertEquals("After update of entity, entity's local change sequence number = 2",
                2, localChangeSeqNumAfterUpdate);
    }

    @Test
    public void givenSyncableEntityStatusCreated_whenMasterSeqNumGetAndIncrementCalled_shouldStartAt1AndIncrement() {
        ExampleDatabase db1 = ExampleDatabase.getInstance(null);
        db1.clearAll();
        long firstMasterChangeSeqNum = db1.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum(
                42, 1);
        long masterChangeSeqAfterUpdate = db1.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum(
                42, 1);

        Assert.assertEquals(1, firstMasterChangeSeqNum);
        Assert.assertEquals(2, masterChangeSeqAfterUpdate);
    }

    @Test
    public void givenSyncableEntityStatusCreated_whenLocalSeqNumGetAndIncrementCalled_shouldStartAt1AndIncrement() {
        ExampleDatabase db1 = ExampleDatabase.getInstance(null);
        db1.clearAll();

        long firstLocalChangeSeqNum = db1.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(
                42, 1);
        long localChangeSeqAfterUpdate = db1.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(
                42, 1);

        Assert.assertEquals(1, firstLocalChangeSeqNum);
        Assert.assertEquals(2, localChangeSeqAfterUpdate);
    }

}
