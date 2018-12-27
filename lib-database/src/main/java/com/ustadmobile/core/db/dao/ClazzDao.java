package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(readPermissionCondition = " (SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
        "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
        "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
        "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
        "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
        " AND (" +
            "(EntityRole.ertableId = " + Clazz.TABLE_ID +
            " AND EntityRole.erEntityUid = Clazz.clazzUid) " +
            "OR" +
            "(EntityRole.ertableId = " + Location.TABLE_ID +
            " AND EntityRole.erEntityUid IN (SELECT locationAncestorId FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid = Clazz.clazzLocationUid))" +
        ") AND (Role.rolePermissions & " + Role.PERMISSION_SELECT + ") > 0)")
@UmRepository
public abstract class ClazzDao implements SyncableDao<Clazz, ClazzDao> {

    @Override
    @UmInsert
    public abstract long insert(Clazz entity);

    @Override
    @UmInsert
    public abstract void insertAsync(Clazz entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM Clazz WHERE clazzUid = :uid")
    public abstract Clazz findByUid(long uid);


    @UmQuery("SELECT Clazz.*, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid) AS numStudents" +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    public abstract UmProvider<ClazzWithNumStudents> findAllClazzesByPersonUid(long personUid);


    /** Check if a permission is present on a specific entity e.g. update/modify etc*/
    public void personHasPermission(long personUid, long clazzUid, long permission,
                                             UmCallback<Boolean> callback) {
        callback.onSuccess(Boolean.TRUE);
    }

    public void personHasPermission(long personUid, long permission, UmCallback<Boolean> callback) {
        callback.onSuccess(Boolean.TRUE);
    }

}
