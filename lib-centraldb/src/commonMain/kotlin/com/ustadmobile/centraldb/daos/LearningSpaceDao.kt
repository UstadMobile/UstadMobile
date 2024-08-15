package com.ustadmobile.centraldb.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.centraldb.entities.LearningSpaceInfo
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(learningSpace: LearningSpaceInfo)

}
