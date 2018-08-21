package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.Clazz;

@UmDao
public abstract class ClazzDao implements BaseDao<Clazz> {

    @Override
    @UmInsert
    public abstract long insert(Clazz entity);

    @Override
    @UmInsert
    public abstract void insertAsync(Clazz entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract Clazz findByUid(long uid);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Clazz> callback);

    @UmQuery("SELECT * FROM Clazz ORDER BY clazzUid")
    public abstract UmProvider<Clazz> findAll();
}
