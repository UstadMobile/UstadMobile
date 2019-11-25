package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error

expect abstract class AbstractFetchListenerMpp() : FetchListenerMpp {

    override fun onAdded(download: DownloadMpp)

    override fun onQueued(download: DownloadMpp, waitingOnNetwork: Boolean)

    override fun onWaitingNetwork(download: DownloadMpp)

    override fun onCompleted(download: DownloadMpp)

    override fun onError(download: DownloadMpp, error: Error, throwable: Throwable?)

    override fun onDownloadBlockUpdated(download: DownloadMpp, downloadBlock: DownloadBlockMpp, totalBlocks: Int)

    override fun onStarted(download: DownloadMpp, downloadBlocks: List<DownloadBlockMpp>, totalBlocks: Int)

    override fun onProgress(download: DownloadMpp, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long)

    override fun onPaused(download: DownloadMpp)

    override fun onResumed(download: DownloadMpp)

    override fun onCancelled(download: DownloadMpp)

    override fun onRemoved(download: DownloadMpp)

    override fun onDeleted(download: DownloadMpp)
}