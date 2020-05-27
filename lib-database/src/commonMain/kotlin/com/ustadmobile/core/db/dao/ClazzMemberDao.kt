package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.util.getSystemTimeInMillis

@UmDao(inheritPermissionFrom = ClazzDao::class, inheritPermissionForeignKey = "clazzMemberClazzUid",
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@UmRepository
@Dao
abstract class ClazzMemberDao : BaseDao<ClazzMember> {

    @Insert
    abstract override fun insert(entity: ClazzMember): Long

    @Insert
    abstract fun insertListAsync(entityList: List<ClazzMember>)

    open fun updateDateLeft(clazzMemberUidList: List<Long>, endDate: Long) {
        clazzMemberUidList.forEach {
            updateDateLeftByUid(it, endDate)
        }
    }

    @Query("UPDATE ClazzMember SET clazzMemberDateLeft = :endDate WHERE clazzMemberUid = :clazzMemberUid")
    abstract fun updateDateLeftByUid(clazzMemberUid: Long, endDate: Long)

    @Update
    abstract override fun update(entity: ClazzMember)

    @Update
    abstract suspend fun updateAsync(entity: ClazzMember):Int

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        val auditLog = AuditLog(fromPersonUid, ClazzMember.TABLE_ID, toPersonUid)
        insertAuditLog(auditLog)

    }

    fun insertClazzMember(entity: ClazzMember, loggedInPersonUid: Long) {
        val personUid = insert(entity)
        createAuditLog(personUid, loggedInPersonUid)
    }

    fun updateClazzMember(entity: ClazzMember, loggedInPersonUid: Long) {
        update(entity)
        createAuditLog(entity.clazzMemberUid, loggedInPersonUid)
    }


    /**
     * Enrol the given person into the given class.
     */
    suspend fun enrolPersonIntoClazz(personToEnrol: Person, clazzUid: Long, role: Int): ClazzMemberWithPerson {
        val clazzMember = ClazzMemberWithPerson().apply {
            clazzMemberPersonUid = personToEnrol.personUid
            clazzMemberClazzUid = clazzUid
            clazzMemberRole = role
            clazzMemberActive = true
            clazzMemberDateJoined = getSystemTimeInMillis()
            person = personToEnrol
        }
        clazzMember.clazzMemberUid = insertAsync(clazzMember)
        return clazzMember
    }

    /**
     * Provide a list of the classes a given person is in with the class information itself (e.g.
     * for person detail).
     *
     * @param personUid
     * @param date If this is not 0, then the query will ensure that the registration is current at
     * the given
     */
    @Query("""SELECT ClazzMember.*, Clazz.* 
        FROM ClazzMember
        LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
        WHERE ClazzMember.clazzMemberPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft)
    """)
    abstract fun findAllClazzesByPersonWithClazz(personUid: Long, date: Long): DataSource.Factory<Int, ClazzMemberWithClazz>

    @Query("""SELECT ClazzMember.*, Clazz.* 
        FROM ClazzMember
        LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
        WHERE ClazzMember.clazzMemberPersonUid = :personUid
        AND (:date = 0 OR :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft)
    """)
    abstract fun findAllClazzesByPersonWithClazzAsList(personUid: Long, date: Long): List<ClazzMemberWithClazz>

    @Query("""SELECT ClazzMember.*, Person.*
        FROM ClazzMember
        LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
        WHERE ClazzMember.clazzMemberClazzUid = :clazzUid
        AND :date BETWEEN ClazzMember.clazzMemberDateJoined AND ClazzMember.clazzMemberDateLeft
        AND (:roleFilter = 0 OR ClazzMember.clazzMemberRole = :roleFilter)
    """)
    abstract fun getAllClazzMembersAtTime(clazzUid: Long, date: Long, roleFilter: Int) : List<ClazzMemberWithPerson>

    @Query("SELECT * FROM ClazzMember")
    abstract fun findAllAsList(): List<ClazzMember>

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    abstract fun findByUid(uid: Long): ClazzMember?

    @Query("""SELECT ClazzMember.*, Person.* FROM 
        ClazzMember
        LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
        WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberRole = :roleId
        ORDER BY Person.firstNames
    """)
    abstract fun findByClazzUidAndRole(clazzUid: Long, roleId: Int): DataSource.Factory<Int, ClazzMemberWithPerson>

    @Query("SELECT ClazzMember.* FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberRole = :roleId")
    abstract fun findMemberOnlyByClazzUidAndRoleAsList(clazzUid: Long, roleId: Int): List<ClazzMember>

    @Query("SELECT ClazzMember.*, Person.* FROM ClazzMember" +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = 1")
    abstract fun findClazzMembersByClazzId(uid: Long): DataSource.Factory<Int, ClazzMemberWithPerson>

    @Query("SELECT ClazzMember.*, Person.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = :role")
    abstract suspend fun findClazzMemberWithPersonByRoleForClazzUid(uid: Long, role: Int) :List<ClazzMemberWithPerson>

    @Query("SELECT ClazzMember.*, Person.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = :role")
    abstract fun findClazzMemberWithPersonByRoleForClazzUidSync(uid: Long, role: Int): List<ClazzMemberWithPerson>

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " + "AND clazzMemberClazzUid = :clazzUid")
    abstract fun findByPersonUidAndClazzUid(personUid: Long, clazzUid: Long): ClazzMember?

    @Query("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " + "AND clazzMemberClazzUid = :clazzUid")
    abstract suspend fun findByPersonUidAndClazzUidAsync(personUid: Long, clazzUid: Long)
            : ClazzMember?

    @Query("Update ClazzMember SET clazzMemberAttendancePercentage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = " +
            " ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = " +
            STATUS_ATTENDED + ") * 1.0 " +
            " / " +
            "MAX(1.0, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = " +
            "ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1) * 1.0) " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid ")
    abstract fun updateAttendancePercentages(clazzUid: Long)

    @Query("SELECT ClazzMember.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            "AND ClazzMember.clazzMemberRole = :role " +
            "AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findByClazzUid(uid: Long, role: Int): List<ClazzMember>

    @Query("SELECT ClazzMember.clazzMemberUid, Clazz.clazzName, Person.firstNames, " +
            "   Person.lastName, Person.personUid, " +
            "   (SELECT SUM(CASE attendanceStatus WHEN :type THEN 1 ELSE 0 END) FROM ClazzLogAttendanceRecord " +
            "    LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "    WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            "    ORDER BY ClazzLog.logDate DESC LIMIT :days) AS num " +
            " FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " WHERE clazzMemberClazzUid = :clazzUid" +
            " AND num = :days")
    abstract suspend fun findAllMembersForAttendanceOverConsecutiveDays(
            type: Int, days: Int, clazzUid: Long) :List<PersonNameWithClazzName>


    @Query("SELECT  " +
            " SUM(CASE WHEN attendancePercentage >  79 AND attendancePercentage < 101 THEN 1 ELSE 0 END) *100 / " +
            " (select COUNT(*) FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.clazzLogClazzUid = :clazzUid " +
            " AND ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 GROUP BY ClazzLogUid ORDER BY ClazzLog.logDate ASC LIMIT 1)   " +
            " AS high, " +
            " SUM(CASE WHEN attendancePercentage >  59 AND attendancePercentage < 80 THEN 1 ELSE 0 END) *100 / " +
            " (select COUNT(*) FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog on  " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.clazzLogClazzUid = :clazzUid  " +
            " AND ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime  " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 GROUP BY ClazzLogUid ORDER BY ClazzLog.logDate ASC LIMIT 1)    AS mid, " +
            " SUM(CASE WHEN attendancePercentage >  0 AND attendancePercentage < 60 THEN 1 ELSE 0 END) *100 / " +
            " (select COUNT(*) FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog on  " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.clazzLogClazzUid = :clazzUid " +
            " AND ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 GROUP BY ClazzLogUid ORDER BY ClazzLog.logDate ASC LIMIT 1)    AS low " +
            "  FROM ( " +
            " SELECT ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid, " +
            "  SUM(CASE attendanceStatus WHEN :type THEN 1 ELSE 0 END) * 100 / " +
            "  (select count(*) FROM ClazzLog WHERE ClazzLog.clazzLogClazzUid = :clazzUid " +
            "  AND ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime) AS attendancePercentage " +
            "  FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLog on " +
            "  ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "  LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            " WHERE ClazzLog.clazzLogClazzUid = :clazzUid  " +
            "  AND ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime  AND ClazzMember.clazzMemberRole = 1 " +
            " GROUP BY ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid ) ")
    @Deprecated("Used only in older code")
    abstract suspend fun findAttendanceSpreadByThresholdForTimePeriodAndClazzAndType(type: Int,
                    clazzUid: Long, fromTime: Long, toTime: Long): ThresholdResult?

    @Query("SELECT * FROM Person where personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = 1) AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findAllActivePeopleInClassUid(clazzUid: Long): DataSource.Factory<Int, Person>


    @Query("SELECT Person.*, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid " +
            " FROM Person where personUid IN ( " +
            "  SELECT Person.personUid FROM ClazzMember  " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND CAST(ClazzMember.clazzMemberActive AS INTEGER)= 1 " +
            " AND ClazzMember.clazzMemberRole = 1) AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findAllPeopleWithPersonPictureInClassUid(clazzUid: Long): DataSource.Factory<Int, PersonWithPersonPicture>


    @Query("SELECT Person.*, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid " +
            " FROM Person where personUid IN ( " +
            "  SELECT Person.personUid FROM ClazzMember  " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = 1 AND ClazzMember.clazzMemberUid != :currentClazzMemberUid)" +
            " AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findAllPeopleWithPersonPictureInClassUid2(
            clazzUid: Long, currentClazzMemberUid: Long): DataSource.Factory<Int, PersonWithPersonPicture>

    @Query("SELECT * FROM Person where personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = 1 AND ClazzMember.clazzMemberUid NOT IN (:notIn)) " +
            "AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findAllPeopleInClassUidExcept(clazzUid: Long, notIn: List<Long>): DataSource.Factory<Int, Person>


    @Query("SELECT * FROM Person where personUid NOT IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberRole = 1) " +
            " AND CAST(Person.active AS INTEGER) = 1 ")
    abstract fun findAllPeopleNotInClassUid(clazzUid: Long): DataSource.Factory<Int, Person>

    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            " AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid AND ClazzMember.clazzMemberRole = "
            + ClazzMember.ROLE_STUDENT + " ) AS enrolled FROM Person WHERE CAST(Person.active AS INTEGER) = 1 " +
            " ORDER BY Person.firstNames ASC")
    abstract fun findAllStudentsWithEnrollmentForClassUid(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>


    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid AND " +
            " clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            ":clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled" +
            " FROM Person " +
            " WHERE CAST(Person.active AS INTEGER) = 1 " +
            "  AND (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberPersonUid = " +
            "       Person.personUid AND ClazzMember.clazzMemberRole = 1) = 0 " +
            "ORDER BY Person.firstNames ASC")
    abstract fun findAllEligibleTeachersWithEnrollmentForClassUid(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>

    @Query("SELECT " +
            "  Person.* , " +
            "  (:clazzUid) AS clazzUid,  " +
            " '' AS clazzName, " +
            "  (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "    PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "    picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            "   ClazzMember.clazzMemberAttendancePercentage as attendancePercentage, " +
            "  (SELECT clazzMemberActive FROM ClazzMember " +
            "    WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "    AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            "   (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            "     :clazzUid AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            "FROM Person " +
            "LEFT JOIN ClazzMember ON ClazzMember.clazzMemberClazzUid = :clazzUid AND " +
            "   ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "WHERE personUid IN " +
            "   ( SELECT Person.personUid FROM ClazzMember  LEFT  JOIN Person" +
            "     On ClazzMember.clazzMemberPersonUid = Person.personUid WHERE " +
            "     ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "     AND ClazzMember.clazzMemberAttendancePercentage >= :apl  AND " +
            "     ClazzMember.clazzMemberAttendancePercentage <= :aph" +
            "     AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            "   ) " +
            "   AND CAST(Person.active AS INTEGER) = 1 " +
            "   AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery " +
            "ORDER BY clazzMemberRole ASC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(clazzUid: Long,
               apl: Float, aph: Float, searchQuery: String)
            : DataSource.Factory<Int, PersonWithEnrollment>


    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            ":clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " ) AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzMemberRole ASC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUid(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>


    //REPORT Query: At Risk Student Report
    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +

            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +

            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +

            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +

            " FROM Person " +

            " WHERE personUid IN ( " +
            "   SELECT Person.personUid FROM ClazzMember " +
            "   LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "   WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "       AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT + " " +
            "       AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            "   )" +
            " AND attendancePercentage < :riskThreshold " +
            " AND CAST(Person.active AS INTEGER) = 1 ORDER BY attendancePercentage DESC")
    abstract suspend fun findAllStudentsAtRiskForClazzUidAsync(clazzUid: Long,
                                                               riskThreshold:Float)
            :List<PersonWithEnrollment>

    @Query(AT_RISK_STUDENT_REPORT_QUERY)
    abstract fun findAllStudentsAtRiskForClazzList(clazzes: List<Long>,
                                                   riskThreshold: Float): DataSource.Factory<Int, PersonWithEnrollment>

    @Query(AT_RISK_STUDENT_REPORT_QUERY)
    abstract suspend fun findAllStudentsAtRiskForClazzListAsync(
            clazzes: List<Long>,riskThreshold: Float) : List<PersonWithEnrollment>


    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            ":clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " ) AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzMemberRole DESC, Person.firstNames ASC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>

    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            ":clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " ) AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzMemberRole DESC, attendancePercentage ASC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceAsc(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>

    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = " +
            ":clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " ) AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzMemberRole DESC, attendancePercentage DESC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceDesc(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>


    @Query("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " '' AS clazzName, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " ) AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzMemberRole DESC, Person.firstNames DESC")
    abstract fun findAllPersonWithEnrollmentInClazzByClazzUidSortByNameDesc(clazzUid: Long): DataSource.Factory<Int, PersonWithEnrollment>


    @Query("SELECT coalesce(AVG(clazzMemberAttendancePercentage),0) FROM ClazzMember " +
            " WHERE clazzMemberPersonUid = :personUid")
    abstract suspend fun getAverageAttendancePercentageByPersonUidAsync(personUid: Long): Float

    @Query("SELECT coalesce(AVG(clazzMemberAttendancePercentage),0) FROM ClazzMember " +
            " WHERE clazzMemberPersonUid = :personUid")
    abstract fun getAverageAttendancePercentageByPersonUidLive(personUid: Long): DoorLiveData<Float>

    @Query("SELECT " +
            " (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            " ) * 1.0 " +
            " / " +
            " MAX(1.0, (SELECT COUNT(*)  FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1" +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            ") * 1.0) as attended_average " +
            " FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT + " ")
    abstract suspend fun getAttendanceAverageAsListForClazzBetweenDates(clazzUid: Long,
                                                                fromDate: Long, toDate: Long): List<Float>


    @Query("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE " + "clazzMemberPersonUid = :personUid AND clazzMemberClazzUid = :clazzUid")
    abstract fun updateClazzMemberActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Int): Int

    fun updateClazzMemberActiveForPersonAndClazz(personUid: Long, clazzUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 1)
        } else {
            updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 0)
        }
    }

    @Query("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE clazzMemberUid = :clazzMemberUid")
    abstract fun updateClazzMemberActiveForClazzMember(clazzMemberUid: Long, enrolled: Int): Int

    fun updateClazzMemberActiveForClazzMember(clazzMemberUid: Long, enrolled: Boolean): Int {
        return if (enrolled) {
            updateClazzMemberActiveForClazzMember(clazzMemberUid, 1)
        } else {
            updateClazzMemberActiveForClazzMember(clazzMemberUid, 0)
        }
    }

    @Query("UPDATE ClazzMember SET clazzMemberActive = 0 WHERE clazzMemberPersonUid = :personUid")
    abstract suspend fun inactivateClazzMemberForPerson(personUid: Long): Int

    companion object {

        //Report Query: At risk student report with with Provider (for live data)
        const val AT_RISK_STUDENT_REPORT_QUERY = "SELECT " +
                "   Person.* , " +
                "" +
                "   (Clazz.clazzUid) AS clazzUid, " +
                "   (Clazz.clazzName) AS clazzName, " +
                "" +
                "   (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
                "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
                "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
                "" +
                "   (SELECT clazzMemberAttendancePercentage FROM ClazzMember " +
                "   WHERE clazzMemberPersonUid = Person.personUid " +
                "   AND clazzMemberClazzUid = Clazz.clazzUid) AS attendancePercentage, " +
                "" +
                "   (SELECT clazzMemberActive FROM ClazzMember " +
                "   WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "   AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
                "" +
                "   (SELECT clazzMemberRole FROM ClazzMember WHERE ClazzMember" +
                ".clazzMemberClazzUid = Clazz.clazzUid " +
                "   AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
                "" +
                " FROM ClazzMember " +
                "  LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
                "  LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "" +
                " WHERE ClazzMember.clazzMemberClazzUid IN (:clazzes)" +
                "   AND ClazzMember.clazzMemberRole =  " + ClazzMember.ROLE_STUDENT + " " +
                "   AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
                "   " +
                "   AND attendancePercentage < :riskThreshold " +
                "   AND CAST(Person.active AS INTEGER) = 1 ORDER BY clazzUid, attendancePercentage DESC"
    }
}
