package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED

@Repository
@Dao
abstract class ClazzEnrolmentDao : BaseDao<ClazzEnrolment> {

    @Query("""
     REPLACE INTO ClazzEnrolmentReplicate(cePk, ceDestination)
      SELECT DISTINCT ClazzEnrolment.clazzEnrolmentUid AS ceUid,
             :newNodeId AS ceDestination
        FROM UserSession
             JOIN PersonGroupMember 
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2} 
             JOIN ClazzEnrolment 
                   ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND ClazzEnrolment.clazzEnrolmentLct != COALESCE(
             (SELECT ceVersionId
                FROM ClazzEnrolmentReplicate
               WHERE cePk = ClazzEnrolment.clazzEnrolmentUid
                 AND ceDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
             SET cePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzEnrolment::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

     @Query("""
 REPLACE INTO ClazzEnrolmentReplicate(cePk, ceDestination)
  SELECT DISTINCT ClazzEnrolment.clazzEnrolmentUid AS ceUid,
         UserSession.usClientNodeId AS ceDestination
    FROM ChangeLog
         JOIN ClazzEnrolment
             ON ChangeLog.chTableId = ${ClazzEnrolment.TABLE_ID}
                AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
         JOIN Clazz
             ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
             ${Role.PERMISSION_CLAZZ_SELECT}
             ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzEnrolment.clazzEnrolmentLct != COALESCE(
         (SELECT ceVersionId
            FROM ClazzEnrolmentReplicate
           WHERE cePk = ClazzEnrolment.clazzEnrolmentUid
             AND ceDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
     SET cePending = true
  */               
    """)
    @ReplicationRunOnChange([ClazzEnrolment::class])
    @ReplicationCheckPendingNotificationsFor([ClazzEnrolment::class])
    abstract suspend fun replicateClazzEnrolmentOnChange()

    /**
     * Note: When actually enroling into a class, use UmAppDatbaseExt#processEnrolmentIntoClass
     * to ensure that permissions, group membership, etc. are taken care of
     */
    @Insert
    abstract fun insertListAsync(entityList: List<ClazzEnrolment>)

    open suspend fun updateDateLeft(clazzEnrolmentUidList: List<Long>, endDate: Long) {
        val updateTime = systemTimeInMillis()
        clazzEnrolmentUidList.forEach {
            updateDateLeftByUid(it, endDate, updateTime)
        }
    }

    @Query("""SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentPersonUid = :personUid 
        AND clazzEnrolmentClazzUid = :clazzUid 
        AND clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS} LIMIT 1""")
    abstract suspend fun findByPersonUidAndClazzUidAsync(personUid: Long, clazzUid: Long): ClazzEnrolment?

    @Query("""SELECT ClazzEnrolment.*, LeavingReason.*, 
         COALESCE(Clazz.clazzTimeZone, COALESCE(School.schoolTimeZone, 'UTC')) as timeZone
         FROM ClazzEnrolment LEFT JOIN
        LeavingReason ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
        LEFT JOIN Clazz ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
        LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid
        WHERE clazzEnrolmentPersonUid = :personUid 
        AND ClazzEnrolment.clazzEnrolmentActive 
        AND clazzEnrolmentClazzUid = :clazzUid ORDER BY clazzEnrolmentDateLeft DESC""")
    abstract fun findAllEnrolmentsByPersonAndClazzUid(personUid: Long, clazzUid: Long):
            DoorDataSourceFactory<Int, ClazzEnrolmentWithLeavingReason>

    @Query("""SELECT ClazzEnrolment.*, LeavingReason.*,
         COALESCE(Clazz.clazzTimeZone, COALESCE(School.schoolTimeZone, 'UTC')) as timeZone
         FROM ClazzEnrolment LEFT JOIN
        LeavingReason ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
        LEFT JOIN Clazz ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
        LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid
        WHERE ClazzEnrolment.clazzEnrolmentUid = :enrolmentUid""")
    abstract suspend fun findEnrolmentWithLeavingReason(enrolmentUid: Long): ClazzEnrolmentWithLeavingReason?

    @Query("""
        UPDATE ClazzEnrolment 
          SET clazzEnrolmentDateLeft = :endDate,
              clazzEnrolmentLct = :updateTime
        WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract suspend fun updateDateLeftByUid(clazzEnrolmentUid: Long, endDate: Long, updateTime: Long)

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
    @Query("""SELECT ClazzEnrolment.*, Clazz.*, (SELECT ((CAST(COUNT(DISTINCT CASE WHEN 
        ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED THEN 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) AS REAL) / 
        MAX(COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid),1)) * 100) 
        FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog ON 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid WHERE 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = :personUid 
        AND ClazzLog.clazzLogClazzUid = Clazz.clazzUid AND ClazzLog.logDate 
        BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft) 
        as attendance
        FROM ClazzEnrolment
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid
        AND ClazzEnrolment.clazzEnrolmentActive
        ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
    """)
    @PostgresQuery("""SELECT ClazzEnrolment.*, Clazz.*, (SELECT ((CAST(COUNT(DISTINCT CASE WHEN 
        ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED THEN 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ELSE NULL END) AS REAL) / 
        GREATEST(COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid),1)) * 100) 
        FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog ON 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid WHERE 
        ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = :personUid 
        AND ClazzLog.clazzLogClazzUid = Clazz.clazzUid AND ClazzLog.logDate 
        BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft) 
        as attendance
        FROM ClazzEnrolment
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid
        AND ClazzEnrolment.clazzEnrolmentActive
        ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
    """)
    abstract fun findAllClazzesByPersonWithClazz(personUid: Long): DoorDataSourceFactory<Int, ClazzEnrolmentWithClazzAndAttendance>

    @Query("""SELECT COALESCE(MAX(clazzEnrolmentDateLeft),0) FROM ClazzEnrolment WHERE 
        ClazzEnrolment.clazzEnrolmentPersonUid = :selectedPerson 
        AND ClazzEnrolment.clazzEnrolmentActive 
        AND clazzEnrolmentClazzUid = :selectedClazz AND clazzEnrolmentUid != :selectedEnrolment
    """)
    abstract suspend fun findMaxEndDateForEnrolment(selectedClazz: Long, selectedPerson: Long,
                                            selectedEnrolment: Long): Long

    @Query("""SELECT ClazzEnrolment.*, Clazz.* 
        FROM ClazzEnrolment 
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid 
        AND ClazzEnrolment.clazzEnrolmentActive
        ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
    """)
    abstract suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long): List<ClazzEnrolmentWithClazz>

    @Query("""
        SELECT ClazzEnrolment.*, Person.*
          FROM ClazzEnrolment
    LEFT JOIN Person ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
              AND :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
              AND ClazzEnrolment.clazzEnrolmentDateLeft
              AND CAST(clazzEnrolmentActive AS INTEGER) = 1
              AND (:roleFilter = 0 OR ClazzEnrolment.clazzEnrolmentRole = :roleFilter)
              AND (:personUidFilter = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :personUidFilter)
    """)
    abstract suspend fun getAllClazzEnrolledAtTimeAsync(clazzUid: Long, date: Long, roleFilter: Int,
        personUidFilter: Long = 0): List<ClazzEnrolmentWithPerson>

    @Query("SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentUid = :uid")
    abstract suspend fun findByUid(uid: Long): ClazzEnrolment?

    @Query("SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzEnrolment?>

    @Query("""
                UPDATE ClazzEnrolment
                   SET clazzEnrolmentActive = :active,
                       clazzEnrolmentLct= :changeTime
                WHERE clazzEnrolmentPersonUid = :personUid 
                      AND clazzEnrolmentClazzUid = :clazzUid
                      AND clazzEnrolmentRole = :roleId""")
    abstract suspend fun updateClazzEnrolmentActiveForPersonAndClazz(
        personUid: Long,
        clazzUid: Long,
        roleId: Int,
        active: Boolean,
        changeTime: Long
    ): Int


    @Query("""
        SELECT Person.*, 
               (SELECT ((CAST(COUNT(DISTINCT 
                        CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = $STATUS_ATTENDED 
                                  THEN ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid 
                             ELSE NULL 
                             END) 
                        AS REAL) / 
                        MAX(COUNT(ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid),1)) * 100) 
                   FROM ClazzLogAttendanceRecord 
                        JOIN ClazzLog 
                             ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid 
                  WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid = Person.personUid 
                    AND ClazzLog.clazzLogClazzUid = :clazzUid)  AS attendance, 
        
    	       (SELECT MIN(ClazzEnrolment.clazzEnrolmentDateJoined) 
                  FROM ClazzEnrolment 
                 WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS earliestJoinDate, 
        
    	      (SELECT MAX(ClazzEnrolment.clazzEnrolmentDateLeft) 
                 FROM ClazzEnrolment 
                WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS latestDateLeft, 
        
              (SELECT clazzEnrolmentRole 
                 FROM clazzEnrolment 
                WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid 
                  AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
        AND ClazzEnrolment.clazzEnrolmentActive) AS enrolmentRole
        FROM PersonGroupMember
        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2} 
        
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
           AND PersonGroupMember.groupMemberActive 
           AND Person.personUid IN (SELECT clazzEnrolmentPersonUid 
                                      FROM ClazzEnrolment 
                                     WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                                       AND ClazzEnrolment.clazzEnrolmentActive 
                                       AND ClazzEnrolment.clazzEnrolmentRole = :roleId 
                                       AND (:filter != $FILTER_ACTIVE_ONLY 
                                        OR (:currentTime 
                                            BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                            AND ClazzEnrolment.clazzEnrolmentDateLeft))) 
          AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
     GROUP BY Person.personUid
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
    @QueryLiveTables(value = ["Clazz", "Person", "ClazzEnrolment", "PersonGroupMember", "ScopedGrant"])
    @SqliteOnly
    abstract fun findByClazzUidAndRole(clazzUid: Long, roleId: Int, sortOrder: Int, searchText: String? = "%",
                                       filter: Int, accountPersonUid: Long, currentTime: Long): DoorDataSourceFactory<Int, PersonWithClazzEnrolmentDetails>

    @Query("""
        UPDATE ClazzEnrolment 
          SET clazzEnrolmentActive = :enrolled,
              clazzEnrolmentLct = :timeChanged
        WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract fun updateClazzEnrolmentActiveForClazzEnrolment(
        clazzEnrolmentUid: Long,
        enrolled: Boolean,
        timeChanged: Long,
    ): Int

    @Query("""
            UPDATE ClazzEnrolment 
               SET clazzEnrolmentRole = :newRole,
                   clazzEnrolmentLct = :updateTime      
             -- Avoid potential for duplicate approvals if user was previously refused      
             WHERE clazzEnrolmentUid = COALESCE( 
                    (SELECT clazzEnrolmentUid
                       FROM ClazzEnrolment
                      WHERE clazzEnrolmentPersonUid = :personUid 
                            AND clazzEnrolmentClazzUid = :clazzUid
                            AND clazzEnrolmentRole = :oldRole
                            AND CAST(clazzEnrolmentActive AS INTEGER) = 1
                      LIMIT 1), 0)""")
    abstract suspend fun updateClazzEnrolmentRole(
        personUid: Long,
        clazzUid: Long,
        newRole: Int,
        oldRole: Int,
        updateTime: Long
    ): Int

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
