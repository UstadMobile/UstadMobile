package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("""
            SELECT ClazzAssignment.*, 
            
            
            FROM ClazzAssignment
        
        
            WHERE clazzAssignmentActive
            AND (ClazzAssignment.clazzAssignmentTitle LIKE :searchText 
                    OR ClazzAssignment.clazzAssignmentDescription :searchText)
            ORDER BY CASE(:sortOrder)
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.clazzAssignmentDeadlineDateTime
                WHEN $SORT_SCORE_ASC THEN resultScoreScaled
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.clazzAssignmentDeadlineDateTime
                WHEN $SORT_SCORE_DESC THEN resultScoreScaled
                ELSE 0
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_ASC THEN ClazzAssignment.clazzAssignmentTitle
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_DESC THEN ClazzAssignment.clazzAssignmentTitle
                ELSE ''
            END DESC
    """)
    abstract suspend fun getAllAssignments(clazzUid: Long,
                                   sortOrder: Int, searchText: String? = "%"):
            DataSource.Factory<Int, ClazzAssignmentWithMetrics>

    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE clazzAssignmentUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?

    companion object{

        const val SORT_DEADLINE_ASC = 1

        const val SORT_DEADLINE_DESC = 2

        const val SORT_TITLE_ASC = 3

        const val SORT_TITLE_DESC = 4

        const val SORT_SCORE_ASC = 5

        const val SORT_SCORE_DESC = 6

    }

}