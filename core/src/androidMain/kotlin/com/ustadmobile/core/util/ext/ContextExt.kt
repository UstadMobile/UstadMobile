package com.ustadmobile.core.util.ext

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle


/**
 * If using Android 8+ we need to use the startForegroundService method. Older versions of Android
 * must use plain old startService.
 */
fun Context.startForegroundServiceAsSupported(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}

val Context.appMetaData: Bundle?
    get() = this.applicationContext.packageManager.getApplicationInfo(
        applicationContext.packageName, PackageManager.GET_META_DATA
    ).metaData
