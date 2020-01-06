package com.ustadmobile.sharedse.network.ext

import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin
import com.ustadmobile.lib.db.entities.DownloadJobItemWithParents

suspend fun ContainerDownloadManager.addTestRootDownload(contentEntryUid: Long = 1): Pair<DownloadJob, DownloadJobItem> {
    val newDownloadJob = DownloadJob(contentEntryUid, System.currentTimeMillis())
    createDownloadJob(newDownloadJob)

    val rootDownloadJobItem = DownloadJobItemWithParents(newDownloadJob, contentEntryUid, 0L, 0L,
            mutableListOf())
    addItemsToDownloadJob(listOf(rootDownloadJobItem))

    return Pair(newDownloadJob, rootDownloadJobItem)
}

suspend fun ContainerDownloadManager.addTestChildDownload(parent: DownloadJobItem, child: DownloadJobItem): DownloadJobItem {
    val newChildItem = DownloadJobItemWithParents(mutableListOf(DownloadJobItemParentChildJoin(parent.djiUid, 0,
    0L))).also {
        it.downloadLength = child.downloadLength
        it.downloadedSoFar = child.downloadedSoFar
        it.djiStatus = child.djiStatus
        it.djiContentEntryUid = child.djiContentEntryUid
        it.djiContainerUid = child.djiContainerUid
        it.djiDjUid = parent.djiDjUid
    }

    this.addItemsToDownloadJob(listOf(newChildItem))

    return newChildItem
}