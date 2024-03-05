package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ScopedGrantDaoCommon.SQL_USER_HAS_SYSTEM_LEVEL_PERMISSION
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.ScopedGrantAndGroupMember
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ScopedGrantDao {


    @Insert
    abstract suspend fun insertAsync(scopedGrant: ScopedGrant): Long

    @Insert
    abstract suspend fun insertListAsync(scopedGrantList: List<ScopedGrant>)

    @Update
    abstract suspend fun updateAsync(scopedGrant: ScopedGrant)

    @Update
    abstract suspend fun updateListAsync(scopedGrantList: List<ScopedGrant>)


    @Query("""
        SELECT ScopedGrant.*
          FROM ScopedGrant
         WHERE sgUid = :sgUid 
    """)
    abstract suspend fun findByUid(sgUid: Long): ScopedGrant?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findScopedGrantAndPersonGroupByPersonUidAndPermission",
            ),
        )
    )
    @Query(SQL_USER_HAS_SYSTEM_LEVEL_PERMISSION)
    abstract fun userHasSystemLevelPermissionAsFlow(
        accountPersonUid: Long,
        permission: Long,
    ): Flow<Boolean>


    @Query("""
        SELECT EXISTS(
                SELECT PersonGroupMember.groupMemberGroupUid
                  FROM PersonGroupMember 
                       JOIN ScopedGrant
                           ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                 WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
                   AND ScopedGrant.sgEntityUid =  ${ScopedGrant.ALL_ENTITIES}
                   AND ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                   AND (ScopedGrant.sgPermissions & ${Role.ALL_PERMISSIONS}) > 0  
               )
    """)
    abstract fun userHasAllPermissionsOnAllTablesGrant(
        accountPersonUid: Long,
    ): Flow<Boolean>

    @Query("""
        SELECT PersonGroupMember.*, PersonGroup.*, ScopedGrant.*
          FROM PersonGroupMember 
               JOIN ScopedGrant
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
               JOIN PersonGroup
                    ON PersonGroup.groupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
           AND (ScopedGrant.sgPermissions & :permission) > 0    
    """)
    abstract suspend fun findScopedGrantAndPersonGroupByPersonUidAndPermission(
        accountPersonUid: Long,
        permission: Long
    ): List<ScopedGrantAndGroupMember>

    /**
     * Find all ScopedGrants for the given personUid
     */
    @Query("""
        SELECT PersonGroupMember.*, PersonGroup.*, ScopedGrant.*
          FROM PersonGroupMember 
               JOIN ScopedGrant
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
               JOIN PersonGroup
                    ON PersonGroup.groupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
    """)
    abstract suspend fun findScopedGrantAndPersonGroupByPersonUid(
        accountPersonUid: Long
    ): List<ScopedGrantAndGroupMember>



    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findScopedGrantAndPersonGroupByPersonUidAndPermission",
            ),
        )
    )
    @Query(SQL_USER_HAS_SYSTEM_LEVEL_PERMISSION)
    abstract suspend fun userHasSystemLevelPermission(
        accountPersonUid: Long,
        permission: Long
    ): Boolean


    /**
     * Get permissions entities for a given accountPersonUid (the person logged in) that apply to another person (e.g. a profile being viewed etc).
     * accountPersonUid may equal personUid if someone is viewing their own profile etc.
     *
     * Same query structure as PersonDao.personHasPermissionFlow
     */
    @Query("""
        SELECT PersonGroupMember.*, PersonGroup.*, ScopedGrant.*
          FROM Person
               JOIN ScopedGrant
                    ON ${Person.FROM_PERSON_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
               JOIN PersonGroupMember 
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
               JOIN PersonGroup
                    ON PersonGroup.groupUid = PersonGroupMember.groupMemberGroupUid 
         WHERE Person.personUid = :personUid
           AND (ScopedGrant.sgPermissions & :permission) > 0
           AND PersonGroupMember.groupMemberPersonUid = :accountPersonUid
    """)
    abstract suspend fun personPermissionsForPerson(
        accountPersonUid: Long,
        personUid: Long,
        permission: Long
    ): List<ScopedGrantAndGroupMember>

}