package com.ustadmobile.appconfigdb.daos

import androidx.room.Delete
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

    @Delete
    abstract fun delete(learningSpace: LearningSpaceInfo)

    @Insert
    abstract fun insert(learningSpace: LearningSpaceInfo)

    @HttpAccessible
    @Query("""
        SELECT LearningSpaceInfo.*
          FROM LearningSpaceInfo
    """)
    abstract fun findAllAsPagingSource(): PagingSource<Int, LearningSpaceInfo>


    @Query("""
       UPDATE LearningSpaceInfo
       SET lsiUrl = :lsiUrl,
           lsiName = :lsiName
       WHERE lsiUrl = :lsiUrl
    """)
    abstract  fun updateLearningSpaceInfo(lsiUrl: String,lsiName: String)


    @Query("""
        DELETE FROM LearningSpaceInfo
        WHERE lsiUrl = :lsiUrl
    """)
    abstract  fun deleteLearningSpaceInfo(lsiUrl:String)

}
