package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryParentChildJoinDao
        implements SyncableDao<ContentEntryParentChildJoin, ContentEntryParentChildJoinDao> {

    @UmQuery("SELECT * from ContentEntryParentChildJoin WHERE " +
           "cepcjChildContentEntryUid = :childEntryContentUid")
    public abstract ContentEntryParentChildJoin findParentByChildUuids(long childEntryContentUid);

    @UmQuery("SELECT * from ContentEntryParentChildJoin WHERE " +
            "cepcjChildContentEntryUid = :childEntryContentUid")
    public abstract List<ContentEntryParentChildJoin> findListOfParentsByChildUuid(long childEntryContentUid);

    @UmQuery("SELECT * from ContentEntryParentChildJoin WHERE " +
            "cepcjParentContentEntryUid = :parentUid AND cepcjChildContentEntryUid = :childUid")
    public abstract ContentEntryParentChildJoin findJoinByParentChildUuids(long parentUid, long childUid);

    @UmUpdate
    public abstract void update(ContentEntryParentChildJoin entity);

}
