package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryContentCategoryJoinDao
        implements SyncableDao<ContentEntryContentCategoryJoin, ContentEntryContentCategoryJoinDao> {

    @UmQuery("SELECT * from ContentEntryContentCategoryJoin WHERE " +
            "ceccjContentCategoryUid = :categoryUid AND ceccjContentEntryUid = :contentEntry")
    public abstract ContentEntryContentCategoryJoin findJoinByParentChildUuids(long categoryUid, long contentEntry);

    @UmUpdate
    public abstract void update(ContentEntryContentCategoryJoin entity);

}
