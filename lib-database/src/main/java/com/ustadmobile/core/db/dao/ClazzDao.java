package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

import static com.ustadmobile.core.db.dao.ClazzDao.PERMISSION_CONDITION1;
import static com.ustadmobile.core.db.dao.ClazzDao.PERMISSION_CONDITION2;

@UmDao(readPermissionCondition = PERMISSION_CONDITION1 + Role.PERMISSION_SELECT + PERMISSION_CONDITION2)
@UmRepository
public abstract class ClazzDao implements SyncableDao<Clazz, ClazzDao> {

    protected static final String PERMISSION_CONDITION1 = " (SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
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
            ") AND (Role.rolePermissions & ";

    protected static final String PERMISSION_CONDITION2 = ") > 0)";

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
    public abstract void personHasPermission(long accountPersonUid, long permission,
                                             UmCallback<Boolean> callback);

    @UmQuery("SELECT Clazz.clazzUid as primaryKey, " +
            "(" + PERMISSION_CONDITION1 + Role.PERMISSION_UPDATE + PERMISSION_CONDITION2 + ") " +
                " AS userCanUpdate " +
            " FROM Clazz WHERE Clazz.clazzUid in (:primaryKeys)")
    @UmSyncFindUpdateable
    public abstract List<UmSyncExistingEntity> syncFindExistingEntities(List<Long> primaryKeys,
                                                                        long accountPersonUid);
}
