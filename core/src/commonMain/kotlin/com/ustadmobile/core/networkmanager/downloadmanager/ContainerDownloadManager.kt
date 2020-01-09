package com.ustadmobile.core.networkmanager.downloadmanager

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

abstract class ContainerDownloadManager {

    abstract suspend fun getDownloadJobItemByJobItemUid(jobItemUid: Int): DoorLiveData<DownloadJobItem?>

    @JsName("getDownloadJobItemByContentEntryUid")
    abstract suspend fun getDownloadJobItemByContentEntryUid(contentEntryUid: Long): DoorLiveData<DownloadJobItem?>

    abstract suspend fun getDownloadJob(jobUid: Int): DoorLiveData<DownloadJob?>

    abstract suspend fun getDownloadJobRootItem(jobUid: Int): DownloadJobItem?

    abstract suspend fun createDownloadJob(downloadJob: DownloadJob)

    abstract suspend fun addItemsToDownloadJob(newItems: List<DownloadJobItemWithParents>)

    abstract suspend fun handleDownloadJobItemUpdated(downloadJobItem: DownloadJobItem, autoCommit: Boolean = true)

    /**
     * This is only for handling changes that have been made externally (e.g. change to save directory)
     */
    abstract suspend fun handleDownloadJobUpdated(downloadJob: DownloadJob)

    abstract suspend fun enqueue(downloadJobId: Int)

    abstract suspend fun pause(downloadJobId: Int)

    abstract suspend fun cancel(downloadJobId: Int)

    abstract suspend fun setMeteredDataAllowed(downloadJobUid: Int, meteredDataAllowed: Boolean)

    abstract suspend fun handleConnectivityChanged(status: ConnectivityStatus)

    abstract suspend fun commit()

    abstract val connectivityLiveData: DoorLiveData<ConnectivityStatus?>

    fun determineParentStatusFromChildStatuses(childStatuses: List<DownloadJobItemUidAndStatus>): Int {
        return when {
            childStatuses.all { it.djiStatus > JobStatus.COMPLETE } -> {
                childStatuses.maxBy { it.djiStatus }?.djiStatus ?: JobStatus.FAILED
            }
            childStatuses.any { it.djiStatus == JobStatus.RUNNING } -> JobStatus.RUNNING
            childStatuses.any { it.djiStatus == JobStatus.QUEUED } -> JobStatus.QUEUED
            else -> childStatuses.minBy { it.djiStatus }?.djiStatus ?: 0
        }
    }

}