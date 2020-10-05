package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.LearnerGroup
import com.ustadmobile.lib.db.entities.LearnerGroupMember

@UmRepository
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