package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzActivity;

@UmDao
public abstract class ClazzActivityDao implements BaseDao<ClazzActivity> {

    @UmInsert
    public abstract long insert(ClazzActivity entity);

    @UmUpdate
    public abstract void update(ClazzActivity entity);

    @UmInsert
    public abstract void insertAsync(ClazzActivity entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzActivity")
    public abstract UmProvider<ClazzActivity> findAllClazzActivityChanges();

    @UmUpdate
    public abstract void updateAsync(ClazzActivity entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM ClazzActivity WHERE clazzActivityUid = :uid")
    public abstract ClazzActivity findByUid(long uid);


}
