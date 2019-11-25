package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error

actual abstract class AbstractFetchListenerMpp : FetchListenerMpp {

    actual override fun onAdded(download: DownloadMpp) {
    }

    actual override fun onQueued(download: DownloadMpp, waitingOnNetwork: Boolean) {
    }

    actual override fun onWaitingNetwork(download: DownloadMpp) {
    }

    actual override fun onCompleted(download: DownloadMpp) {
    }

    actual override fun onError(download: DownloadMpp, error: Error, throwable: Throwable?) {
    }

    actual override fun onDownloadBlockUpdated(download: DownloadMpp, downloadBlock: DownloadBlockMpp, totalBlocks: Int) {
    }

    actual override fun onStarted(download: DownloadMpp, downloadBlocks: List<DownloadBlockMpp>, totalBlocks: Int) {
    }

    actual override fun onProgress(download: DownloadMpp, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
    }

    actual override fun onPaused(download: DownloadMpp) {
    }

    actual override fun onResumed(download: DownloadMpp) {
    }

    actual override fun onCancelled(download: DownloadMpp) {
    }

    actual override fun onRemoved(download: DownloadMpp) {
    }

    actual override fun onDeleted(download: DownloadMpp) {
    }

}