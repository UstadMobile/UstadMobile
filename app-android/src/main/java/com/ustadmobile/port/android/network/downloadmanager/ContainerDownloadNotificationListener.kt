package com.ustadmobile.port.android.network.downloadmanager

import android.content.Context
import android.content.Intent
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.networkmanager.DownloadNotificationService
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadListener
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.ext.startForegroundServiceAsSupported
import com.ustadmobile.lib.db.entities.DownloadJobItem

/**
 * Ensure that the foreground notification for a download starting is displayed. Normally this is
 * done when the download job preparation is requested. However if a download is being unpaused, etc.
 * this might not be sufficient.
 */
class ContainerDownloadNotificationListener(val appContext: Context, val endpoint: Endpoint) : ContainerDownloadListener {
    override fun onDownloadStarted(
        downloadManager: ContainerDownloadManager,
        downloadJobItem: DownloadJobItem
    ) {
        val startIntent = Intent(appContext, DownloadNotificationService::class.java).apply {
            action = DownloadNotificationService.ACTION_DOWNLOADJOBITEM_STARTED
            putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBUID, downloadJobItem.djiDjUid)
            putExtra(DownloadNotificationService.EXTRA_ENDPOINT, endpoint.url)
        }

        appContext.startForegroundServiceAsSupported(startIntent)
    }

    override fun onDownloadEnded(
        downloadManager: ContainerDownloadManager,
        downloadJobItem: DownloadJobItem
    ) {

    }
}