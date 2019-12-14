package com.ustadmobile.core.networkmanager.downloadmanager

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*

interface ContainerDownloadManager {

    suspend fun getDownloadJobItemByJobItemUid(jobItemUid: Int): DoorLiveData<DownloadJobItem?>

    suspend fun getDownloadJobItemByContentEntryUid(contentEntryUid: Int): DoorLiveData<DownloadJobItem?>

    suspend fun createDownloadJob(downloadJob: DownloadJob)

    suspend fun addItemsToDownloadJob(newItems: List<DownloadJobItemWithParents>)

    suspend fun handleDownloadJobItemUpdated(downloadJobItem: DownloadJobItem)

    suspend fun enqueue(downloadJobId: Int)

    suspend fun pause(downloadJobId: Int)

    suspend fun cancel(downloadJobId: Int)

    suspend fun setMeteredDataAllowed(downloadJobUid: Int, meteredDataAllowed: Boolean)

    suspend fun handleConnectivityChanged(status: ConnectivityStatus)

}