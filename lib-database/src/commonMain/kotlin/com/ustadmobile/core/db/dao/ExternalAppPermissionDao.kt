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
         WHERE ExternalAppPermission.eapPackageId = :packageId
           AND ExternalAppPermission.eapPersonUid = :personUid
           AND :currentTime BETWEEN ExternalAppPermission.eapStartTime AND ExternalAppPermission.eapExpireTime
         LIMIT 1  
    """)
    abstract suspend fun getGrantedAuthToken(
        packageId: String,
        personUid: Long,
        currentTime: Long,
    ): String?

    @Query("""
        SELECT *
          FROM ExternalAppPermission
         WHERE eapUid = :eapUid 
    """)
    abstract suspend fun getExternalAccessPermissionByUid(eapUid: Int): ExternalAppPermission?

}