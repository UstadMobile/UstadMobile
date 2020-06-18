package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ClazzWorkDao.Companion.STUDENT_PROGRESS_QUERY
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
        LEFT JOIN ClazzWorkSubmission ON ClazzWorkSubmission.clazzWorkSubmissionClazzMemberUid = ClazzMember.clazzMemberUid 
		WHERE ClazzWork.clazzWorkUid = :uid
    """)
    abstract suspend fun findWithSubmissionByUidAndPerson(uid: Long, personUid: Long): ClazzWorkWithSubmission?

    @Query("$FIND_WITH_METRICS_BY_CLAZZUID ORDER BY ClazzWork.clazzWorkTitle ASC")
    abstract fun findWithMetricsByClazzUidLiveAsc(clazzUid: Long): DataSource.Factory<Int,ClazzWorkWithMetrics>

    @Query("$FIND_WITH_METRICS_BY_CLAZZUID ORDER BY ClazzWork.clazzWorkTitle DESC")
    abstract fun findWithMetricsByClazzUidLiveDesc(clazzUid: Long): DataSource.Factory<Int,ClazzWorkWithMetrics>

    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract fun findClazzWorkWithMetricsByClazzWorkUidLive(clazzWorkUid: Long)
            : DoorLiveData<ClazzWorkWithMetrics?>

    @Query(FIND_CLAZZWORKWITHMETRICS_QUERY)
    abstract suspend fun findClazzWorkWithMetricsByClazzWorkUidAsync(clazzWorkUid: Long)
            : ClazzWorkWithMetrics?

    @Query(STUDENT_PROGRESS_QUERY)
    abstract fun findStudentProgressByClazzWork(clazzWorkUid: Long): DataSource.Factory<Int,
            ClazzMemberWithClazzWorkProgress>


    companion object{

        const val FIND_CLAZZWORKWITHMETRICS_QUERY = """
            SELECT ClazzWork.*, 
            0 as totalStudents, 
            0 as submittedStudents, 
            0 as notSubmittedStudents,
            0 as completedStudents, 
            0 as markedStudents,
            0 as firstContentEntryUid,
            Clazz.clazzTimeZone as clazzTimeZone
            FROM ClazzWork
            LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid
            WHERE ClazzWork.clazzWorkUid = :clazzWorkUid
            AND CAST(ClazzWork.clazzWorkActive AS INTEGER) = 1
        """

        const val FIND_WITH_METRICS_BY_CLAZZUID = """
            SELECT ClazzWork.*, 
             0 as totalStudents, 
             0 as submittedStudents, 
             0 as notSubmittedStudents,
             0 as completedStudents, 
             0 as markedStudents, 
             0 as firstContentEntryUid,
             Clazz.clazzTimeZone as clazzTimeZone 
             FROM ClazzWork 
             LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
             WHERE clazzWorkClazzUid = :clazzUid
            AND CAST(clazzWorkActive as INTEGER) = 1
        """

        const val STUDENT_PROGRESS_QUERY = """
            SELECT 
                Person.*, ClazzMember.*, cws.*, 
                0 as mProgress, cm.*
            FROM ClazzMember
                LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
                LEFT JOIN ClazzWork ON ClazzWork.clazzWorkUid = :clazzWorkUid
                LEFT JOIN Clazz ON Clazz.clazzUid = ClazzWork.clazzWorkClazzUid 
                LEFT JOIN Comments AS cm ON cm.commentsUid = (
                    SELECT Comments.commentsUid FROM Comments WHERE
                    Comments.commentsEntityType = ${ClazzWork.CLAZZ_WORK_TABLE_ID}
                    AND commentsEntityUid = ClazzWork.clazzWorkUid 
                    AND CAST(commentsInActive AS INTEGER) = 1 
                    AND CAST(commentsPublic AS INTEGER) = 1 
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
