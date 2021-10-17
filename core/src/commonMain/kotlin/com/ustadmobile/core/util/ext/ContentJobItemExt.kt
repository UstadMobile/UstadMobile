package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.db.entities.ContentJobItem

private fun Int.isStatusQueuedOrDownloading() = this >= JobStatus.WAITING_MIN && this < JobStatus.COMPLETE_MIN

private fun Int.isStatusPaused() = this == JobStatus.PAUSED

private fun Int.isStatusCompletedSuccessfully() = this == JobStatus.COMPLETE

private fun Int.isStatusCompleted() = this >= JobStatus.COMPLETE_MIN

private fun Int.isStatusPausedOrQueuedOrDownloading() = this >= JobStatus.PAUSED && this < JobStatus.COMPLETE_MIN

fun ContentJobItem?.isStatusQueuedOrDownloading() = this?.cjiRecursiveStatus?.isStatusQueuedOrDownloading() ?: false

fun ContentJobItem?.isStatusPaused() = this?.cjiRecursiveStatus?.isStatusPaused() ?: false

fun ContentJobItem?.isStatusCompletedSuccessfully() = this?.cjiRecursiveStatus?.isStatusCompletedSuccessfully() ?: false

fun ContentJobItem?.isStatusCompleted() = this?.cjiRecursiveStatus?.isStatusCompleted() ?: false

fun ContentJobItem?.isStatusPausedOrQueuedOrDownloading() = this?.cjiRecursiveStatus?.isStatusPausedOrQueuedOrDownloading() ?: false