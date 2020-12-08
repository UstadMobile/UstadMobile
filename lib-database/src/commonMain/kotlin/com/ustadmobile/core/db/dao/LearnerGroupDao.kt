package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.LearnerGroup

@Repository
@Dao
abstract class LearnerGroupDao : BaseDao<LearnerGroup> {


    @Query("""SELECT LearnerGroup.* FROM LearnerGroup 
            LEFT JOIN GroupLearningSession ON 
            GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroup.learnerGroupUid 
            WHERE GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid""")
    abstract fun findGroupsForEntryAsync(contentEntryUid: Long): DataSource.Factory<Int, LearnerGroup>

    @Query("""SELECT LearnerGroup.* FROM LearnerGroup 
            LEFT JOIN GroupLearningSession ON 
            GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroup.learnerGroupUid 
            WHERE GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid""")
    abstract fun findGroupListForEntry(contentEntryUid: Long): List<LearnerGroup>

}