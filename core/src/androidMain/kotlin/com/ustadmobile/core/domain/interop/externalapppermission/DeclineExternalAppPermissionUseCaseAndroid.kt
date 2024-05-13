package com.ustadmobile.core.domain.interop.externalapppermission

import android.app.Activity

class DeclineExternalAppPermissionUseCaseAndroid(
    private val activity: Activity,
): DeclineExternalAppPermissionUseCase {

    override suspend fun invoke() {
        activity.finish()
    }
}