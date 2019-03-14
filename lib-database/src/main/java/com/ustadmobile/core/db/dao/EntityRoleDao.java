package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
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

    @UmQuery("SELECT *, '' AS groupName, '' AS entityName, '' AS entityType FROM EntityRole")
    public abstract UmProvider<EntityRoleWithGroupName> findAllRoleAssignments();
}
