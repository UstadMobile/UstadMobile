package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmInsert;

public interface BaseDao<T> {

    @UmInsert
    long insert(T entity);

    @UmInsert
    void insertAsync(T entity, UmCallback<Long> result);

    T findByUid(long uid);

}
