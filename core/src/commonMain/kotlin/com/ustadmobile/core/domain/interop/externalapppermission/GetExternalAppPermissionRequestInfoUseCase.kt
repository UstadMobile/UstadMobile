package com.ustadmobile.core.domain.interop.externalapppermission

import com.ustadmobile.core.domain.interop.InteropIcon

interface GetExternalAppPermissionRequestInfoUseCase {

    data class ExternalAppPermissionRequestInfo(
        val appDisplayName: String,
        val icon: InteropIcon,
        val packageName: String? = null,
    )

    suspend operator fun invoke() : ExternalAppPermissionRequestInfo

}