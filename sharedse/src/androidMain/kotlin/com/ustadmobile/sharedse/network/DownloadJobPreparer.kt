package com.ustadmobile.sharedse.network

import android.content.Context
import android.content.Intent
import android.os.Build

actual fun requestDownloadPreparation(downloadJobUid: Int, context: Any) {
    val androidContext = context as Context
    val prepareJobIntent = Intent(androidContext, DownloadNotificationService::class.java)
    prepareJobIntent.action = DownloadNotificationService.ACTION_PREPARE_DOWNLOAD
    prepareJobIntent.putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBUID, downloadJobUid)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        androidContext.startForegroundService(prepareJobIntent)
    } else {
        androidContext.startService(prepareJobIntent)
    }

}