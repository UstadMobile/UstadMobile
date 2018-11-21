package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.WamdaSubject;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
public abstract class WamdaSubjectDao implements SyncableDao<WamdaSubject, WamdaSubjectDao> {

    @UmInsert
    public abstract void insert(WamdaSubject subject, UmCallback<Long> callback);


}
