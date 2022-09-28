package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Update

interface OneToManyJoinDao<T> {

    @Insert
    suspend fun insertListAsync(entityList: List<T>)

    @Update
    suspend fun updateListAsync(entityList: List<T>)

    //suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long)


}