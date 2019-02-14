package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanUpdate;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

import static com.ustadmobile.core.db.dao.ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1;
import static com.ustadmobile.core.db.dao.ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2;
import static com.ustadmobile.core.db.dao.ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1;
import static com.ustadmobile.core.db.dao.ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2;

@UmDao(
selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
insertPermissionCondition = TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT +
        TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
public abstract class ClazzDao implements SyncableDao<Clazz, ClazzDao> {

    protected static final String ENTITY_LEVEL_PERMISSION_CONDITION1 = " (SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
            "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
            "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND (" +
            "(EntityRole.ertableId = " + Clazz.TABLE_ID +
            " AND EntityRole.erEntityUid = Clazz.clazzUid) " +
            "OR" +
            "(EntityRole.ertableId = " + Location.TABLE_ID +
            " AND EntityRole.erEntityUid IN (SELECT locationAncestorAncestorLocationUid FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid = Clazz.clazzLocationUid))" +
            ") AND (Role.rolePermissions & ";

    protected static final String ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)";

    protected static final String TABLE_LEVEL_PERMISSION_CONDITION1 = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
            "OR " +
            "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
            " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " WHERE " +
            " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND EntityRole.erTableId = " + Clazz.TABLE_ID +
            " AND Role.rolePermissions & ";

    protected static final String TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)";

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


    /** Check if a permission is present on a specific entity e.g. updateState/modify etc*/
    @UmQuery("SELECT 1 FROM Clazz WHERE Clazz.clazzUid = :clazzUid AND (" + ENTITY_LEVEL_PERMISSION_CONDITION1 +
            " :permission" + ENTITY_LEVEL_PERMISSION_CONDITION2 + ")")
    public abstract void personHasPermission(long accountPersonUid, long clazzUid, long permission,
                                             UmCallback<Boolean> callback);

    @UmQuery("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    public abstract void personHasPermission(long accountPersonUid, long permission,
                                             UmCallback<Boolean> callback);

    @UmQuery("SELECT Clazz.clazzUid as primaryKey, " +
            "(" + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
                " AS userCanUpdate " +
            " FROM Clazz WHERE Clazz.clazzUid in (:primaryKeys)")
    @UmSyncCheckIncomingCanUpdate
    public abstract List<UmSyncExistingEntity> syncFindExistingEntities(List<Long> primaryKeys,
                                                                        long accountPersonUid);

    @UmQuery("SELECT COUNT(*) FROM Clazz " +
            "WHERE " +
            "clazzLocalChangeSeqNum > (SELECT syncedToLocalChangeSeqNum FROM SyncStatus WHERE tableId = 6) " +
            "AND clazzLastChangedBy = (SELECT deviceBits FROM SyncDeviceBits LIMIT 1) " +
            "AND ((" + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE + //can updateState it
                ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
            " OR (" + TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT + //can insert on table
                TABLE_LEVEL_PERMISSION_CONDITION2 + "))")
    public abstract int countPendingLocalChanges(long accountPersonUid);

}
