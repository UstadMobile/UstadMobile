package com.ustadmobile.sharedse.network

import android.content.Context
import android.content.Intent
import android.os.Build

actual fun requestDelete(contentEntryUid: Long, context: Any) {

    val androidContext = context as Context
    val deleteIntent = Intent(androidContext, DownloadNotificationService::class.java)
    deleteIntent.action = DownloadNotificationService.ACTION_DELETE_DOWNLOAD
    deleteIntent.putExtra(DownloadNotificationService.EXTRA_CONTENT_ENTRY_UID, contentEntryUid)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        androidContext.startForegroundService(deleteIntent)
    } else {
        androidContext.startService(deleteIntent)
    }
}