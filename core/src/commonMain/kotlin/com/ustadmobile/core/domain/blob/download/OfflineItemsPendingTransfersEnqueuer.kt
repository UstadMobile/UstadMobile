package com.ustadmobile.core.domain.blob.download

/**
 * Observes the OfflineItemPendingTransferJob table, where rows are created by triggers when
 * entities related to OfflineItem (e.g. ContentEntryVersion) are updated. This ensures that where
 * new data with related blobs enters the database where the OfflineItem indicates that the user
 * wants it offline, then a transferjob will be setup to handle it.
 */
@Suppress("unused") //Reserved for future use
class OfflineItemsPendingTransfersEnqueuer {
}