package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

@UmDao
public abstract class ClazzMemberDao implements BaseDao<ClazzMember> {

    @UmInsert
    public abstract long insert(ClazzMember entity);

    @UmInsert
    public abstract void insertAsync(ClazzMember entity, UmCallback<Long> result);

    @UmUpdate
    public abstract void update(ClazzMember entity);

    @UmUpdate
    public abstract void updateAsync(ClazzMember entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    public abstract ClazzMember findByUid(long uid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember" +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.clazzMemberActive = 1 AND ClazzMember.role = 1")
    public abstract UmProvider<ClazzMemberWithPerson> findClazzMembersByClazzId(long uid);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid AND clazzMemberClazzUid = :clazzUid")
    public abstract ClazzMember findByPersonUidAndClazzUid(long personUid, long clazzUid);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid AND clazzMemberClazzUid = :clazzUid")
    public abstract void findByPersonUidAndClazzUidAsync(long personUid, long clazzUid, UmCallback<ClazzMember> result);

    @UmQuery("Update ClazzMember SET attendancePercentage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            "AND ClazzLog.done = 1 " +
            "AND ClazzLogAttendanceRecord.attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +") * 1.0 " +
            " / " +
            "MAX(1.0, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " AND ClazzLog.done = 1) * 1.0) " +
            "WHERE ClazzMember.clazzMemberClazzUid = :clazzUid ")
    public abstract void updateAttendancePercentages(long clazzUid);

    @UmQuery("SELECT * FROM Person where personUid IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = 1) AND Person.active = 1 ")
    public abstract UmProvider<Person> findAllPeopleInClassUid(long clazzUid);


    @UmQuery("SELECT * FROM Person where personUid NOT IN ( " +
            " SELECT Person.personUid FROM ClazzMember " +
            " LEFT  JOIN Person On ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzMember.clazzMemberClazzUid = :clazzUid AND ClazzMember.role = 1) AND Person.active = 1 ")
    public abstract UmProvider<Person> findAllPeopleNotInClassUid(long clazzUid);


    @UmQuery("SELECT Person.* , (:clazzUid) AS clazzUid, " +
            " (SELECT attendancePercentage FROM ClazzMember WHERE clazzMemberPersonUid = Person.personUid) AS attendancePercentage, " +
            " (SELECT clazzMemberActive FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = :clazzUid " +
            " AND clazzMemberPersonUid = Person.personUid) AS enrolled FROM Person WHERE Person.active = 1 ")
    public abstract UmProvider<PersonWithEnrollment> findAllPeopleWithEnrollmentForClassUid(long clazzUid);


    @UmQuery("SELECT AVG(attendancePercentage) FROM ClazzMember WHERE clazzMemberPersonUid = :personUid")
    public abstract void getAverageAttendancePercentageByPersonUidAsync(long personUid, UmCallback<Float> callback);

}
