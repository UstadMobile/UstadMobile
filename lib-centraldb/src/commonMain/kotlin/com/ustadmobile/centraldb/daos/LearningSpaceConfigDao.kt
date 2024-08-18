package com.ustadmobile.centraldb.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.centraldb.entities.LearningSpaceConfig
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceConfigDao {

    @Insert
    abstract suspend fun insertAsync(learningSpaceConfig: LearningSpaceConfig)

}