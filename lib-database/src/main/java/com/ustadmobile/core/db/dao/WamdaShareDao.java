package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.WamdaShare;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class WamdaShareDao implements SyncableDao<WamdaShare, WamdaShareDao> {
    @UmInsert
    public abstract void insertAsync(WamdaShare share, UmCallback<Long> callback);
}
