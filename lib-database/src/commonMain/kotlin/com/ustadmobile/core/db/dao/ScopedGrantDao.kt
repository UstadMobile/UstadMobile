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
                 WHERE PersonGroupMember.groupMemberPersonUid = :personUid
                   AND (ScopedGrant.sgPermissions & :permission) > 0    
               )
        """
    )
    abstract fun userHasSystemLevelPermissionAsFlow(
        personUid: Long,
        permission: Long,
    ): Flow<Boolean>


    @Query("""
        SELECT PersonGroupMember.*, PersonGroup.*, ScopedGrant.*
          FROM PersonGroupMember 
               JOIN ScopedGrant
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
               JOIN PersonGroup
                    ON PersonGroup.groupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :personUid
           AND (ScopedGrant.sgPermissions & :permission) > 0    
    """)
    abstract suspend fun findScopedGrantAndPersonGroupByPersonUidAndPermission(
        personUid: Long,
        permission: Long
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


}