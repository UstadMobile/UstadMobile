package com.ustadmobile.appconfigdb.daos

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.door.annotation.DoorDao

@DoorDao
expect abstract class LearningSpaceConfigDao {

    @Insert
    abstract suspend fun insertAsync(learningSpaceConfig: LearningSpaceConfig)

    @Query("""
        SELECT LearningSpaceConfig.*, LearningSpaceInfo.*
          FROM LearningSpaceConfig
               JOIN LearningSpaceInfo 
                    ON LearningSpaceInfo.lsiUid  = LearningSpaceConfig.lscUid 
    """)
    abstract fun findAllLearningSpaceConfigAndInfo(): List<LearningSpaceConfigAndInfo>

    @Update
    abstract fun update(learningSpaceConfig: LearningSpaceConfig)

    @Insert
    abstract fun insert(learningSpace: LearningSpaceConfig)
}