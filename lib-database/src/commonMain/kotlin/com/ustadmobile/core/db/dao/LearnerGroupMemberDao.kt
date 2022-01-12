package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.LearnerGroupMemberWithPerson

@Repository
@Dao
abstract class LearnerGroupMemberDao : BaseDao<LearnerGroupMember> {

    @Query("""SELECT LearnerGroupMember.*, Person.* FROM LearnerGroupMember 
        LEFT JOIN Person ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid 
        LEFT JOIN GroupLearningSession ON 
    GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid 
    WHERE GroupLearningSession.groupLearningSessionLearnerGroupUid = :learnerGroupUid 
    AND GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid 
    ORDER BY learnerGroupMemberRole ASC
    """)
    abstract fun findLearnerGroupMembersByGroupIdAndEntry(learnerGroupUid: Long, contentEntryUid: Long): DoorDataSourceFactory<Int, LearnerGroupMemberWithPerson>

    @Query("""SELECT LearnerGroupMember.*, Person.* FROM LearnerGroupMember 
        LEFT JOIN Person ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid 
        LEFT JOIN GroupLearningSession ON 
    GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid 
    WHERE GroupLearningSession.groupLearningSessionLearnerGroupUid = :learnerGroupUid 
    AND GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid 
    ORDER BY learnerGroupMemberRole ASC
    """)
    abstract suspend fun findLearnerGroupMembersByGroupIdAndEntryList(learnerGroupUid: Long, contentEntryUid: Long): List<LearnerGroupMemberWithPerson>

}