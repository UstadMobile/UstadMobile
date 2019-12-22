package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.DownloadJob
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemWithParents

/**
 * Set of convenience extension functions that help make status conditions more readable.
 */

private fun Int.isStatusQueuedOrDownloading() = this >= JobStatus.WAITING_MIN && this < JobStatus.COMPLETE_MIN

private fun Int.isStatusPaused() = this == JobStatus.PAUSED

private fun Int.isStatusCompletedSuccessfully() = this == JobStatus.COMPLETE

private fun Int.isStatusCompleted() = this >= JobStatus.COMPLETE_MIN

private fun Int.isStatusPausedOrQueuedOrDownloading() = this >= JobStatus.PAUSED && this < JobStatus.COMPLETE_MIN

fun DownloadJobItem?.isStatusQueuedOrDownloading() = this?.djiStatus?.isStatusQueuedOrDownloading() ?: false

fun DownloadJobItem?.isStatusPaused() = this?.djiStatus?.isStatusPaused() ?: false

fun DownloadJobItem?.isStatusCompletedSuccessfully() = this?.djiStatus?.isStatusCompletedSuccessfully() ?: false

fun DownloadJobItem?.isStatusCompleted() = this?.djiStatus?.isStatusCompleted() ?: false

fun DownloadJobItem?.isStatusPausedOrQueuedOrDownloading() = this?.djiStatus?.isStatusPausedOrQueuedOrDownloading() ?: false

fun DownloadJob?.isStatusQueuedOrDownloading() = this?.djStatus?.isStatusQueuedOrDownloading() ?: false

fun DownloadJob?.isStatusPaused() = this?.djStatus?.isStatusPaused() ?: false

fun DownloadJob?.isStatusCompletedSuccessfully() = this?.djStatus?.isStatusCompletedSuccessfully() ?: false

fun DownloadJob?.isStatusCompleted() = this?.djStatus?.isStatusCompleted() ?: false

/**
 * Make the root DownloadJobItem for the given DownloadJob
 */
fun DownloadJob.makeRootDownloadJobItem(rootContainer: Container?): DownloadJobItemWithParents {
    return DownloadJobItemWithParents(
            this, this.djRootContentEntryUid,
            rootContainer?.containerUid ?: 0,
            rootContainer?.fileSize ?: 0,
            mutableListOf())
}