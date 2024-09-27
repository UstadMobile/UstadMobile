package com.ustadmobile.appconfigdb.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cash.paging.PagingSource
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository

@DoorDao
@Repository
expect abstract class LearningSpaceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(learningSpace: LearningSpaceInfo)

    @Update
    abstract fun update(learningSpace: LearningSpaceInfo)

    @Insert
    abstract fun insert(learningSpace: LearningSpaceInfo)

    @HttpAccessible
    @Query("""
        SELECT LearningSpaceInfo.*
          FROM LearningSpaceInfo
    """)
    abstract fun findAllAsPagingSource(): PagingSource<Int, LearningSpaceInfo>

}
