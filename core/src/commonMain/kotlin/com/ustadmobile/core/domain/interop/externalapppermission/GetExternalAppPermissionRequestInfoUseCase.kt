package com.ustadmobile.core.domain.interop.externalapppermission

import com.ustadmobile.core.domain.interop.InteropIcon

interface GetExternalAppPermissionRequestInfoUseCase {

    data class ExternalAppPermissionRequestInfo(
        val appName: String,
        val icon: InteropIcon,
    )

    suspend operator fun invoke() : ExternalAppPermissionRequestInfo

}