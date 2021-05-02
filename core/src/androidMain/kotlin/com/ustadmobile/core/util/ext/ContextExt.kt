package com.ustadmobile.core.util.ext

import android.content.Context
import android.content.Intent
import android.os.Build


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
