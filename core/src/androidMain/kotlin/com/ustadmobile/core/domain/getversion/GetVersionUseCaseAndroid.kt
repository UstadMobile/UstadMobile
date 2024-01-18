package com.ustadmobile.core.domain.getversion

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class GetVersionUseCaseAndroid(
    private val appContext: Context,
): GetVersionUseCase {

    override operator fun invoke(): GetVersionUseCase.VersionInfo {
        val packageInfo = appContext.packageManager.getPackageInfo(
            appContext.packageName, PackageManager.GET_META_DATA)
        @Suppress("DEPRECATION")
        val versionCode = if(Build.VERSION.SDK_INT > 28)
            packageInfo.longVersionCode
        else
            packageInfo.versionCode.toLong()

        return GetVersionUseCase.VersionInfo(
            "${packageInfo.versionName} ($versionCode)",
            buildTime = 0,
        )
    }

}