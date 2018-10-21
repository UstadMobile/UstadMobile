package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

@UmDao
public abstract class ContentEntryDao implements BaseDao<ContentEntry> {




//    public static class SyncableEntity {
//
//        private boolean userCanUpdate;
//
//        private long primaryKey;
//
//        public boolean isUserCanUpdate() {
//            return userCanUpdate;
//        }
//
//        public void setUserCanUpdate(boolean userCanUpdate) {
//            this.userCanUpdate = userCanUpdate;
//        }
//
//        public long getPrimaryKey() {
//            return primaryKey;
//        }
//
//        public void setPrimaryKey(long primaryKey) {
//            this.primaryKey = primaryKey;
//        }
//    }





//    //@UmSyncHandleIncoming
//    public SyncResponse<ContentEntry> handlingIncomingSync(List<ContentEntry> incomingChanges,
//                                                           long fromLocalChangeSeqNum,
//                                                           long fromMasterChangeSeqNum,
//                                                           long userId, UmAppDatabase myDb) {
//        SyncResponse<ContentEntry> response = new SyncResponse<>();
//        long toMasterChangeSeq = Long.MAX_VALUE;
//        long toLocalChangeSeq = Long.MAX_VALUE;
//        boolean isMaster = true;//need to check if the database is master
//        if(isMaster) {
//            long changeSeqStart = myDb.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum(
//                    ContentEntry.TABLE_ID, incomingChanges.size());
//            response.setSyncedUpToMasterChangeSeqNum(changeSeqStart + incomingChanges.size());
//            toMasterChangeSeq = (changeSeqStart -1);
//            for(int i = 0; i < incomingChanges.size(); i++) {
//                incomingChanges.get(i).setContentEntryMasterChangeSeqNum(changeSeqStart + i);
//                response.getAssignedMasterSequenceIds().put(
//                        incomingChanges.get(i).getContentEntryLocalChangeSeqNum(),
//                        changeSeqStart + 1);
//                incomingChanges.get(i).setContentEntryLocalChangeSeqNum(0);
//            }
//        }
//
//
//        List<ContentEntry> updateList = new ArrayList<>();
//        List<ContentEntry> insertList = new ArrayList<>();
//        List<Long> primaryKeyList = new ArrayList<>();
//        for(ContentEntry entry : incomingChanges) {
//            primaryKeyList.add(entry.getContentEntryUid());
//        }
//
//        List<SyncableEntity> updateableEntities = findUpdateableEntities(primaryKeyList, userId);
//        HashMap<Long, SyncableEntity> updateableMap = new HashMap<>();
//        for(SyncableEntity entity : updateableEntities) {
//            updateableMap.put(entity.getPrimaryKey(), entity);
//        }
//
//        for(ContentEntry entry : incomingChanges) {
//            if(updateableMap.containsKey(entry.getContentEntryUid())) {
//                if(updateableMap.get(entry.getContentEntryUid()).isUserCanUpdate()){
//                    updateList.add(entry);
//                }
//            }else{
//                insertList.add(entry);
//            }
//        }
//
//        //TODO: permissions + conflict check
//        insertList(insertList);
//        updateList(updateList);
//
//        response.setRemoteChangedEntities(findChangedEntities(
//                fromLocalChangeSeqNum, toLocalChangeSeq,
//                fromMasterChangeSeqNum, toMasterChangeSeq, userId));
//
//        return response;
//    }
//
//    //this returns which of the given uids can be updated by the user in question
//    @UmQuery("SELECT ContentEntry.contentEntryUid as primaryKey, 1 as userCanUpdate FROM ContentEntry " +
//            " WHERE ContentEntry.contentEntryUid in :uids")
//    protected abstract List<SyncableEntity> findUpdateableEntities(List<Long> uids, long accountPersonUid);
//
//    //@UmSyncFindChanges
//    @UmQuery("SELECT * FROM ContentEntry " +
//            "WHERE contentEntryLocalChangeSeqNum BETWEEN :fromLocalChangeSeqNum AND :toLocalChangeSeqNum" +
//            " AND contentEntryMasterChangeSeqNum BETWEEN :fromMasterChangeSeqNum AND :toMasterChangeSeqNum " +
//            " AND (:userId = :userId)  ")
//    public abstract List<ContentEntry> findChangedEntities(long fromLocalChangeSeqNum, long toLocalChangeSeqNum,
//                                                           long fromMasterChangeSeqNum, long toMasterChangeSeqNum,
//                                                           long userId);
//
//
//    @UmQuery("SELECT ContentEntry.* FROM ContentEntry WHERE contentEntryLocalChangeSeqNum > :fromLocalChangeSeqNum AND (:userId = :userId)")
//    public abstract List<ContentEntry> findLocallyChangedEntities(long fromLocalChangeSeqNum, long userId);
//
//    //@UmSyncOutgoing
//    public void syncWith(ContentEntryDao otherDao, long personUid, UmAppDatabase myDb, UmAppDatabase otherDb) {
//        SyncStatus syncStatus = myDb.getSyncStatusDao().findByUid(ContentEntry.TABLE_ID);
//        List<ContentEntry> localChanges = findLocallyChangedEntities(
//                syncStatus.getSyncedToLocalChangeSeqNum(), personUid);
//        SyncResponse<ContentEntry> remoteChanges = otherDao.handlingIncomingSync(localChanges,
//                0, syncStatus.getSyncedToMasterChangeNum(), personUid, otherDb);
//        insertList(remoteChanges.getRemoteChangedEntities());
//        //we are now synced up to (what was) the next local change seq number
//        myDb.getSyncStatusDao().updateSyncedToChangeSeqNums(ContentEntry.TABLE_ID,
//                syncStatus.getNextLocalChangeSeqNum(), remoteChanges.getSyncedUpToMasterChangeSeqNum());
//    }
//
//    @UmUpdate
//    public abstract void updateEntry(ContentEntry entry);
//
//    @UmUpdate
//    public abstract void updateList(List<ContentEntry> entryList);
//


}
