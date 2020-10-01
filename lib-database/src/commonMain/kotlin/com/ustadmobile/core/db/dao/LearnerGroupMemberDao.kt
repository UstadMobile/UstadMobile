package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.LearnerGroupMemberWithPerson

@UmRepository
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
    abstract fun findLearnerGroupMembersByGroupIdAndEntry(learnerGroupUid: Long, contentEntryUid: Long): DataSource.Factory<Int, LearnerGroupMemberWithPerson>

    @Query("""SELECT LearnerGroupMember.*, Person.* FROM LearnerGroupMember 
        LEFT JOIN Person ON LearnerGroupMember.learnerGroupMemberPersonUid = Person.personUid 
        LEFT JOIN GroupLearningSession ON 
    GroupLearningSession.groupLearningSessionLearnerGroupUid = LearnerGroupMember.learnerGroupMemberLgUid 
    WHERE GroupLearningSession.groupLearningSessionLearnerGroupUid = :learnerGroupUid 
    AND GroupLearningSession.groupLearningSessionContentUid = :contentEntryUid 
    ORDER BY learnerGroupMemberRole ASC
    """)
    abstract fun findLearnerGroupMembersByGroupIdAndEntryList(learnerGroupUid: Long, contentEntryUid: Long): List<LearnerGroupMemberWithPerson>

}