package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

@UmDao
public abstract class ContentEntryContentCategoryJoinDao
        implements BaseDao<ContentEntryContentCategoryJoin> {

    @UmQuery("SELECT * from ContentEntryContentCategoryJoin WHERE " +
            "ceccjContentCategoryUid = :categoryUid AND ceccjContentEntryUid = :contentEntry")
    public abstract ContentEntryContentCategoryJoin findJoinByParentChildUuids(long categoryUid, long contentEntry);

    @UmUpdate
    public abstract void updateCategoryChildJoin(ContentEntryContentCategoryJoin categoryToSimlationJoin);
}
