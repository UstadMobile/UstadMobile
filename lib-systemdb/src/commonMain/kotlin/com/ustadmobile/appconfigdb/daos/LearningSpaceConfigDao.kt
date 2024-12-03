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


    @Query(
        """
        UPDATE LearningSpaceConfig
         SET lscDbUrl =
                (CASE
                 WHEN :dbUrl IS NOT NULL THEN :dbUrl
                 ELSE lscDbUrl
                 END),
            lscDbUsername =
                (CASE
                 WHEN :dbUsername IS NOT NULL THEN :dbUsername
                 ELSE lscDbUsername
                 END),
            lscDbPassword =
                (CASE
                 WHEN :dbPassword IS NOT NULL THEN :dbPassword
                 ELSE lscDbPassword
                 END)
         WHERE lscUrl = :lscUrl
     """)
    abstract  fun updateLearningSpaceConfig(
        lscUrl: String,
        dbUrl: String,
        dbPassword: String?,
        dbUsername: String?,
    )

    @Insert
    abstract fun insert(learningSpace: LearningSpaceConfig)
}