package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("""
            SELECT ClazzAssignment.*, 
            0 AS notSubmittedStudents, 0 AS submittedStudents, 0 AS completedStudents,
            0 AS resultScoreScaled, 0 AS resultMax, 0 AS resultScore, 0 as completedContent
            
             FROM ClazzAssignment
        
        
            WHERE caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_START_DATE_ASC THEN ClazzAssignment.caStartDateTime
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.caDeadlineDateTime
                WHEN $SORT_SCORE_ASC THEN resultScoreScaled
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_START_DATE_DESC THEN ClazzAssignment.caStartDateTime
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.caDeadlineDateTime
                WHEN $SORT_SCORE_DESC THEN resultScoreScaled
                ELSE 0
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_ASC THEN ClazzAssignment.caTitle
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_DESC THEN ClazzAssignment.caTitle
                ELSE ''
            END DESC
    """)
    abstract fun getAllAssignments(clazzUid: Long,
                                   sortOrder: Int, searchText: String)
            : DataSource.Factory<Int, ClazzAssignmentWithMetrics>


    @Query("""
        SELECT  ClazzAssignment.*, 
         	  0 AS notSubmittedStudents, 0 AS submittedStudents, 0 AS completedStudents,
                 AVG(CASE WHEN contentEntryRoot 
                     THEN resultScoreScaled ELSE 0 END) AS resultScoreScaled,
                SUM(CASE WHEN contentEntryRoot 
                	THEN resultScoreRaw ELSE 0 END) AS resultScore, 
                SUM(CASE WHEN contentEntryRoot 
                     THEN resultScoreMax ELSE 0 END) AS resultMax,
                'FALSE' AS completedContent
         FROM ClazzAssignment
         	LEFT JOIN ClazzAssignmentContentJoin 
         	ON ClazzAssignmentContentJoin.cacjAssignmentUid = :caUid
        	LEFT JOIN StatementEntity
        	ON StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
       WHERE caActive
         AND ClazzAssignment.caClazzUid = :clazzUid
         AND StatementEntity.timestamp 
     		BETWEEN ClazzAssignment.caStartDateTime 
     		    AND ClazzAssignment.caGracePeriodDateTime
    """)
    abstract suspend fun getAssignmentMetrics(clazzUid: Long, caUid: Long): ClazzAssignmentWithMetrics?


    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?

    companion object{

        const val SORT_DEADLINE_ASC = 1

        const val SORT_DEADLINE_DESC = 2

        const val SORT_TITLE_ASC = 3

        const val SORT_TITLE_DESC = 4

        const val SORT_SCORE_ASC = 5

        const val SORT_SCORE_DESC = 6

        const val SORT_START_DATE_ASC = 7

        const val SORT_START_DATE_DESC = 8

    }

}