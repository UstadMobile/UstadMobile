package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class ClazzWorkDao : BaseDao<ClazzWork> {

    @Query("SELECT * FROM ClazzWork WHERE clazzWorkUid = :clazzWorkUid " +
            " AND CAST(clazzWorkActive AS INTEGER) = 1")
    abstract suspend fun findByUidAsync(clazzWorkUid: Long): ClazzWork?

    @Update
    abstract suspend fun updateAsync(entity: ClazzWork): Int

    @Query("""
        SELECT ClazzWork.*, ClazzWorkSubmission.* FROM ClazzWork 
        LEFT JOIN ClazzEnrollment ON ClazzEnrollment.clazzEnrollmentPersonUid = :personUid
			AND ClazzEnrollment.clazzEnrollmentClazzUid = ClazzWork.clazzWorkClazzUid 
			AND CAST(ClazzEnrollment.clazzEnrollmentActive AS INTEGER) = 1
        LEFT JOIN ClazzWorkSubmission ON 
            ClazzWorkSubmission.clazzWorkSubmissionClazzEnrollmentUid = ClazzEnrollment.clazzEnrollmentUid
             AND ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
		WHERE ClazzWork.clazzWorkUid = :uid 
        ORDER BY ClazzWorkSubmission.clazzWorkSubmissionDateTimeStarted DESC LIMIT 1
    """)
    abstract suspend fun findWithSubmissionByUidAndPerson(uid: Long, personUid: Long): ClazzWorkWithSubmission?

    @Query("""
            SELECT ClazzWork.*, 
            
            (
                SELECT COUNT(*) FROM ClazzEnrollment WHERE ClazzEnrollment.clazzEnrollmentClazzUid = Clazz.clazzUid 
                AND CAST(ClazzEnrollment.clazzEnrollmentActive AS INTEGER) = 1 
                AND ClazzEnrollment.clazzEnrollmentRole = ${ClazzEnrollment.ROLE_STUDENT} 
            ) as totalStudents, 
            (
                SELECT COUNT(*) FROM ClazzWorkSubmission WHERE 
                clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
            ) as submittedStudents, 
            0 as notSubmittedStudents,
            0 as completedStudents, 
            (
                SELECT COUNT(*) FROM ClazzWorkSubmission WHERE 
                ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                AND ClazzWorkSubmission.clazzWorkSubmissionDateTimeMarked > 0
            ) as markedStudents,
             0 as firstContentEntryUid,
             Clazz.clazzTimeZone as clazzTimeZone 
             FROM ClazzWork 
             LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
             WHERE clazzWorkClazzUid = :clazzUid
             AND (:role = ${ClazzEnrollment.ROLE_TEACHER} OR clazzWorkStartDateTime < :today)
            AND CAST(clazzWorkActive as INTEGER) = 1 
            AND ClazzWork.clazzWorkTitle LIKE :searchText 
            ORDER BY CASE(:sortOrder)
                WHEN $SORT_DEADLINE_ASC THEN ClazzWork.clazzWorkDueDateTime
                WHEN $SORT_VISIBLE_FROM_ASC THEN ClazzWork.clazzWorkStartDateTime
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_DEADLINE_DESC THEN ClazzWork.clazzWorkDueDateTime
                WHEN $SORT_VISIBLE_FROM_DESC THEN ClazzWork.clazzWorkStartDateTime
                ELSE 0
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_ASC THEN ClazzWork.clazzWorkTitle
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_DESC THEN ClazzWork.clazzWorkTitle
                ELSE ''
            END DESC
        """
    )
    abstract fun findWithMetricsByClazzUidLive(clazzUid: Long, role: Int, today: Long, sortOrder: Int, searchText: String? = "%")
            : DataSource.Factory<Int, ClazzWorkWithMetrics>


    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract suspend fun findClazzWorkWithMetricsByClazzWorkUidAsync(clazzWorkUid: Long)
            : ClazzWorkWithMetrics?

    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract fun findClazzWorkWithMetricsByClazzWorkUid(clazzWorkUid: Long)
            : DataSource.Factory<Int, ClazzWorkWithMetrics>?

    @Query(STUDENT_PROGRESS_QUERY)
    abstract fun findStudentProgressByClazzWork(clazzWorkUid: Long, sortOrder: Int, searchText: String? = "%"): DataSource.Factory<Int,
            ClazzEnrollmentWithClazzWorkProgress>

    @Query(STUDENT_PROGRESS_QUERY)
    abstract suspend fun findStudentProgressByClazzWorkTest(clazzWorkUid: Long, sortOrder: Int, searchText: String? = "%"): List<ClazzEnrollmentWithClazzWorkProgress>

    @Query(FIND_CLAZZENROLLMENT_AND_SUBMISSION_WITH_PERSON)
    abstract suspend fun findClazzEnrollmentWithAndSubmissionWithPerson(clazzWorkUid: Long,
                                                                        clazzEnrollmentUid: Long): ClazzEnrollmentAndClazzWorkWithSubmission?

    @Query("SELECT * FROM ClazzWork")
    abstract suspend fun findAllTesting(): List<ClazzWork>

    companion object {

        const val SORT_DEADLINE_ASC = 1

        const val SORT_DEADLINE_DESC = 2

        const val SORT_VISIBLE_FROM_ASC = 3

        const val SORT_VISIBLE_FROM_DESC = 4

        const val SORT_TITLE_ASC = 5

        const val SORT_TITLE_DESC = 6

        const val SORT_FIRST_NAME_ASC = 7

        const val SORT_FIRST_NAME_DESC = 8

        const val SORT_LAST_NAME_ASC = 9

        const val SORT_LAST_NAME_DESC = 10

        const val SORT_CONTENT_PROGRESS_ASC = 11

        const val SORT_CONTENT_PROGRESS_DESC = 12

        const val SORT_STATUS_ASC = 13

        const val SORT_STATUS_DESC = 14


        const val FIND_CLAZZWORKWITHMETRICS_QUERY = """
            SELECT ClazzWork.*, 
            (
                SELECT COUNT(*) FROM ClazzEnrollment WHERE 
                ClazzEnrollment.clazzEnrollmentClazzUid = Clazz.clazzUid 
                AND CAST(ClazzEnrollment.clazzEnrollmentActive AS INTEGER) = 1 
                AND ClazzEnrollment.clazzEnrollmentRole = ${ClazzEnrollment.ROLE_STUDENT} 
            ) as totalStudents, 
            (
                SELECT COUNT(DISTINCT clazzWorkSubmissionClazzEnrollmentUid) FROM ClazzWorkSubmission WHERE
                clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
            ) as submittedStudents, 
            0 as notSubmittedStudents,
            0 as completedStudents, 
            (
                SELECT COUNT(DISTINCT clazzWorkSubmissionClazzEnrollmentUid) FROM ClazzWorkSubmission WHERE 
                ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                AND ClazzWorkSubmission.clazzWorkSubmissionDateTimeMarked > 0
            ) as markedStudents,
            0 as firstContentEntryUid,
            Clazz.clazzTimeZone as clazzTimeZone
            FROM ClazzWork
            LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid
            WHERE ClazzWork.clazzWorkUid = :clazzWorkUid
            AND CAST(ClazzWork.clazzWorkActive AS INTEGER) = 1
        """


        const val FIND_CLAZZENROLLMENT_AND_SUBMISSION_WITH_PERSON =
                """
            SELECT ClazzWork.*, ClazzWorkSubmission.*, ClazzEnrollment.*, Person.*
             FROM ClazzWork
            LEFT JOIN ClazzEnrollment ON ClazzEnrollment.clazzEnrollmentUid = :clazzEnrollmentUid
            LEFT JOIN Person ON Person.personUid = ClazzEnrollment.clazzEnrollmentPersonUid 
            LEFT JOIN ClazzWorkSubmission ON ClazzWorkSubmission.clazzWorkSubmissionUid = 
                (
                SELECT ClazzWorkSubmission.clazzWorkSubmissionUid FROM ClazzWorkSubmission 
                WHERE ClazzWorkSubmission.clazzWorkSubmissionClazzEnrollmentUid = ClazzEnrollment.clazzEnrollmentUid
                AND CAST(ClazzWorkSubmission.clazzWorkSubmissionInactive AS INTEGER) = 0
                AND ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                ORDER BY ClazzWorkSubmission.clazzWorkSubmissionDateTimeStarted DESC LIMIT 1
                )
             LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
             WHERE clazzWorkUid = :clazzWorkUid
            AND CAST(clazzWorkActive as INTEGER) = 1
                """

        //Removed:
        //AND ContentEntryProgress.contentEntryProgressStatusFlag = ${ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED}
        const val STUDENT_PROGRESS_QUERY = """
            SELECT 
                Person.*, ClazzEnrollment.*, cws.*,
                (
                    (
                        SELECT SUM(ContentEntryProgress.contentEntryProgressProgress) 
                        FROM ContentEntryProgress WHERE
                        CAST(ContentEntryProgress.contentEntryProgressActive AS INTEGER) = 1
                        AND ContentEntryProgress.contentEntryProgressPersonUid = Person.personUid
                    ) 
                    /
                    (
                        SELECT COUNT(*) FROM ClazzWorkContentJoin WHERE 
                        ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid = ClazzWork.clazzWorkUid
                        AND CAST(clazzWorkContentJoinInactive AS INTEGER) = 0
                    )
    
                ) as mProgress,
            cm.*, 

            (SELECT CASE WHEN EXISTS (
                SELECT ClazzWorkContentJoin.* FROM ClazzWorkContentJoin
                LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzWorkContentJoinContentUid
                WHERE 
                    ClazzWorkContentJoin.clazzWorkContentJoinClazzWorkUid = :clazzWorkUid
                    AND CAST(ClazzWorkContentJoin.clazzWorkContentJoinInactive AS INTEGER) = 0
                    AND NOT ContentEntry.ceInactive
                    AND ContentEntry.publik 
                
                )
            THEN 1 ELSE 0 END) as clazzWorkHasContent

            
            FROM ClazzEnrollment
                LEFT JOIN Person ON ClazzEnrollment.clazzEnrollmentPersonUid = Person.personUid
                LEFT JOIN ClazzWork ON ClazzWork.clazzWorkUid = :clazzWorkUid
                LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
                LEFT JOIN Comments AS cm ON cm.commentsUid = (
                    SELECT Comments.commentsUid FROM Comments WHERE
                    Comments.commentsEntityType = ${ClazzWork.CLAZZ_WORK_TABLE_ID}
                    AND commentsEntityUid = ClazzWork.clazzWorkUid 
                    AND CAST(commentsInActive AS INTEGER) = 0 
                    AND CAST(commentsPublic AS INTEGER) = 0 
                    AND Comments.commentsPersonUid = Person.personUid
                    ORDER BY commentsDateTimeAdded DESC LIMIT 1)
                LEFT JOIN ClazzWorkSubmission AS cws ON cws.clazzWorkSubmissionUid = 
                    (SELECT ClazzWorkSubmission.clazzWorkSubmissionUid FROM ClazzWorkSubmission WHERE
                    ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid 
                    AND ClazzWorkSubmission.clazzWorkSubmissionClazzEnrollmentUid = ClazzEnrollment.clazzEnrollmentUid
                    LIMIT 1)
            WHERE 
                    ClazzEnrollment.clazzEnrollmentClazzUid = Clazz.clazzUid
                    AND ClazzEnrollment.clazzEnrollmentRole = ${ClazzEnrollment.ROLE_STUDENT} 
                    AND Person.firstNames || ' ' || Person.lastName LIKE :searchText 
                    ORDER BY CASE(:sortOrder)
                        WHEN $SORT_FIRST_NAME_ASC THEN Person.firstNames
                        WHEN $SORT_LAST_NAME_ASC THEN Person.lastName
                        ELSE ''
                    END ASC,
                    CASE(:sortOrder)
                        WHEN $SORT_FIRST_NAME_DESC THEN Person.firstNames
                        WHEN $SORT_LAST_NAME_DESC THEN Person.lastName
                        ELSE ''
                    END DESC,
                    CASE(:sortOrder)
                        WHEN $SORT_CONTENT_PROGRESS_ASC THEN mProgress
                        ELSE 0
                    END ASC,
                    CASE(:sortOrder)
                        WHEN $SORT_CONTENT_PROGRESS_DESC THEN mProgress
                        ELSE 0
                    END DESC,
                   CASE(:sortOrder)
                        WHEN $SORT_STATUS_ASC THEN 
                        CASE WHEN (cws.clazzWorkSubmissionDateTimeMarked > 0) THEN cws.clazzWorkSubmissionDateTimeMarked
                        ELSE CASE WHEN (cws.clazzWorkSubmissionDateTimeFinished > 0) THEN 
                        cws.clazzWorkSubmissionDateTimeFinished ELSE mProgress 
                    END END END ASC,
                    CASE(:sortOrder)
                        WHEN $SORT_STATUS_DESC THEN 
                        CASE WHEN (cws.clazzWorkSubmissionDateTimeMarked > 0) THEN cws.clazzWorkSubmissionDateTimeMarked
                        ELSE CASE WHEN (cws.clazzWorkSubmissionDateTimeFinished > 0) THEN 
                        cws.clazzWorkSubmissionDateTimeFinished ELSE mProgress 
                        END END END DESC
        """
    }
}
