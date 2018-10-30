package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

import java.util.List;

@UmDao
public abstract class ClazzDao implements BaseDao<Clazz> {

    @Override
    @UmInsert
    public abstract long insert(Clazz entity);

    @Override
    @UmInsert
    public abstract void insertAsync(Clazz entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract Clazz findByUid(long uid);

    @UmQuery("SELECT * From Clazz WHERE clazzUid = :uid")
    public abstract UmLiveData<Clazz> findByUidLive(long uid);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Clazz> result);

    @UmUpdate
    public abstract void updateAsync(Clazz entity, UmCallback<Integer> result);

    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE " +
                "  ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) " +
                "AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
                "  WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzesByPersonUid(long personUid);

    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
                " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz ")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzes();


    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE Clazz.clazzActive = 1 ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzes();

    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.clazzName ASC")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByNameAsc();
    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.clazzName DESC")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByNameDesc();
    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.attendanceAverage ASC ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByAttendanceAsc();
    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.attendanceAverage DESC ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByAttendanceDesc();



    @UmQuery(
        "SELECT Clazz.*, (:personUid) AS personUid, " +
            "(SELECT COUNT(*) FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) " +
                " AS numStudents, " +
            "(SELECT (EXISTS (SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
                " AND clazzMemberClazzUid = Clazz.clazzUid " +
                " ))) AS enrolled " +
            "FROM Clazz WHERE Clazz.clazzActive = 1")
    public abstract UmProvider<ClazzWithEnrollment> findAllClazzesWithEnrollmentByPersonUid(long personUid);

    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract List<ClazzWithNumStudents> findAllClazzesByPersonUidAsList(long personUid);

    @UmQuery("Update Clazz SET attendanceAverage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzClazzUid = :clazzUid " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = 1) * 1.0 " +
            " /  " +
            "MAX(1, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzClazzUid = :clazzUid " +
            ")) * 1.0 " +
            "Where Clazz.clazzUid = :clazzUid")
    public abstract void updateAttendancePercentage(long clazzUid);


}
