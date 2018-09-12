package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

@UmDao
public abstract class ClazzMemberDao implements BaseDao<ClazzMember> {

    @UmInsert
    public abstract long insert(ClazzMember entity);

    @UmInsert
    public abstract void insertAsync(ClazzMember entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM ClazzMember WHERE clazzMemberUid = :uid")
    public abstract ClazzMember findByUid(long uid);

    @UmQuery("SELECT ClazzMember.*, Person.* FROM ClazzMember" +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid" +
            " WHERE ClazzMember.clazzMemberClazzUid = :uid AND ClazzMember.role = 1")
    public abstract UmProvider<ClazzMemberWithPerson> findClazzMembersByClazzId(long uid);

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

    @UmQuery("SELECT AVG(attendancePercentage) FROM ClazzMember WHERE clazzMemberPersonUid = :personUid")
    public abstract void getAverageAttendancePercentageByPersonUidAsync(long personUid, UmCallback<Float> callback);

}
