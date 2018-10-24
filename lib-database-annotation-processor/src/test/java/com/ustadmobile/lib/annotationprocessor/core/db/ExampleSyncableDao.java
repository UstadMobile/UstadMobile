package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
public abstract class ExampleSyncableDao implements SyncableDao<ExampleSyncableEntity, ExampleSyncableDao>  {

    @UmSyncFindUpdateable
    @UmQuery("SELECT ExampleSyncableEntity.exampleSyncableUid AS primaryKey, 1 as userCanUpdate " +
            "FROM ExampleSyncableEntity " +
            "WHERE ExampleSyncableEntity.exampleSyncableUid IN (:primaryKeys)")
    public abstract List<UmSyncExistingEntity> syncFindExistingEntities(List<Long> primaryKeys, long accountPersonUid);

    @UmSyncFindAllChanges
    @UmQuery("SELECT * FROM ExampleSyncableEntity " +
            "WHERE localChangeSeqNum BETWEEN :fromLocalChangeSeqNum AND :toLocalChangeSeqNum" +
            " AND masterChangeSeqNum BETWEEN :fromMasterChangeSeqNum AND :toMasterChangeSeqNum " +
            " AND (:accountPersonUid = :accountPersonUid)  ")
    public abstract List<ExampleSyncableEntity> syncFindAllChanges(long fromLocalChangeSeqNum, long toLocalChangeSeqNum,
                               long fromMasterChangeSeqNum, long toMasterChangeSeqNum,
                               long accountPersonUid);

    @UmSyncFindLocalChanges
    @UmQuery("SELECT ExampleSyncableEntity.* FROM ExampleSyncableEntity WHERE localChangeSeqNum > :fromLocalChangeSeqNum AND (:userId = :userId)")
    public abstract List<ExampleSyncableEntity> findLocalChanges(long fromLocalChangeSeqNum, long userId);
}
