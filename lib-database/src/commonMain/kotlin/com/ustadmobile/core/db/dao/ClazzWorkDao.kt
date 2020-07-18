package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao
@UmRepository
@Dao
abstract class ClazzWorkDao : BaseDao<ClazzWork> {

    @Query("SELECT * FROM ClazzWork WHERE clazzWorkUid = :clazzWorkUid " +
            " AND CAST(clazzWorkActive AS INTEGER) = 1")
    abstract fun findByUidAsync(clazzWorkUid: Long): ClazzWork?

    @Update
    abstract suspend fun updateAsync(entity: ClazzWork) : Int

    @Query("""
        SELECT ClazzWork.*, ClazzWorkSubmission.* FROM ClazzWork 
        LEFT JOIN ClazzMember ON ClazzMember.clazzMemberPersonUid = :personUid
			AND ClazzMember.clazzMemberClazzUid = ClazzWork.clazzWorkClazzUid 
			AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1
        LEFT JOIN ClazzWorkSubmission ON 
            ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid = ClazzMember.clazzMemberUid
             AND ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
		WHERE ClazzWork.clazzWorkUid = :uid 
    """)
    abstract suspend fun findWithSubmissionByUidAndPerson(uid: Long, personUid: Long): ClazzWorkWithSubmission?

    @Query("$FIND_WITH_METRICS_BY_CLAZZUID ORDER BY ClazzWork.clazzWorkTitle ASC")
    abstract fun findWithMetricsByClazzUidLiveAsc(clazzUid: Long): DataSource.Factory<Int,ClazzWorkWithMetrics>

    @Query("$FIND_WITH_METRICS_BY_CLAZZUID ORDER BY ClazzWork.clazzWorkTitle DESC")
    abstract fun findWithMetricsByClazzUidLiveDesc(clazzUid: Long): DataSource.Factory<Int,ClazzWorkWithMetrics>

    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract suspend fun findClazzWorkWithMetricsByClazzWorkUidAsync(clazzWorkUid: Long)
            : ClazzWorkWithMetrics?

    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract fun findClazzWorkWithMetricsByClazzWorkUid(clazzWorkUid: Long)
            : DataSource.Factory<Int, ClazzWorkWithMetrics>?

    @Query(STUDENT_PROGRESS_QUERY)
    abstract fun findStudentProgressByClazzWork(clazzWorkUid: Long): DataSource.Factory<Int,
            ClazzMemberWithClazzWorkProgress>

    @Query(STUDENT_PROGRESS_QUERY)
    abstract fun findStudentProgressByClazzWorkTest(clazzWorkUid: Long): List<ClazzMemberWithClazzWorkProgress>

    @Query(FIND_CLAZZMEMBER_AND_SUBMISSION_WITH_PERSON)
    abstract suspend fun findClazzMemberWithAndSubmissionWithPerson(clazzWorkUid: Long,
                                        clazzMemberUid: Long): ClazzMemberAndClazzWorkWithSubmission?

    @Query("SELECT * FROM ClazzWork")
    abstract suspend fun findAllTesting(): List<ClazzWork>

    companion object{


        const val FIND_CLAZZWORKWITHMETRICS_QUERY = """
            SELECT ClazzWork.*, 
            (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid 
                AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 
                AND ClazzMember.clazzMemberRole = ${ClazzMember.ROLE_STUDENT} 
                ) as totalStudents, 
            (
                SELECT COUNT(*) FROM ( SELECT * FROM ClazzWorkSubmission WHERE 
                clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                GROUP BY ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid)
            ) as submittedStudents, 
            0 as notSubmittedStudents,
            0 as completedStudents, 
            (
                SELECT COUNT(*) FROM ( SELECT * FROM ClazzWorkSubmission WHERE 
                ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                AND ClazzWorkSubmission.clazzWorkSubmissionDateTimeMarked > 0
                GROUP BY ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid)
            ) as markedStudents,
            0 as firstContentEntryUid,
            Clazz.clazzTimeZone as clazzTimeZone
            FROM ClazzWork
            LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid
            WHERE ClazzWork.clazzWorkUid = :clazzWorkUid
            AND CAST(ClazzWork.clazzWorkActive AS INTEGER) = 1
        """


        const val FIND_WITH_METRICS_BY_CLAZZUID = """
            SELECT ClazzWork.*, 
            
            (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid 
                AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 
                AND ClazzMember.clazzMemberRole = ${ClazzMember.ROLE_STUDENT} 
                ) as totalStudents, 
            (
                SELECT COUNT(*) FROM ( SELECT * FROM ClazzWorkSubmission WHERE 
                clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                GROUP BY ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid)
            ) as submittedStudents, 
            0 as notSubmittedStudents,
            0 as completedStudents, 
            (
                SELECT COUNT(*) FROM ( SELECT * FROM ClazzWorkSubmission WHERE 
                ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                AND ClazzWorkSubmission.clazzWorkSubmissionDateTimeMarked > 0
                GROUP BY ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid)
            ) as markedStudents,

             0 as firstContentEntryUid,
             Clazz.clazzTimeZone as clazzTimeZone 
             FROM ClazzWork 
             LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
             WHERE clazzWorkClazzUid = :clazzUid
            AND CAST(clazzWorkActive as INTEGER) = 1
        """

        const val FIND_CLAZZMEMBER_AND_SUBMISSION_WITH_PERSON =
                """
            SELECT ClazzWork.*, ClazzWorkSubmission.*, ClazzMember.*, Person.*
             FROM ClazzWork
            LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = :clazzMemberUid
            LEFT JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid 
            LEFT JOIN ClazzWorkSubmission ON ClazzWorkSubmission.clazzWorkSubmissionUid = 
                (
                SELECT ClazzWorkSubmission.clazzWorkSubmissionUid FROM ClazzWorkSubmission 
                WHERE ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid = ClazzMember.clazzMemberUid
                AND CAST(ClazzWorkSubmission.clazzWorkSubmissionInactive AS INTEGER) = 0
                AND ClazzWorkSubmission.clazzWorkSubmissionClazzWorkUid = ClazzWork.clazzWorkUid
                ORDER BY ClazzWorkSubmission.clazzWorkSubmissionDateTimeStarted DESC LIMIT 1
                )
             LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
             WHERE clazzWorkUid = :clazzWorkUid
            AND CAST(clazzWorkActive as INTEGER) = 1
                """

        const val STUDENT_PROGRESS_QUERY = """
            SELECT 
                Person.*, ClazzMember.*, cws.*,
                (
                    (
                        SELECT SUM(ContentEntryProgress.contentEntryProgressProgress) 
                        FROM ContentEntryProgress WHERE
                        CAST(ContentEntryProgress.contentEntryProgressActive AS INTEGER) = 1 
                        AND ContentEntryProgress.contentEntryProgressStatusFlag = ${ContentEntryProgress.CONTENT_ENTRY_PROGRESS_FLAG_COMPLETED}
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

            
            FROM ClazzMember
                LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
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
                    AND ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid = ClazzMember.clazzMemberUid
                    LIMIT 1)
            WHERE 
                    ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
                    AND ClazzMember.clazzMemberRole = ${ClazzMember.ROLE_STUDENT}
        """
    }
}
