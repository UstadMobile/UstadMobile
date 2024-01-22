package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Update
import kotlin.js.JsName

/**
 * Represents the minimal functionality that a DAO is expected to provide
 *
 * @param <T> The entity object
</T> */
interface BaseDao<T> {

    /**
     * Insert the given entity
     *
     * @param entity entity to insert
     *
     * @return the generated primary key (if any)
     */
    @Insert
    fun insert(entity: T): Long

    /**
     * Insert the given entity asynchronously
     *
     * @param entity entity to insert
     * @param result calback to run when complete, which returns the generated primary key (if any)
     */
    @JsName("insertAsync")
    @Insert
    suspend fun insertAsync(entity: T): Long

    @Insert
    fun insertList(entityList: List<T>)

    @Update
    fun update(entity: T)

}
