package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryDao implements SyncableDao<ContentEntry, ContentEntryDao> {

    @UmQuery("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    public abstract ContentEntry findBySourceUrl(String sourceUrl);

    @UmQuery("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract UmProvider<ContentEntry> getChildrenByParentUid(long parentUid);

    @UmQuery("SELECT COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    public abstract void getCountNumberOfChildrenByParentUUid(long parentUid, UmCallback<Integer> callback);


    @UmQuery("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    public abstract void getContentByUuid(long parentUid, UmCallback<ContentEntry> callback);


    @UmQuery("SELECT ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    public abstract void findAllLanguageRelatedEntries(long entryUuid, UmCallback<List<ContentEntry>> umCallback);


    @UmUpdate
    public abstract void update(ContentEntry entity);

    @UmQueryFindByPrimaryKey
    public abstract void findByUid(Long entryUid, UmCallback<ContentEntry> umCallback);

    @UmQuery("SELECT * FROM ContentEntry WHERE publik")
    public abstract List<ContentEntry> getPublicContentEntries();
}
