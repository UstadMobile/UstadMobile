package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryParentChildJoinDao
        implements SyncableDao<ContentEntryParentChildJoin, ContentEntryParentChildJoinDao> {


    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
           "cepcjChildContentEntryUid = :childEntryContentUid LIMIT 1")
    public abstract ContentEntryParentChildJoin findParentByChildUuids(long childEntryContentUid);

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
            "cepcjChildContentEntryUid = :childEntryContentUid")
    public abstract List<ContentEntryParentChildJoin> findListOfParentsByChildUuid(long childEntryContentUid);

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin WHERE " +
            "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid LIMIT 1")
    public abstract ContentEntryParentChildJoin findJoinByParentChildUuids(long parentUid, long childUid);

    @UmUpdate
    public abstract void update(ContentEntryParentChildJoin entity);

    @UmQuery("SELECT ContentEntryParentChildJoin.* FROM " +
            "ContentEntryParentChildJoin " +
            "LEFT JOIN ContentEntry parentEntry ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = parentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntry childEntry ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = childEntry.contentEntryUid " +
            "WHERE parentEntry.publik AND childEntry.publik")
    public abstract List<ContentEntryParentChildJoin> getPublicContentEntryParentChildJoins();

    @UmQuery("SELECT * FROM ContentEntryParentChildJoin")
    public abstract List<ContentEntryParentChildJoin> getAll();
}
