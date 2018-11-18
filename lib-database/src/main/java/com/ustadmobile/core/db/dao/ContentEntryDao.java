package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

@UmDao
@UmRepository
public abstract class ContentEntryDao implements SyncableDao<ContentEntry, ContentEntryDao> {


    @UmSyncFindUpdateable
    @UmQuery("SELECT ContentEntry.contentEntryUid AS primaryKey, 1 as userCanUpdate " +
            "FROM ContentEntry " +
            "WHERE ContentEntry.contentEntryUid IN (:primaryKeys) AND (:accountPersonUid = :accountPersonUid)")
    public abstract List<UmSyncExistingEntity> syncFindExistingEntities(List<Long> primaryKeys, long accountPersonUid);


    @UmSyncFindAllChanges
    @UmQuery("SELECT * FROM ContentEntry WHERE contentEntryLocalChangeSeqNum BETWEEN :fromLocalChangeSeqNum AND :toLocalChangeSeqNum " +
            " AND contentEntryMasterChangeSeqNum BETWEEN :fromMasterChangeSeqNum and :toMasterChangeSeqNum " +
            " AND (:accountPersonUid = :accountPersonUid)")
    public abstract List<ContentEntry> syncFindAllChanges(long fromLocalChangeSeqNum, long toLocalChangeSeqNum,
                                                          long fromMasterChangeSeqNum, long toMasterChangeSeqNum,
                                                          long accountPersonUid);

    @UmSyncFindLocalChanges
    @UmQuery("SELECT * FROM ContentEntry WHERE contentEntryLocalChangeSeqNum >= :fromLocalChangeSeqNum AND (:accountPersonUid = :accountPersonUid)")
    public abstract List<ContentEntry> findLocalChanges(long fromLocalChangeSeqNum, long accountPersonUid);



}
