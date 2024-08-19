package com.ustadmobile.appconfigdb.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(learningSpace: LearningSpaceInfo)

}
