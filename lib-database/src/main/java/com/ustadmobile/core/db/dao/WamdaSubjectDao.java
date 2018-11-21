package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.WamdaSubject;

@UmDao
public abstract class WamdaSubjectDao {

    @UmInsert
    public abstract void insert(WamdaSubject subject, UmCallback<Long> callback);


}
