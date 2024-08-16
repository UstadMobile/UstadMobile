package com.ustadmobile.core.domain.interop.externalapppermission

import android.app.Activity
import com.ustadmobile.core.domain.interop.InteropIconAndroid

/**
 * Provide information about the external app that is requesting permission so that we can display
 * this info to the user for them to accept or decline.
 *
 * On Android this is done via the calling activity
 */
class GetExternalAppPermissionRequestInfoUseCaseAndroid(
    val activity: Activity
): GetExternalAppPermissionRequestInfoUseCase {

    override suspend fun invoke(

    ): GetExternalAppPermissionRequestInfoUseCase.ExternalAppPermissionRequestInfo {
        val caller = activity.callingActivity
            ?: throw IllegalStateException("Calling activity is null")

        val activityInfo = activity.packageManager.getActivityInfo(caller, 0)

        return GetExternalAppPermissionRequestInfoUseCase.ExternalAppPermissionRequestInfo(
            appDisplayName = activityInfo.loadLabel(activity.packageManager).toString(),
            icon = InteropIconAndroid(activityInfo.loadIcon(activity.packageManager))
        )

    }
}