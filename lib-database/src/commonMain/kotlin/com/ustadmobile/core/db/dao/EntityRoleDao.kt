package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.PostgresQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*

@Repository
@Dao
abstract class EntityRoleDao {

    @Query("""
        SELECT COALESCE((
               SELECT admin 
                 FROM Person 
                WHERE personUid = :accountPersonUid), 0)
            OR EXISTS(SELECT EntityRole.erUid FROM EntityRole 
               JOIN Role 
                    ON EntityRole.erRoleUid = Role.roleUid 
               JOIN PersonGroupMember 
                    ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
               AND (Role.rolePermissions & :permission) > 0) AS hasPermission""")
    @PostgresQuery("""
        SELECT COALESCE((
               SELECT admin 
                 FROM Person 
                WHERE personUid = :accountPersonUid), false)
            OR EXISTS(SELECT EntityRole.erUid FROM EntityRole 
               JOIN Role 
                    ON EntityRole.erRoleUid = Role.roleUid 
               JOIN PersonGroupMember 
                    ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
               AND (Role.rolePermissions & :permission) > 0) AS hasPermission
    """)
    abstract suspend fun userHasTableLevelPermission(accountPersonUid: Long, permission: Long) : Boolean

    @Query("SELECT * FROM EntityRole WHERE erTableId = :tableId " +
            " AND erEntityUid = :entityUid AND erGroupUid = :groupUid " +
            " AND erRoleUid = :roleUid ")
    abstract suspend fun findByEntitiyAndPersonGroupAndRole(tableId: Int, entityUid: Long, groupUid:
    Long, roleUid: Long) : List<EntityRole>

    @Query(FILTER_BY_PERSON_UID2)
    abstract fun filterByPersonWithExtra(personGroupUid: Long)
            : DoorDataSourceFactory<Int, EntityRoleWithNameAndRole>

    @Query(FILTER_BY_PERSON_UID2)
    abstract suspend fun filterByPersonWithExtraAsList(personGroupUid: Long)
            : List<EntityRoleWithNameAndRole>

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : EntityRole?


    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<EntityRole?>

    @Update
    abstract suspend fun updateAsync(entity: EntityRole) :Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: EntityRole)

    companion object {


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
                    WHERE EntityRole.erGroupUid = :personGroupUid
                    AND CAST(EntityRole.erActive AS INTEGER) = 1 
                """
    }
}
