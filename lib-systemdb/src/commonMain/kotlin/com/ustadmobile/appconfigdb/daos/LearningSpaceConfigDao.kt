package com.ustadmobile.appconfigdb.daos

import androidx.room.Insert
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceConfigDao {

    @Insert
    abstract suspend fun insertAsync(learningSpaceConfig: LearningSpaceConfig)

}