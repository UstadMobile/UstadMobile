package com.ustadmobile.core.domain.interop.externalapppermission

import com.ustadmobile.core.db.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.lib.util.randomString

class StoreExternalAppPermissionUseCase(
    private val getExternalAppPermissionRequestInfoUseCase: GetExternalAppPermissionRequestInfoUseCase,
    private val db: UmAppDatabase,
) {

    suspend operator fun invoke(
        personUid: Long,
    ): ExternalAppPermission {
        val requestInfo = getExternalAppPermissionRequestInfoUseCase()
        val permission = ExternalAppPermission(
            eapPersonUid = personUid,
            eapStartTime = systemTimeInMillis(),
            eapExpireTime = UNSET_DISTANT_FUTURE,
            eapPackageId = requestInfo.packageName,
            eapAuthToken = randomString(24),
        )

        return permission.copy(
            eapUid = db.externalAppPermissionDao().insertAsync(permission).toInt()
        )
    }

}