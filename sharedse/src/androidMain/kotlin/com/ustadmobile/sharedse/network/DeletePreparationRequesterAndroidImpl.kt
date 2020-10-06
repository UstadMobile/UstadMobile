package com.ustadmobile.sharedse.network

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ustadmobile.core.account.Endpoint

class DeletePreparationRequesterAndroidImpl(private val mContext: Context,
                                            private val endpoint: Endpoint): DeletePreparationRequester {

    override fun requestDelete(downloadJobUid: Int) {
        val deleteIntent = Intent(mContext, DownloadNotificationService::class.java)
        deleteIntent.action = DownloadNotificationService.ACTION_DELETE_DOWNLOAD
        deleteIntent.putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBUID, downloadJobUid)
        deleteIntent.putExtra(DownloadNotificationService.EXTRA_ENDPOINT, endpoint.url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(deleteIntent)
        } else {
            mContext.startService(deleteIntent)
        }
    }
}