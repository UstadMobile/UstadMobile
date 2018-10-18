package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.SyncResponse;
import com.ustadmobile.lib.db.entities.SyncStatus;

import java.util.List;

@UmDao
public abstract class ContentEntryDao implements BaseDao<ContentEntry> {


    public SyncResponse<ContentEntry> handlingIncomingSync(List<ContentEntry> incomingChanges,
                                                           long fromLocalChangeSeqNum,
                                                           long fromMasterChangeSeqNum,
                                                           long userId, UmAppDatabase myDb) {
        SyncResponse<ContentEntry> response = new SyncResponse<>();
        long toMasterChangeSeq = Long.MAX_VALUE;
        long toLocalChangeSeq = Long.MAX_VALUE;
        boolean isMaster = true;//need to check if the database is master
        if(isMaster) {
            long changeSeqStart = myDb.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum(
                    ContentEntry.TABLE_ID, incomingChanges.size());
            response.setSyncedUpToMasterChangeSeqNum(changeSeqStart + incomingChanges.size());
            toMasterChangeSeq = (changeSeqStart -1);
            for(int i = 0; i < incomingChanges.size(); i++) {
                incomingChanges.get(i).setContentEntryMasterChangeSeqNum(changeSeqStart + i);
                response.getAssignedMasterSequenceIds().put(
                        incomingChanges.get(i).getContentEntryLocalChangeSeqNum(),
                        changeSeqStart + 1);
                incomingChanges.get(i).setContentEntryLocalChangeSeqNum(0);
            }
        }

        //TODO: permissions + conflict check
        insertList(incomingChanges);
        response.setRemoteChangedEntities(findChangedEntities(
                fromLocalChangeSeqNum, toLocalChangeSeq,
                fromMasterChangeSeqNum, toMasterChangeSeq, userId));

        return response;
    }


    @UmQuery("SELECT * FROM ContentEntry " +
            "WHERE contentEntryLocalChangeSeqNum BETWEEN :fromLocalChangeSeqNum AND :toLocalChangeSeqNum" +
            " AND contentEntryMasterChangeSeqNum BETWEEN :fromMasterChangeSeqNum AND :toMasterChangeSeqNum " +
            " AND (:userId = :userId)  ")
    public abstract List<ContentEntry> findChangedEntities(long fromLocalChangeSeqNum, long toLocalChangeSeqNum,
                                                           long fromMasterChangeSeqNum, long toMasterChangeSeqNum,
                                                           long userId);


    @UmQuery("SELECT ContentEntry.* FROM ContentEntry WHERE contentEntryLocalChangeSeqNum > :fromLocalChangeSeqNum AND (:userId = :userId)")
    public abstract List<ContentEntry> findLocallyChangedEntities(long fromLocalChangeSeqNum, long userId);

    public void syncWith(ContentEntryDao otherDao, long personUid, UmAppDatabase myDb, UmAppDatabase otherDb) {
        SyncStatus syncStatus = myDb.getSyncStatusDao().findByUid(ContentEntry.TABLE_ID);
        List<ContentEntry> localChanges = findLocallyChangedEntities(
                syncStatus.getSyncedToLocalChangeSeqNum(), personUid);
        SyncResponse<ContentEntry> remoteChanges = otherDao.handlingIncomingSync(localChanges,
                0, syncStatus.getSyncedToMasterChangeNum(), personUid, otherDb);
        insertList(remoteChanges.getRemoteChangedEntities());
        //we are now synced up to (what was) the next local change seq number
        myDb.getSyncStatusDao().updateSyncedToChangeSeqNums(ContentEntry.TABLE_ID,
                syncStatus.getNextLocalChangeSeqNum(), remoteChanges.getSyncedUpToMasterChangeSeqNum());
    }

    @UmUpdate
    public abstract void updateEntry(ContentEntry entry);




}
