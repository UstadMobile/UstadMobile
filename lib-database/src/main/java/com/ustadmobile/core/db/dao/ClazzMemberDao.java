package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonNameWithClazzName;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import java.util.List;

@UmDao(
        inheritPermissionFrom = ClazzDao.class,
        inheritPermissionForeignKey = "clazzMemberClazzUid",
        inheritPermissionJoinedPrimaryKey = "clazzUid"
)
@UmRepository
public abstract class ClazzMemberDao implements SyncableDao<ClazzMember, ClazzMemberDao> {

    @UmInsert
    public abstract long insert(ClazzMember entity);

    @UmInsert
    public abstract void insertAsync(ClazzMember entity, UmCallback<Long> resultObject);

    @UmUpdate
    public abstract void update(ClazzMember entity);

    @UmUpdate
    public abstract void updateAsync(ClazzMember entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM ClazzMember")
    public abstract List<ClazzMember> findAllAsList();

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    public abstract ClazzMember findByUid(long uid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember" +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.clazzMemberActive = 1 " +
            "AND ClazzMember.role = 1")
    public abstract UmProvider<ClazzMemberWithPerson> findClazzMembersByClazzId(long uid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.clazzMemberActive = 1 " +
            "AND ClazzMember.role = :role")
    public abstract void findClazzMemberWithPersonByRoleForClazzUid(long uid, int role,
                                                  UmCallback<List<ClazzMemberWithPerson>> resultList);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.clazzMemberActive = 1 " +
            "AND ClazzMember.role = :role")
    public abstract List<ClazzMemberWithPerson> findClazzMemberWithPersonByRoleForClazzUidSync(
            long uid, int role);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
            "AND clazzMemberClazzUid = :clazzUid")
    public abstract ClazzMember findByPersonUidAndClazzUid(long personUid, long clazzUid);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
            "AND clazzMemberClazzUid = :clazzUid")
    public abstract void findByPersonUidAndClazzUidAsync(long personUid, long clazzUid,
                                                         UmCallback<ClazzMember> resultObject);

    @UmQuery("Update ClazzMember SET attendancePercentage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = " +
            "ClazzLog.clazzLogUid " +
            "WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            "AND ClazzLog.done = 1 " +
            "AND ClazzLogAttendanceRecord.attendanceStatus = " +
            ClazzLogAttendanceRecord.STATUS_ATTENDED +") * 1.0 " +
            " / " +
            "MAX(1.0, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = " +
            "ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND ClazzLog.done = 1) * 1.0) " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid ")
    public abstract void updateAttendancePercentages(long clazzUid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.clazzMemberActive = 1 " +
            "AND ClazzMember.role = :role")
    public abstract List<ClazzMember> findByClazzUid(long uid, int role);

    @UmQuery("SELECT ClazzMember.clazzMemberUid, Clazz.clazzName, Person.firstNames, " +
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
    public abstract void findAllMembersForAttendanceOverConsecutiveDays(
            int type, int days, long clazzUid, UmCallback<List<PersonNameWithClazzName>> resultList);

    @UmQuery("SELECT * FROM Person where personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = 1) AND Person.active = 1 ")
    public abstract UmProvider<Person> findAllActivePeopleInClassUid(long clazzUid);


    @UmQuery("SELECT Person.*, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid " +
            " FROM Person where personUid IN ( " +
            "  SELECT Person.personUid FROM ClazzMember  " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = 1) AND Person.active = 1 ")
    public abstract UmProvider<PersonWithPersonPicture> findAllPeopleWithPersonPictureInClassUid(long clazzUid);


    @UmQuery("SELECT Person.*, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid " +
            " FROM Person where personUid IN ( " +
            "  SELECT Person.personUid FROM ClazzMember  " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = 1 AND ClazzMember.clazzMemberUid != :currentClazzMemberUid)" +
            " AND Person.active = 1 " )
    public abstract UmProvider<PersonWithPersonPicture> findAllPeopleWithPersonPictureInClassUid(
            long clazzUid, long currentClazzMemberUid);

    @UmQuery("SELECT * FROM Person where personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = 1 AND ClazzMember.clazzMemberUid NOT IN (:notIn)) " +
            "AND Person.active = 1 ")
    public abstract UmProvider<Person> findAllPeopleInClassUidExcept(long clazzUid, List<Long> notIn);


    @UmQuery("SELECT * FROM Person where personUid NOT IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.role = 1) " +
            "AND Person.active = 1 ")
    public abstract UmProvider<Person> findAllPeopleNotInClassUid(long clazzUid);

    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            " AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid AND ClazzMember.role = "
            + ClazzMember.ROLE_STUDENT + " ) AS enrolled FROM Person WHERE Person.active = 1 ")
    public abstract UmProvider<PersonWithEnrollment> findAllStudentsWithEnrollmentForClassUid(long clazzUid);


    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid AND " +
            " clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "   AND ClazzMember.role = 1) as isClazzStudent " +
            " FROM Person WHERE Person.active = 1 " +
            " AND isClazzStudent = 0")
    public abstract UmProvider<PersonWithEnrollment> findAllEligibleTeachersWithEnrollmentForClassUid(long clazzUid);

    @UmQuery("SELECT " +
            "  Person.* , " +
            "  (:clazzUid) AS clazzUid,  " +
            "  (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "    PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "    picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            "   ClazzMember.attendancePercentage, " +
            "  (SELECT clazzMemberActive FROM ClazzMember " +
            "    WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "    AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            "   (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "   AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            "FROM " +
            "Person " +
            "LEFT JOIN ClazzMember ON ClazzMember.clazzMemberClazzUid = :clazzUid AND " +
            "   ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "WHERE " +
            "  personUid IN ( " +
            "   SELECT Person.personUid FROM ClazzMember " +
            "   LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "   WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "   AND ClazzMember.attendancePercentage >= :apl  AND ClazzMember.attendancePercentage <= :aph" +
            "   AND ClazzMember.clazzMemberActive = 1 " +
            ") " +
            " AND Person.active = 1 " +
            " AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery " +
            "ORDER BY clazzMemberRole ASC")
    public abstract UmProvider<PersonWithEnrollment>
            findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(long clazzUid,
                             float apl, float aph, String searchQuery);


    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " ) AND Person.active = 1 ORDER BY clazzMemberRole ASC")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentInClazzByClazzUid(long clazzUid);



    //REPORT Query: At Risk Student Report
    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +

            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +

            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +

            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +

            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +

            " FROM Person " +

            " WHERE personUid IN ( " +
            "   SELECT Person.personUid FROM ClazzMember " +
            "   LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "   WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            "       AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT + " " +
            "       AND ClazzMember.clazzMemberActive = 1 " +
            "   )" +
            " AND attendancePercentage < :riskThreshold " +
            " AND Person.active = 1 ORDER BY attendancePercentage DESC")
    public abstract void findAllStudentsAtRiskForClazzUidAsync(long clazzUid, float riskThreshold,
                                               UmCallback<List<PersonWithEnrollment>> resultList);

    //Report Query: At risk student report with with Provider (for live data)
    @UmQuery("SELECT " +
            "   Person.* , " +
            "" +
            "   (Clazz.clazzUid) AS clazzUid, " +
            "   (Clazz.clazzName) AS clazzName, " +
            "" +
            "   (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            "" +
            "   (SELECT attendancePercentage FROM ClazzMember " +
            "   WHERE clazzMemberPersonUid = Person.personUid " +
            "   AND clazzMemberClazzUid = Clazz.clazzUid) AS attendancePercentage, " +
            "" +
            "   (SELECT clazzMemberActive FROM ClazzMember " +
            "   WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            "   AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            "" +
            "   (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            "   AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            "" +
            " FROM ClazzMember " +
            "  LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "  LEFT JOIN Clazz ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            "" +
            " WHERE ClazzMember.clazzMemberClazzUid IN (:clazzes)" +
            "   AND ClazzMember.role =  " + ClazzMember.ROLE_STUDENT + " " +
            "   AND ClazzMember.clazzMemberActive = 1 " +
            "   " +
            "   AND attendancePercentage < :riskThreshold " +
            "   AND Person.active = 1 ORDER BY clazzUid, attendancePercentage DESC")
    public abstract UmProvider<PersonWithEnrollment> findAllStudentsAtRiskForClazzListAsync(List<Long> clazzes,
                                                                                            float riskThreshold);


    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " ) AND Person.active = 1 ORDER BY clazzMemberRole DESC, Person.firstNames ASC")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentInClazzByClazzUidSortByNameAsc(long clazzUid);

    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " ) AND Person.active = 1 ORDER BY clazzMemberRole DESC, attendancePercentage ASC")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceAsc(long clazzUid);

    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " ) AND Person.active = 1 ORDER BY clazzMemberRole DESC, attendancePercentage DESC")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentInClazzByClazzUidSortByAttendanceDesc(long clazzUid);


    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "   PersonPicture.personPicturePersonUid = Person.personUid ORDER BY " +
            "   picTimestamp DESC LIMIT 1) AS personPictureUid , " +
            " (SELECT attendancePercentage FROM ClazzMember " +
            "WHERE clazzMemberPersonUid = Person.personUid " +
            "AND clazzMemberClazzUid = :clazzUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled, " +
            " (SELECT role FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) as clazzMemberRole " +
            " FROM Person " +
            " WHERE personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " ) AND Person.active = 1 ORDER BY clazzMemberRole DESC, Person.firstNames DESC")
    public abstract UmProvider<PersonWithEnrollment> findAllPersonWithEnrollmentInClazzByClazzUidSortByNameDesc(long clazzUid);


    @UmQuery("SELECT AVG(attendancePercentage) FROM ClazzMember WHERE clazzMemberPersonUid = :personUid")
    public abstract void getAverageAttendancePercentageByPersonUidAsync(long personUid, UmCallback<Float> callback);


    @UmQuery("SELECT " +
            " (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND ClazzLog.done = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            " AND ClazzLogAttendanceRecord.attendanceStatus =  " + ClazzLogAttendanceRecord.STATUS_ATTENDED + " ) * 1.0 " +
            " / " +
            " MAX(1.0, (SELECT COUNT(*)  FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND ClazzLog.done = 1) * 1.0) as attended_average " +
            " FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT + " ")
    public abstract void getAttendanceAverageAsListForClazzBetweenDates(long clazzUid,
                                                                long fromDate, long toDate,
                                                                UmCallback<List<Float>> resultList);


    @UmQuery("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE " +
            "clazzMemberPersonUid = :personUid AND clazzMemberClazzUid = :clazzUid")
    public abstract int updateClazzMemberActiveForPersonAndClazz(long personUid, long clazzUid, int enrolled);

    public int updateClazzMemberActiveForPersonAndClazz(long personUid, long clazzUid, boolean enrolled){
        if(enrolled){
            return updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 1);
        }else{
            return updateClazzMemberActiveForPersonAndClazz(personUid, clazzUid, 0);
        }
    }

    @UmQuery("UPDATE ClazzMember SET clazzMemberActive = :enrolled WHERE clazzMemberUid = :clazzMemberUid")
    public abstract int updateClazzMemberActiveForClazzMember(long clazzMemberUid, int enrolled);

    public int updateClazzMemberActiveForClazzMember(long clazzMemberUid, boolean enrolled){
        if(enrolled){
           return updateClazzMemberActiveForClazzMember(clazzMemberUid, 1);
        }else{
           return updateClazzMemberActiveForClazzMember(clazzMemberUid, 0);
        }
    }

}
