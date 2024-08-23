package com.ustadmobile.appconfigdb.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(learningSpace: LearningSpaceInfo)

    @Update
    abstract fun update(learningSpace: LearningSpaceInfo)

    @Insert
    abstract fun insert(learningSpace: LearningSpaceInfo)

}
