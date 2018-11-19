package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

import javax.swing.text.AbstractDocument;

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


    @UmQuery("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl")
    public abstract ContentEntry findBySourceUrl(String sourceUrl);

    @UmQuery("Select ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract UmProvider<ContentEntry> getChildrenByParentUid(long parentUid);

    @UmQuery("Select COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract void getCountNumberOfChildrenByParentUUid(long parentUid, UmCallback<Integer> callback);


    @UmQuery("Select * FROM ContentEntry where contentEntryUid = :parentUid")
    public abstract void getContentByUuid(long parentUid, UmCallback<ContentEntry> callback);


    @UmQuery("Select ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    public abstract void findAllLanguageRelatedEntries(long entryUuid, UmCallback<List<ContentEntry>> umCallback);


    @UmUpdate
    public abstract void update(ContentEntry entity);

}
