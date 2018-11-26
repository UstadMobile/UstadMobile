package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.db.entities.SyncStatus;

public class TestSync {

//    @Test
//    public void givenEntryInLocalDatabase_whenSynced_shouldBeInMasterDb() {
//        UmAppDatabase localDb = UmAppDatabase.getInstance(null, "ds1");
//        UmAppDatabase masterDb = UmAppDatabase.getInstance(null, "ds2");
//
//        localDb.clearAllTables();
//        masterDb.clearAllTables();
//
//        SyncStatus masterSyncStatus = new SyncStatus();
//        masterSyncStatus.setTableId(TABLE_ID);
//        masterSyncStatus.setLocalChangeSeqNum(1);
//        localDb.getSyncStatusDao().insert(masterSyncStatus);
//
//        SyncStatus localSyncStatus = new SyncStatus();
//        localSyncStatus.setTableId(TABLE_ID);
//        localSyncStatus.setMasterChangeSeqNum(1);
//        masterDb.getSyncStatusDao().insert(localSyncStatus);
//
//        ContentEntry contentEntry1 = new ContentEntry();
//        contentEntry1.setContentEntryLocalChangeSeqNum(
//                localDb.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(TABLE_ID, 1));
//        contentEntry1.setTitle("Synced entity");
//        contentEntry1.setContentEntryUid(localDb.getContentEntryDao().insert(contentEntry1));
//
//
//        localDb.getContentEntryDao().syncWith(masterDb.getContentEntryDao(), 0,
//                localDb, masterDb);
//
//        ContentEntry contentEntrySyncd = masterDb.getContentEntryDao().findByUid(
//                contentEntry1.getContentEntryUid());
//
//        Assert.assertNotNull(contentEntrySyncd);
//    }
//
//
//    @Test
//    public void givenEntryUpdatedInMaster_whenSynced_shouldBeUpdatedInLocalDb(){
//        UmAppDatabase localDb = UmAppDatabase.getInstance(null, "ds1");
//        UmAppDatabase masterDb = UmAppDatabase.getInstance(null, "ds2");
//
//        localDb.clearAllTables();
//        masterDb.clearAllTables();
//
//        SyncStatus masterSyncStatus = new SyncStatus();
//        masterSyncStatus.setTableId(TABLE_ID);
//        masterSyncStatus.setLocalChangeSeqNum(1);
//        localDb.getSyncStatusDao().insert(masterSyncStatus);
//
//        SyncStatus localSyncStatus = new SyncStatus();
//        localSyncStatus.setTableId(TABLE_ID);
//        localSyncStatus.setMasterChangeSeqNum(1);
//        masterDb.getSyncStatusDao().insert(localSyncStatus);
//
//        ContentEntry contentEntry1 = new ContentEntry();
//        contentEntry1.setContentEntryLocalChangeSeqNum(
//                localDb.getSyncStatusDao().getAndIncrementNextLocalChangeSeqNum(TABLE_ID, 1));
//        contentEntry1.setTitle("Synced entity");
//        contentEntry1.setContentEntryUid(localDb.getContentEntryDao().insert(contentEntry1));
//
//
//        localDb.getContentEntryDao().syncWith(masterDb.getContentEntryDao(), 0,
//                localDb, masterDb);
//
//        ContentEntry contentEntry2 = masterDb.getContentEntryDao().findByUid(
//                contentEntry1.getContentEntryUid());
//        contentEntry2.setTitle("Updated Title");
//        contentEntry2.setContentEntryMasterChangeSeqNum(
//                masterDb.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum(TABLE_ID, 1));
//        masterDb.getContentEntryDao().updateEntry(contentEntry2);
//
//        localDb.getContentEntryDao().syncWith(masterDb.getContentEntryDao(), 0,
//                localDb, masterDb);
//
//        ContentEntry entryAfterSync = localDb.getContentEntryDao().findByUid(
//                contentEntry2.getContentEntryUid());
//        Assert.assertEquals("After sync, entry title updated on master is reflected on local db",
//                contentEntry2.getTitle(), entryAfterSync.getTitle());
//
//
//    }





}
