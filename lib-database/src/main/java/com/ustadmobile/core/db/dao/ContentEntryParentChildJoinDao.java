package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

@UmDao
public abstract class ContentEntryParentChildJoinDao
        implements BaseDao<ContentEntryParentChildJoin> {

    @UmQuery("SELECT * from ContentEntryParentChildJoin WHERE " +
           "cepcjParentContentEntryUid = :childEntryContentUid")
    public abstract ContentEntryParentChildJoin findParentByChildUuids(long childEntryContentUid);

    @UmUpdate
    public abstract int updateParentChildJoin(ContentEntryParentChildJoin edraakParentJoin);
}
