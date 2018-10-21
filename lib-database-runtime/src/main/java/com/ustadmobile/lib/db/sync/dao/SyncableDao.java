package com.ustadmobile.lib.db.sync.dao;

import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.db.sync.SyncResponse;

import java.util.List;

public interface SyncableDao<T, D> extends BaseDao<T> {

    /**
     * Sync with the other DAO. THe other DAO should be the same class
     *
     * @param otherDao
     * @param accountPersonUid
     */
//    @UmSyncOutgoing
//    void syncWith(D otherDao, long accountPersonUid);

    @UmSyncIncoming
    SyncResponse<T> handlingIncomingSync(List<T> incomingChanges, long fromChangeSequenceNumber,
                                         long userId);

//    @UmSyncFindLocalChanges
//    List<T> findLocalChanges(long fromLocalChangeSeqNum, long userId);
//
//    @UmSyncFindAllChanges
//    List<T> syncFindAllChanges(long fromLocalChangeSeqNum, long toLocalChangeSeqNum,
//                               long fromMasterChangeSeqNum, long toMasterChangeSeqNum,
//                               long accountPersonUid);
//
//    @UmSyncFindUpdateable
//    List<UmSyncExistingEntity> syncFindExistingEntities(long[] primaryKey, long accountPersonUid);

}
