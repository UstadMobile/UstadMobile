package com.ustadmobile.core.domain.interop.externalapppermission

interface GrantExternalAppPermissionUseCase {

    suspend operator fun invoke(personUid: Long)

}