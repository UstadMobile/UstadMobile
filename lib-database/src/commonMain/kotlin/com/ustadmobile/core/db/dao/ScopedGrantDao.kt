package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ScopedGrantDaoCommon.SQL_FIND_BY_TABLE_AND_ENTITY
import app.cash.paging.PagingSource
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

    @Query(SQL_FIND_BY_TABLE_AND_ENTITY)
    abstract suspend fun findByTableIdAndEntityUid(tableId: Int, entityUid: Long): List<ScopedGrantAndName>

    @Query(SQL_FIND_BY_TABLE_AND_ENTITY)
    abstract fun findByTableIdAndEntityUidWithNameAsDataSource(
        tableId: Int,
        entityUid: Long
    ): PagingSource<Int, ScopedGrantWithName>


    @Query("""
        SELECT ScopedGrant.*
          FROM ScopedGrant
         WHERE sgTableId = :tableId
           AND sgEntityUid = :entityUid
    """)
    abstract fun findByTableIdAndEntityIdSync(tableId: Int, entityUid: Long): List<ScopedGrant>

    @Query("""
        SELECT ScopedGrant.*
          FROM ScopedGrant
         WHERE sgUid = :sgUid 
    """)
    abstract suspend fun findByUid(sgUid: Long): ScopedGrant?

    @Query("""
        SELECT ScopedGrant.*, 
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               LEFT JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                    ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgUid = :sgUid 
    """)
    abstract fun findByUidLiveWithName(sgUid: Long): Flow<ScopedGrantWithName?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findScopedGrantAndPersonGroupByPersonUidAndPermission",
            ),
        )
    )
    @Query(
        """
        SELECT EXISTS(
                SELECT PersonGroupMember.groupMemberGroupUid
                  FROM PersonGroupMember 
                       JOIN ScopedGrant
                           ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                 WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
                   AND (ScopedGrant.sgEntityUid = 0 OR ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
                   AND (ScopedGrant.sgTableId = 0 OR ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES})
                   AND (ScopedGrant.sgPermissions & :permission) > 0    
               )
        """
    )
    abstract fun userHasSystemLevelPermissionAsFlow(
        accountPersonUid: Long,
        permission: Long,
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


    @Query(
        """
        SELECT EXISTS(
                SELECT PersonGroupMember.groupMemberGroupUid
                  FROM PersonGroupMember 
                       JOIN ScopedGrant
                           ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                 WHERE PersonGroupMember.groupMemberPersonUid = :personUid
                   AND (ScopedGrant.sgPermissions & :permission) > 0    
               )
        """
    )
    abstract suspend fun userHasSystemLevelPermission(
        personUid: Long,
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