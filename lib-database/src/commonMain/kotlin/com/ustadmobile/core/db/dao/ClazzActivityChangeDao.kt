package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM ClazzActivityChange")
    abstract fun findAllClazzActivityChanges(): DataSource.Factory<Int, ClazzActivityChange>

    @Query("SELECT * FROM ClazzActivityChange")
    abstract suspend fun findAllClazzActivityChangesAsync(): List<ClazzActivityChange>

    @Query("SELECT * FROM ClazzActivityChange")
    abstract fun findAllClazzActivityChangesAsyncLive(): DoorLiveData<List<ClazzActivityChange>>

    @Update
    abstract suspend fun updateAsync(entity: ClazzActivityChange): Int

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    abstract fun findByUid(uid: Long): ClazzActivityChange?

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzActivityChange?

    @Query("SELECT * FROM ClazzActivityChange WHERE clazzActivityChangeTitle = :title")
    abstract fun findByTitle(title: String): ClazzActivityChange?
}
