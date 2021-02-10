package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithClazz
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithPerson
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED

@UmDao(inheritPermissionFrom = ClazzDao::class, inheritPermissionForeignKey = "clazzEnrollmentClazzUid",
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@Repository
@Dao
abstract class ClazzEnrollmentDao : BaseDao<ClazzEnrollment> {

    @Insert
    abstract fun insertListAsync(entityList: List<ClazzEnrollment>)

    open suspend fun updateDateLeft(clazzEnrollmentUidList: List<Long>, endDate: Long) {
        clazzEnrollmentUidList.forEach {
            updateDateLeftByUid(it, endDate)
        }
    }

    @Query("SELECT * FROM ClazzEnrollment WHERE clazzEnrollmentPersonUid = :personUid " + "AND clazzEnrollmentClazzUid = :clazzUid")
    abstract suspend fun findByPersonUidAndClazzUidAsync(personUid: Long, clazzUid: Long): ClazzEnrollment?

    @Query("""UPDATE ClazzEnrollment SET clazzEnrollmentDateLeft = :endDate,
            clazzEnrollmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE clazzEnrollmentUid = :clazzEnrollmentUid""")
    abstract suspend fun updateDateLeftByUid(clazzEnrollmentUid: Long, endDate: Long)

    @Update
    abstract suspend fun updateAsync(entity: ClazzEnrollment): Int

    /**
     * Provide a list of the classes a given person is in with the class information itself (e.g.
     * for person detail).
     *
     * @param personUid
     * @param date If this is not 0, then the query will ensure that the registration is current at
     * the given
     */
    @Query("""SELECT ClazzEnrollment.*, Clazz.* 
        FROM ClazzEnrollment
        LEFT JOIN Clazz ON ClazzEnrollment.clazzEnrollmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrollment.clazzEnrollmentPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzEnrollment.clazzEnrollmentDateJoined AND ClazzEnrollment.clazzEnrollmentDateLeft)
    """)
    abstract fun findAllClazzesByPersonWithClazz(personUid: Long, date: Long): DataSource.Factory<Int, ClazzEnrollmentWithClazz>

    @Query("""SELECT ClazzEnrollment.*, Clazz.* 
        FROM ClazzEnrollment
        LEFT JOIN Clazz ON ClazzEnrollment.clazzEnrollmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrollment.clazzEnrollmentPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzEnrollment.clazzEnrollmentDateJoined AND ClazzEnrollment.clazzEnrollmentDateLeft)
    """)
    abstract suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long, date: Long): List<ClazzEnrollmentWithClazz>

    @Query("""SELECT ClazzEnrollment.*, Person.*
        FROM ClazzEnrollment
        LEFT JOIN Person ON ClazzEnrollment.clazzEnrollmentPersonUid = Person.personUid
        WHERE ClazzEnrollment.clazzEnrollmentClazzUid = :clazzUid
        AND :date BETWEEN ClazzEnrollment.clazzEnrollmentDateJoined AND ClazzEnrollment.clazzEnrollmentDateLeft
        AND (:roleFilter = 0 OR ClazzEnrollment.clazzEnrollmentRole = :roleFilter)
    """)
    abstract suspend fun getAllClazzEnrolledAtTimeAsync(clazzUid: Long, date: Long, roleFilter: Int): List<ClazzEnrollmentWithPerson>

    @Query("SELECT * FROM ClazzEnrollment WHERE clazzEnrollmentUid = :uid")
    abstract suspend fun findByUid(uid: Long): ClazzEnrollment?

    @Query("""SELECT Person.*, ((CAST(COUNT(DISTINCT CASE WHEN 
        ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED THEN 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) AS REAL) / 
        COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid)) * 100) as attendance, 
    	(SELECT MIN(ClazzEnrollment.clazzEnrollmentDateJoined) FROM ClazzEnrollment WHERE 
        Person.personUid = ClazzEnrollment.clazzEnrollmentPersonUid) as earliestJoinDate, 
    	(SELECT MAX(ClazzEnrollment.clazzEnrollmentDateLeft) FROM ClazzEnrollment WHERE 
        Person.personUid = ClazzEnrollment.clazzEnrollmentPersonUid) as latestDateLeft
        FROM PERSON 
        LEFT JOIN ClazzLogAttendanceRecord ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid
        WHERE Person.personUid IN (SELECT clazzEnrollmentPersonUid FROM ClazzEnrollment 
        WHERE ClazzEnrollment.clazzEnrollmentClazzUid = :clazzUid AND ClazzEnrollment.clazzEnrollmentActive 
        AND ClazzEnrollment.clazzEnrollmentRole = :roleId AND (:filter != $FILTER_ACTIVE_ONLY 
        OR (:currentTime BETWEEN ClazzEnrollment.clazzEnrollmentDateJoined AND ClazzEnrollment.clazzEnrollmentDateLeft))) 
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
                                       filter: Int, currentTime: Long): DataSource.Factory<Int, ClazzEnrollmentWithPerson>


    @Query("""UPDATE ClazzEnrollment SET clazzEnrollmentActive = :enrolled,
                clazzEnrollmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
                WHERE clazzEnrollmentPersonUid = :personUid AND clazzEnrollmentClazzUid = :clazzUid""")
    abstract fun updateClazzEnrollmentActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Int): Int

    fun updateClazzEnrollmentActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzEnrollmentActiveForPersonAndClazz(personUid, clazzUid, 1)
        } else {
            updateClazzEnrollmentActiveForPersonAndClazz(personUid, clazzUid, 0)
        }
    }

    @Query("""UPDATE ClazzEnrollment SET clazzEnrollmentActive = :enrolled,
            clazzEnrollmentLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE clazzEnrollmentUid = :clazzEnrollmentUid""")
    abstract fun updateClazzEnrollmentActiveForClazzEnrollment(clazzEnrollmentUid: Long, enrolled: Int): Int

    fun updateClazzEnrollmentActiveForClazzEnrollment(clazzEnrollmentUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzEnrollmentActiveForClazzEnrollment(clazzEnrollmentUid, 1)
        } else {
            updateClazzEnrollmentActiveForClazzEnrollment(clazzEnrollmentUid, 0)
        }
    }

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
