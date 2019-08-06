package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzActivityChange


@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class ClazzActivityChangeDao : BaseDao<ClazzActivityChange> {

    @Insert
    abstract override fun insert(entity: ClazzActivityChange): Long

    @Update
    abstract override fun update(entity: ClazzActivityChange)

    @Insert
    abstract fun insertAsync(entity: ClazzActivityChange, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM ClazzActivityChange")
    abstract fun findAllClazzActivityChanges(): DataSource.Factory<Int, ClazzActivityChange>

    @Query("SELECT * FROM ClazzActivityChange")
    abstract fun findAllClazzActivityChangesAsync(resultList: UmCallback<List<ClazzActivityChange>>)

    @Query("SELECT * FROM ClazzActivityChange")
    abstract fun findAllClazzActivityChangesAsyncLive(): DoorLiveData<List<ClazzActivityChange>>

    @Update
    abstract fun updateAsync(entity: ClazzActivityChange, resultObject: UmCallback<Int>)

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    abstract fun findByUid(uid: Long): ClazzActivityChange

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<ClazzActivityChange>)

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeTitle = :title")
    abstract fun findByTitle(title: String): ClazzActivityChange
}
