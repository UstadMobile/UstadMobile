package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;

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


}
