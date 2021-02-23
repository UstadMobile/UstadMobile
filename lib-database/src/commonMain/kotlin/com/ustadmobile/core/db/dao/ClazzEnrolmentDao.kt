package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED

@UmDao(inheritPermissionFrom = ClazzDao::class, inheritPermissionForeignKey = "clazzEnrolmentClazzUid",
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@Repository
@Dao
abstract class ClazzEnrolmentDao : BaseDao<ClazzEnrolment> {

    @Insert
    abstract fun insertListAsync(entityList: List<ClazzEnrolment>)

    open suspend fun updateDateLeft(clazzEnrolmentUidList: List<Long>, endDate: Long) {
        clazzEnrolmentUidList.forEach {
            updateDateLeftByUid(it, endDate)
        }
    }

    @Query("""SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = :personUid 
        AND clazzEnrolmentClazzUid = :clazzUid 
        AND clazzEnrolmentStatus = ${ClazzEnrolment.STATUS_ENROLED} LIMIT 1""")
    abstract suspend fun findByPersonUidAndClazzUidAsync(personUid: Long, clazzUid: Long): ClazzEnrolment?

    @Query("""SELECT ClazzEnrolment.*, LeavingReason.* FROM ClazzEnrolment LEFT JOIN
        LeavingReason ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
        WHERE clazzEnrolmentPersonUid = :personUid 
        AND clazzEnrolmentClazzUid = :clazzUid ORDER BY clazzEnrolmentDateLeft DESC""")
    abstract fun findAllEnrolmentsByPersonAndClazzUid(personUid: Long, clazzUid: Long):
            DataSource.Factory<Int, ClazzEnrolmentWithLeavingReason>

    @Query("""SELECT ClazzEnrolment.*, LeavingReason.* FROM ClazzEnrolment LEFT JOIN
        LeavingReason ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
        WHERE ClazzEnrolment.clazzEnrolmentUid = :enrolmentUid""")
    abstract suspend fun findEnrolmentWithLeavingReason(enrolmentUid: Long): ClazzEnrolmentWithLeavingReason?

    @Query("""UPDATE ClazzEnrolment SET clazzEnrolmentDateLeft = :endDate,
            clazzEnrolmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
            WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract suspend fun updateDateLeftByUid(clazzEnrolmentUid: Long, endDate: Long)

    @Update
    abstract suspend fun updateAsync(entity: ClazzEnrolment): Int

    /**
     * Provide a list of the classes a given person is in with the class information itself (e.g.
     * for person detail).
     *
     * @param personUid
     * @param date If this is not 0, then the query will ensure that the registration is current at
     * the given
     */
    @Query("""SELECT ClazzEnrolment.*, Clazz.* 
        FROM ClazzEnrolment
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft)
    """)
    abstract fun findAllClazzesByPersonWithClazz(personUid: Long, date: Long): DataSource.Factory<Int, ClazzEnrolmentWithClazz>

    @Query("""SELECT COALESCE(MAX(clazzEnrolmentDateLeft),0) FROM ClazzEnrolment WHERE 
        ClazzEnrolment.clazzEnrolmentPersonUid = :selectedPerson 
        AND clazzEnrolmentClazzUid = :selectedClazz AND clazzEnrolmentUid != :selectedEnrolment
    """)
    abstract suspend fun findMaxEndDateForEnrolment(selectedClazz: Long, selectedPerson: Long,
                                            selectedEnrolment: Long): Long

    @Query("""SELECT ClazzEnrolment.*, Clazz.* 
        FROM ClazzEnrolment
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft)
    """)
    abstract suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long, date: Long): List<ClazzEnrolmentWithClazz>

    @Query("""SELECT ClazzEnrolment.*, Person.*
        FROM ClazzEnrolment
        LEFT JOIN Person ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
        AND :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft
        AND (:roleFilter = 0 OR ClazzEnrolment.clazzEnrolmentRole = :roleFilter)
    """)
    abstract suspend fun getAllClazzEnrolledAtTimeAsync(clazzUid: Long, date: Long, roleFilter: Int): List<ClazzEnrolmentWithPerson>

    @Query("SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentUid = :uid")
    abstract suspend fun findByUid(uid: Long): ClazzEnrolment?

    @Query("""SELECT Person.*, (SELECT ((CAST(COUNT(DISTINCT CASE WHEN 
        ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED THEN 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) AS REAL) / 
        COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid)) * 100) 
        FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog ON 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid WHERE 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid 
        AND ClazzLog.clazzLogClazzUid = :clazzUid)  as attendance, 
    	(SELECT MIN(ClazzEnrolment.clazzEnrolmentDateJoined) FROM ClazzEnrolment WHERE 
        Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) as earliestJoinDate, 
    	(SELECT MAX(ClazzEnrolment.clazzEnrolmentDateLeft) FROM ClazzEnrolment WHERE 
        Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) as latestDateLeft, 
        (SELECT clazzEnrolmentRole FROM clazzEnrolment WHERE Person.personUid = 
        ClazzEnrolment.clazzEnrolmentPersonUid AND 
        ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid) as enrolmentRole
        FROM PERSON 
        WHERE Person.personUid IN (SELECT clazzEnrolmentPersonUid FROM ClazzEnrolment 
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid AND ClazzEnrolment.clazzEnrolmentActive 
        AND ClazzEnrolment.clazzEnrolmentRole = :roleId AND (:filter != $FILTER_ACTIVE_ONLY 
        OR (:currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft))) 
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
                WHEN $SORT_ATTENDANCE_ASC THEN attendance
                WHEN $SORT_DATE_REGISTERED_ASC THEN earliestJoinDate
                WHEN $SORT_DATE_LEFT_ASC THEN latestDateLeft
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_ATTENDANCE_DESC THEN attendance
                WHEN $SORT_DATE_REGISTERED_DESC THEN earliestJoinDate
                WHEN $SORT_DATE_LEFT_DESC THEN latestDateLeft
                ELSE 0
            END DESC
    """)
    abstract fun findByClazzUidAndRole(clazzUid: Long, roleId: Int, sortOrder: Int, searchText: String? = "%",
                                       filter: Int, currentTime: Long): DataSource.Factory<Int, PersonWithClazzEnrolmentDetails>


    @Query("""UPDATE ClazzEnrolment SET clazzEnrolmentActive = :enrolled,
                clazzEnrolmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
                WHERE clazzEnrolmentPersonUid = :personUid AND clazzEnrolmentClazzUid = :clazzUid""")
    abstract fun updateClazzEnrolmentActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Int): Int

    fun updateClazzEnrolmentActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzEnrolmentActiveForPersonAndClazz(personUid, clazzUid, 1)
        } else {
            updateClazzEnrolmentActiveForPersonAndClazz(personUid, clazzUid, 0)
        }
    }

    @Query("""UPDATE ClazzEnrolment SET clazzEnrolmentActive = :enrolled,
            clazzEnrolmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract fun updateClazzEnrolmentActiveForClazzEnrolment(clazzEnrolmentUid: Long, enrolled: Int): Int

    fun updateClazzEnrolmentActiveForClazzEnrolment(clazzEnrolmentUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzEnrolmentActiveForClazzEnrolment(clazzEnrolmentUid, 1)
        } else {
            updateClazzEnrolmentActiveForClazzEnrolment(clazzEnrolmentUid, 0)
        }
    }

    @Query("""UPDATE ClazzEnrolment SET clazzEnrolmentRole = :role,
            clazzEnrolmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE clazzEnrolmentPersonUid = :personUid AND clazzEnrolmentClazzUid = :clazzUid""")
    abstract fun updateClazzEnrolmentRole(personUid: Long, clazzUid: Long, role: Int)

    companion object {

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

        const val SORT_ATTENDANCE_ASC = 5

        const val SORT_ATTENDANCE_DESC = 6

        const val SORT_DATE_REGISTERED_ASC = 7

        const val SORT_DATE_REGISTERED_DESC = 8

        const val SORT_DATE_LEFT_ASC = 9

        const val SORT_DATE_LEFT_DESC = 10

        const val FILTER_ACTIVE_ONLY = 1

    }
}
