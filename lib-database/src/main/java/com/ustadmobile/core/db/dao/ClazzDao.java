package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.Clazz;
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


    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzesByPersonUid(long personUid);

    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract List<ClazzWithNumStudents> findAllClazzesByPersonUidAsList(long personUid);

    @UmQuery("Update Clazz SET attendanceAverage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            " LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = 1) * 1.0 " +
            " /  " +
            "MAX(1, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "WHERE ClazzLog.done = 1)) * 1.0 " +
            "Where Clazz.clazzUid = :clazzUid")
    public abstract void updateAttendancePercentage(long clazzUid);


}
