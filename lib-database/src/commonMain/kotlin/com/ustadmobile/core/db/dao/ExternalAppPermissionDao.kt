package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.lib.db.entities.ExternalAppPermission

@DoorDao
expect abstract class ExternalAppPermissionDao {

    @Insert
    abstract suspend fun insertAsync(externalAppPermission: ExternalAppPermission): Long

    @Query("""
        SELECT ExternalAppPermission.eapAuthToken
          FROM ExternalAppPermission
         WHERE ExternalAppPermission.eapCallerUid = :callerUid
           AND ExternalAppPermission.eapPersonUid = :personUid
           AND :currentTime BETWEEN ExternalAppPermission.eapStartTime AND ExternalAppPermission.eapExpireTime
         LIMIT 1  
    """)
    abstract suspend fun getGrantedAuthToken(
        callerUid: Int,
        personUid: Long,
        currentTime: Long,
    ): String?

    @Query("""
        SELECT *
          FROM ExternalAppPermission
         WHERE eapUid = :eapUid 
    """)
    abstract suspend fun getExternalAccessPermissionByUid(eapUid: Int): ExternalAppPermission?

    @Query("""
        UPDATE ExternalAppPermission
           SET eapStartTime = :startTime,
               eapExpireTime = :expireTime
         WHERE eapUid = :eapUid
    """)
    abstract suspend fun grantPermission(
        eapUid: Int,
        startTime: Long,
        expireTime: Long,
    )

}