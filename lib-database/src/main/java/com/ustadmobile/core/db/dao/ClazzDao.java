package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzAverage;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import java.util.List;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Role;

import static com.ustadmobile.core.db.dao.ClazzDao.PERMISSION_CONDITION1;
import static com.ustadmobile.core.db.dao.ClazzDao.PERMISSION_CONDITION2;


@UmDao(readPermissionCondition = PERMISSION_CONDITION1
        + Role.PERMISSION_SELECT + PERMISSION_CONDITION2)
@UmRepository
public abstract class ClazzDao implements SyncableDao<Clazz, ClazzDao> {


    public static final String CLAZZ_WHERE = " SELECT Clazz.*, (SELECT COUNT(*) " +
            " FROM ClazzMember WHERE " +
            " ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT +
            " AND ClazzMember.clazzMemberActive = 1) AS numStudents, " +
            " (SELECT COUNT(*) FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER +
            " AND ClazzMember.clazzMemberActive = 1 ) AS numTeachers, " +
            " (SELECT GROUP_CONCAT(Person.firstNames ||  Person.lastName ) as teacherName " +
            " FROM Person where Person.personUid in (SELECT ClazzMember.clazzMemberPersonUid " +
            " FROM ClazzMember WHERE ClazzMember.role = " + ClazzMember.ROLE_TEACHER  +
            " AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid" +
            " AND ClazzMember.clazzMemberActive = 1) " +
            " ) AS teacherNames " ;


    protected static final String PERMISSION_CONDITION1 = " (SELECT admin FROM Person " +
            " WHERE personUid = :accountPersonUid) = 1 OR " +
            "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
            "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND (" +
            "(EntityRole.ertableId = " + Clazz.TABLE_ID +
            " AND EntityRole.erEntityUid = Clazz.clazzUid) " +
            "OR" +
            "(EntityRole.ertableId = " + Location.TABLE_ID +
            " AND EntityRole.erEntityUid IN (SELECT locationAncestorId FROM LocationAncestorJoin " +
            " WHERE locationAncestorChildLocationUid = Clazz.clazzLocationUid))" +
            ") AND (Role.rolePermissions & ";

    protected static final String PERMISSION_CONDITION2 = ") > 0)";


    @Override
    @UmInsert
    public abstract long insert(Clazz entity);

    @Override
    @UmInsert
    public abstract void insertAsync(Clazz entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract Clazz findByUid(long uid);

    @UmQuery("SELECT * From Clazz WHERE clazzUid = :uid")
    public abstract UmLiveData<Clazz> findByUidLive(long uid);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Clazz> resultObject);

    @UmUpdate
    public abstract void updateAsync(Clazz entity, UmCallback<Integer> resultObject);

    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
                "  WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzesByPersonUid(long personUid);

    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz ")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzes();


    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzes();

    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.clazzName ASC")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByNameAsc();

    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.clazzName DESC")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByNameDesc();
    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.attendanceAverage ASC ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByAttendanceAsc();
    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " ORDER BY Clazz.attendanceAverage DESC ")
    public abstract UmProvider<ClazzWithNumStudents> findAllActiveClazzesSortByAttendanceDesc();

    @UmQuery("SELECT * FROM Clazz WHERE clazzName = :name")
    public abstract void findByClazzNameAsync(String name, UmCallback<List<Clazz>> resultList);


    @UmQuery("SELECT " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1) as numClazzes, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT + ") as numStudents, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER + ") as numTeachers, " +
            " ((SELECT SUM(Clazz.attendanceAverage) FROM Clazz WHERE Clazz.clazzActive = 1 ) / " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1)) as attendanceAverage ")
    public abstract void getClazzSummaryAsync(UmCallback<ClazzAverage> resultObject);

    @UmQuery(
        "SELECT Clazz.*, (:personUid) AS personUid, " +
            "(SELECT COUNT(*) FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) " +
                " AS numStudents, " +
            "(SELECT (EXISTS (SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
                " AND clazzMemberClazzUid = Clazz.clazzUid " +
                " ))) AS enrolled " +
            "FROM Clazz WHERE Clazz.clazzActive = 1")
    public abstract UmProvider<ClazzWithEnrollment>
                                            findAllClazzesWithEnrollmentByPersonUid(long personUid);

    @UmQuery(CLAZZ_WHERE +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract List<ClazzWithNumStudents> findAllClazzesByPersonUidAsList(long personUid);

    @UmQuery("Update Clazz SET attendanceAverage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            " LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzClazzUid = :clazzUid " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = 1) * 1.0 " +
            " /  " +
            "MAX(1, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            "LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzClazzUid = :clazzUid " +
            ")) * 1.0 " +
            "Where Clazz.clazzUid = :clazzUid")
    public abstract void updateAttendancePercentage(long clazzUid);

    /** Check if a permission is present on a specific entity e.g. update/modify etc*/
    @UmQuery("SELECT 1 FROM Clazz WHERE Clazz.clazzUid = :clazzUid AND (" + PERMISSION_CONDITION1 +
            " :permission" + PERMISSION_CONDITION2 + ")")
    public abstract void personHasPermission(long accountPersonUid, long clazzUid, long permission,
                                             UmCallback<Boolean> callback);

    @UmQuery("SELECT " +
            "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) OR " +
            "EXISTS(SELECT EntityRole.erUid FROM EntityRole " +
            " LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " LEFT JOIN PersonGroupMember ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            " WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND " +
            " EntityRole.erTableId = " + Clazz.TABLE_ID +
            " AND " +
            " (Role.rolePermissions & :permission) > 0)")
    public abstract void personHasPermission(long accountPersonUid, long permission, UmCallback<Boolean> callback);


}
