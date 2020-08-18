package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)", 
        updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class EntityRoleDao : BaseDao<EntityRole> {

    @Query("""SELECT COALESCE((SELECT admin FROM Person WHERE personUid = :accountPersonUid), 0) 
            OR EXISTS(SELECT EntityRole.erUid FROM EntityRole 
             JOIN Role ON EntityRole.erRoleUid = Role.roleUid 
             JOIN PersonGroupMember ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
             WHERE 
             PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
             AND EntityRole.erTableId = :tableId 
             AND (Role.rolePermissions & :permission) > 0) AS hasPermission""")
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

    @Query(FILTER_BY_PERSON_UID2)
    abstract fun filterByPersonWithExtra(personUid: Long)
            : DataSource.Factory<Int, EntityRoleWithNameAndRole>

    @Query(FILTER_BY_PERSON_UID2)
    abstract fun filterByPersonWithExtraAsList(personUid: Long)
            : List<EntityRoleWithNameAndRole>

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : EntityRole?


    @Query("""SELECT EntityRole.*, Role.*, 
            (CASE 
                WHEN EntityRole.erTableId = ${Clazz.TABLE_ID}	THEN (SELECT Clazz.clazzName FROM Clazz WHERE Clazz.clazzUid = EntityRole.erEntityUid)
                WHEN EntityRole.erTableId = ${Person.TABLE_ID}	THEN (SELECT Person.firstNames||' '||Person.lastName FROM Person WHERE Person.personUid = EntityRole.erEntityUid)
                WHEN EntityRole.erTableId = ${School.TABLE_ID}	THEN (SELECT School.schoolName FROM School WHERE School.schoolUid = EntityRole.erEntityUid)
                ELSE '' 
            END) as entityRoleScopeName,
        
            FROM EntityRole 
            LEFT JOIN Role on Role.roleUid = EntityRole.erRoleUid 
            WHERE erUid = :uid """)
    abstract suspend fun findWithNameAndRoleByUidAsync(uid: Long): EntityRoleWithNameAndRole

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<EntityRole?>

    @Update
    abstract suspend fun updateAsync(entity: EntityRole) :Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: EntityRole)

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


        const val FILTER_BY_PERSON_UId =
                """
        SELECT  
        (CASE 
            WHEN EntityRole.erTableId = ${Clazz.TABLE_ID}	THEN (SELECT Clazz.clazzName FROM Clazz WHERE Clazz.clazzUid = EntityRole.erEntityUid)
            WHEN EntityRole.erTableId = ${Person.TABLE_ID}	THEN (SELECT Person.firstNames||' '||Person.lastName FROM Person WHERE Person.personUid = EntityRole.erEntityUid)
            WHEN EntityRole.erTableId = ${School.TABLE_ID}	THEN (SELECT School.schoolName FROM School WHERE School.schoolUid = EntityRole.erEntityUid)
            ELSE '' 
        END) as entityRoleScopeName,
        Role.*, EntityRole.* FROM EntityRole
        LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid 
        LEFT JOIN Person AS fromPerson ON fromPerson.personUid = :personUid
        WHERE EntityRole.erGroupUid IN (
        SELECT PersonGroup.groupUid FROM PersonGroupMember 
        LEFT JOIN PersonGroup ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
        WHERE PersonGroupMember.groupMemberPersonUid = fromPerson.personUid 
        )
        
        """

        const val FILTER_BY_PERSON_UID2 =
                """
                    SELECT  
                    (CASE 
                        WHEN EntityRole.erTableId = ${Clazz.TABLE_ID}	THEN (SELECT Clazz.clazzName FROM Clazz WHERE Clazz.clazzUid = EntityRole.erEntityUid)
                        WHEN EntityRole.erTableId = ${Person.TABLE_ID}	THEN (SELECT Person.firstNames||' '||Person.lastName FROM Person WHERE Person.personUid = EntityRole.erEntityUid)
                        WHEN EntityRole.erTableId = ${School.TABLE_ID}	THEN (SELECT School.schoolName FROM School WHERE School.schoolUid = EntityRole.erEntityUid)
                        ELSE '' 
                    END) as entityRoleScopeName,
                    Role.*, EntityRole.* FROM EntityRole
                    LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid 
                    LEFT JOIN Person AS fromPerson ON fromPerson.personUid = :personUid
                    WHERE EntityRole.erGroupUid = (
                        SELECT PersonGroup.groupUid FROM PersonGroup WHERE PersonGroup.groupPersonUid = fromPerson.personUid 
                    )
                """
    }
}
