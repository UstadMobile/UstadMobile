package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ClazzMemberDao implements SyncableDao<ClazzMember, ClazzMemberDao> {

    @UmInsert
    public abstract long insert(ClazzMember entity);

    @UmInsert
    public abstract void insertAsync(ClazzMember entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :memberPersonUid")
    public abstract ClazzMember findByUid(long memberPersonUid);
}
