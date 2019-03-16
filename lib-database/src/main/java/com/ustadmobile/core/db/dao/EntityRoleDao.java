package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

import static com.ustadmobile.core.db.dao.RoleDao.SELECT_ACCOUNT_IS_ADMIN;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)",
updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN ,
insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class EntityRoleDao implements SyncableDao<EntityRole, EntityRoleDao> {

    @UmQuery("SELECT (SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
            "OR EXISTS(SELECT EntityRole.erUid FROM EntityRole " +
            " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " JOIN PersonGroupMember ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid" +
            " WHERE " +
            " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND EntityRole.erTableId = :tableId " +
            " AND (Role.rolePermissions & :permission) > 0) AS hasPermission")
    public abstract void userHasTableLevelPermission(long accountPersonUid, int tableId, long permission,
                                              UmCallback<Boolean> callback);


    @UmQuery("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " +
            "AND erGroupUid = :groupUid")
    public abstract void findByEntitiyAndPersonGroup(int tableId, long entityUid, long groupUid,
                                                     UmCallback<List<EntityRole>> resultList);

    @UmQuery("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " +
            "AND erRoleUid = :roleUid")
    public abstract void findGroupByRoleAndEntityTypeAndUid(int tableId, long entityUid,
                                                            long roleUid, UmCallback<List<EntityRole>> resultList);
    @UmQuery("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " +
            "AND erRoleUid = :roleUid")
    public abstract List<EntityRole> findGroupByRoleAndEntityTypeAndUidSync(int tableId, long entityUid,
                                                            long roleUid);

    @UmQuery("SELECT PersonGroup.groupName AS groupName, '' AS entityName, " +
            " Clazz.clazzName AS clazzName, Location.title AS locationName, " +
            " Person.firstNames||' '||Person.lastName AS personName, " +
            " '' AS entityType, Role.roleName AS roleName, EntityRole.* " +
            " FROM EntityRole  " +
            " LEFT JOIN PersonGroup ON EntityRole.erGroupUid = PersonGroup.groupUid " +
            " LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " LEFT JOIN Clazz ON EntityRole.erEntityUid = Clazz.clazzUid " +
            " LEFT JOIN Location ON EntityRole.erEntityUid = Location.locationUid " +
            " LEFT JOIN Person ON EntityRole.erEntityUid = Person.personUid " +
            " WHERE EntityRole.erGroupUid != 0 AND EntityRole.erActive = 1")
    public abstract UmProvider<EntityRoleWithGroupName> findAllActiveRoleAssignments();

    @UmQuery("UPDATE EntityRole SET erActive = 0 where erUid = :uid ")
    public abstract void inavtivateEntityRoleAsync(long uid, UmCallback<Integer> resultObject);


    @UmQuery("SELECT * FROM EntityRole WHERE erUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<EntityRole> resultObject);

    @UmQuery("SELECT * FROM EntityRole WHERE erUid = :uid")
    public abstract UmLiveData<EntityRole> findByUidLive(long uid);

    @UmUpdate
    public abstract void updateAsync(EntityRole entity, UmCallback<Integer> resultObject);

}
