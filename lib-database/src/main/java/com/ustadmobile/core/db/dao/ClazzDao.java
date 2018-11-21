package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ClazzDao implements SyncableDao<Clazz, ClazzDao> {

    @Override
    @UmInsert
    public abstract long insert(Clazz entity);

    @Override
    @UmInsert
    public abstract void insertAsync(Clazz entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract Clazz findByUid(long uid);


    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzesByPersonUid(long personUid);

}
