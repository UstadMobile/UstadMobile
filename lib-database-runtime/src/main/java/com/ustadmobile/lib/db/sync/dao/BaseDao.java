package com.ustadmobile.lib.db.sync.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmUpdate;

import java.util.List;

/**
 * Represents the minimal functionality that a DAO is expected to provide
 *
 * @param <T> The entity object
 */
public interface BaseDao<T> {

    /**
     * Insert the given entity
     *
     * @param entity entity to insert
     *
     * @return the generated primary key (if any)
     */
    @UmInsert
    long insert(T entity);

    /**
     * Insert the given entity asynchronously
     *
     * @param entity entity to insert
     * @param result calback to run when complete, which returns the generated primary key (if any)
     */
    @UmInsert
    void insertAsync(T entity, UmCallback<Long> result);

    @UmInsert
    void insertList(List<T> entityList);

    /**
     *
     * @param entityList
     */
    @UmUpdate
    void updateList(List<T> entityList);

    /**
     * Find the given entity by the primary key
     *
     * @param uid uid to find
     * @return the object represented by the primary key, or null if there is no such object
     */
    @UmQueryFindByPrimaryKey
    T findByUid(long uid);

    @UmUpdate
    void update(T entity);

}
