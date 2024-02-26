package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.SystemPermission
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class SystemPermissionDao {

    @Query("""
        SELECT SystemPermission.*
          FROM SystemPermission
         WHERE SystemPermission.spToPersonUid = :accountPersonUid
           AND CAST(:includeDeleted AS INTEGER) = 1 
            OR NOT SystemPermission.spIsDeleted
    """)
    abstract suspend fun findAllByPersonUid(
        accountPersonUid: Long,
        includeDeleted: Boolean
    ): List<SystemPermission>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT EXISTS(
               SELECT 1
                 FROM SystemPermission
                WHERE :accountPersonUid != 0 
                  AND SystemPermission.spToPersonUid = :accountPersonUid
                  AND (SystemPermission.spPermissionsFlag & :permission) > 0
                  AND NOT SystemPermission.spIsDeleted 
        )
    """)
    abstract suspend fun personHasSystemPermission(
        accountPersonUid: Long,
        permission: Long,
    ): Boolean

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT EXISTS(
               SELECT 1
                 FROM SystemPermission
                WHERE :accountPersonUid != 0
                  AND SystemPermission.spToPersonUid = :accountPersonUid
                  AND (SystemPermission.spPermissionsFlag & :permission) > 0
                  AND NOT SystemPermission.spIsDeleted 
        )
    """)
    abstract suspend fun personHasSystemPermissionAsFlow(
        accountPersonUid: Long,
        permission: Long,
    ): Flow<Boolean>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(systemPermissions: SystemPermission)

}