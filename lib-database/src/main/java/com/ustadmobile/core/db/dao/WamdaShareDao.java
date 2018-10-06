package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.WamdaShare;

@UmDao
public abstract class WamdaShareDao {
    @UmInsert
    public abstract void insertAsync(WamdaShare share, UmCallback<Long> callback);
}
