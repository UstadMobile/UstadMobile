package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

@UmDao
public abstract class ClazzMemberDao implements BaseDao<ClazzMember> {

    @UmInsert
    public abstract long insert(ClazzMember entity);

    @UmInsert
    public abstract void insertAsync(ClazzMember entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    public abstract ClazzMember findByUid(long uid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember" +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid")
    public abstract UmProvider<ClazzMemberWithPerson> findClazzMembersByClazzId(long uid);
}
