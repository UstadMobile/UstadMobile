package com.ustadmobile.sharedse.network

import android.content.Context
import android.content.Intent
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.networkmanager.DownloadNotificationService
import com.ustadmobile.core.util.ext.startForegroundServiceAsSupported

/**
 *
 */
class DownloadPreparationRequesterAndroidImpl(private val mContext: Context,
                                              private val endpoint: Endpoint): DownloadPreparationRequester {

    override fun requestPreparation(downloadJobId: Int) {
        val prepareJobIntent = Intent(mContext, DownloadNotificationService::class.java)
        prepareJobIntent.action = DownloadNotificationService.ACTION_PREPARE_DOWNLOAD
        prepareJobIntent.putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBUID, downloadJobId)
        prepareJobIntent.putExtra(DownloadNotificationService.EXTRA_ENDPOINT, endpoint.url)

        mContext.startForegroundServiceAsSupported(prepareJobIntent)
    }
}