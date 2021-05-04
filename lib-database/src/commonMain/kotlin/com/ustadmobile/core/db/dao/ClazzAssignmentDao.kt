package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("""
        SELECT ClazzAssignment.*, 
            0 AS notSubmittedStudents, 0 AS submittedStudents, 0 AS completedStudents,
            0 AS resultMax, 0 AS resultScore, 0 as contentComplete, 0 as progress
            
             FROM ClazzAssignment
                 
            WHERE caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_START_DATE_ASC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_ASC THEN (resultScore/resultMax)
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_START_DATE_DESC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_DESC THEN (resultScore/resultMax)
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
        SELECT COALESCE(SUM(ResultSource.resultScoreMax),0) AS resultMax, 
               COALESCE(SUM(ResultSource.resultScoreRaw),0) AS resultScore, 
               'FALSE' as contentComplete, 0 as progress
     	  FROM (SELECT StatementEntity.resultScoreRaw, StatementEntity.resultScoreMax
     	 	      FROM ClazzAssignmentContentJoin 
                         LEFT JOIN ContentEntry 
                         ON ContentEntry.contentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                       
                         LEFT JOIN StatementEntity 
                         ON statementUid = (SELECT statementUid 
                                              FROM StatementEntity 
                                                    LEFT JOIN ClazzAssignment 
                                                    ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                             WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
                                               AND caUid = :caUid
                                               AND statementPersonUid = :personUid
                                               AND contentEntryRoot 
                                               AND StatementEntity.timestamp 
                                                    BETWEEN ClazzAssignment.caStartDate
                                                        AND ClazzAssignment.caGracePeriodDate
                                         ORDER BY resultScoreScaled DESC LIMIT 1)
     	  ) AS ResultSource
    """)
    abstract suspend fun getStatementScoreProgressForAssignment(caUid: Long, personUid: Long): ContentEntryStatementScoreProgress?


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