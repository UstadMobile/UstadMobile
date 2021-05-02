package com.ustadmobile.core.networkmanager.downloadmanager

import com.ustadmobile.lib.db.entities.DownloadJobItem

/**
 * Listener for activity from ContainerDownloadManager
 */
interface ContainerDownloadListener {

    /**
     * Called just after a download has been started
     */
    fun onDownloadStarted(downloadManager: ContainerDownloadManager, downloadJobItem: DownloadJobItem)

    /**
     * Called just after a download has ended (this may or may not have been successful)
     */
    fun onDownloadEnded(downloadManager: ContainerDownloadManager, downloadJobItem: DownloadJobItem)

}