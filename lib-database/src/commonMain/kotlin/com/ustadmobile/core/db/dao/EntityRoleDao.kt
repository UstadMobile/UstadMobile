package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.EntityRole
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)", 
        updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class EntityRoleDao : BaseDao<EntityRole> {

    @Query("SELECT (SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
            "OR EXISTS(SELECT EntityRole.erUid FROM EntityRole " +
            " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " JOIN PersonGroupMember ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid" +
            " WHERE " +
            " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND EntityRole.erTableId = :tableId " +
            " AND (Role.rolePermissions & :permission) > 0) AS hasPermission")
    abstract suspend fun userHasTableLevelPermission(accountPersonUid: Long,
             tableId: Int, permission: Long) : Boolean

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId " +
            " AND erEntityUid = :entityUid AND erGroupUid = :groupUid " +
            " AND erRoleUid = :roleUid ")
    abstract suspend fun findByEntitiyAndPersonGroupAndRole(tableId: Int, entityUid: Long, groupUid:
    Long, roleUid: Long) : List<EntityRole>

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " + "AND erGroupUid = :groupUid")
    abstract suspend fun findByEntitiyAndPersonGroup(tableId: Int, entityUid: Long, groupUid:
        Long) : List<EntityRole>

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " + "AND erGroupUid = :groupUid")
    abstract fun findByEntitiyAndPersonGroupSync(tableId: Int, entityUid: Long, groupUid: Long): List<EntityRole>

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " + "AND erRoleUid = :roleUid")
    abstract suspend fun findGroupByRoleAndEntityTypeAndUid(tableId: Int, entityUid: Long,
                                                            roleUid: Long) :List<EntityRole>

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId AND erEntityUid = :entityUid " + "AND erRoleUid = :roleUid")
    abstract fun findGroupByRoleAndEntityTypeAndUidSync(tableId: Int, entityUid: Long,
                                                        roleUid: Long): List<EntityRole>

    @Query(SELECT_ROLE_ASSIGNMENT_QUERY)
    abstract fun findAllActiveRoleAssignments(): DataSource.Factory<Int, EntityRoleWithGroupName>

    @Query(SELECT_ROLE_ASSIGNMENT_QUERY + ROLE_ASSIGNMENT_BY_PERSONGROUP_WHERE )
    abstract fun findAllActiveRoleAssignmentsByGroupPersonUid(groupPersonUid: Long)
            : DataSource.Factory<Int, EntityRoleWithGroupName>

    @Query("UPDATE EntityRole SET erActive = 0 where erUid = :uid ")
    abstract suspend fun inavtivateEntityRoleAsync(uid: Long) :Int


    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : EntityRole?

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<EntityRole?>

    @Update
    abstract suspend fun updateAsync(entity: EntityRole) :Int

    companion object {
        const val SELECT_ROLE_ASSIGNMENT_QUERY =
                "SELECT PersonGroup.groupName AS groupName, '' AS entityName, " +
                        " Clazz.clazzName AS clazzName, Location.title AS locationName, " +
                        " Person.firstNames||' '||Person.lastName AS personName, " +
                        " '' AS entityType, Role.roleName AS roleName, EntityRole.*, " +
                        " pg.firstNames||' '||pg.lastName AS groupPersonName, " +
                        " pg.personUid AS groupPersonUid " +
                        " FROM EntityRole  " +
                        " LEFT JOIN PersonGroup ON EntityRole.erGroupUid = PersonGroup.groupUid " +
                        " LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                        " LEFT JOIN Clazz ON EntityRole.erEntityUid = Clazz.clazzUid " +
                        " LEFT JOIN Location ON EntityRole.erEntityUid = Location.locationUid " +
                        " LEFT JOIN Person ON EntityRole.erEntityUid = Person.personUid " +
                        " LEFT JOIN Person as pg ON pg.personUid = PersonGroup.groupPersonUid " +
                        " WHERE EntityRole.erGroupUid != 0 AND CAST(EntityRole.erActive AS INTEGER) = 1 "

        const val ROLE_ASSIGNMENT_BY_PERSONGROUP_WHERE =
                " AND groupPersonUid = :groupPersonUid "
    }
}
