package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem

abstract class AbstractEnqueueContentManifestDownloadUseCase(
    private val db: UmAppDatabase
): EnqueueContentManifestDownloadUseCase {

    /**
     *
     */
    protected suspend fun createTransferJob(
        contentEntryVersionUid: Long,
        offlineItemUid: Long,
    ): TransferJob {
        return db.withDoorTransactionAsync {
            val contentEntryVersion = db.contentEntryVersionDao().findByUidAsync(
                contentEntryVersionUid) ?: throw IllegalArgumentException(
                "Enqueue: Could not find ContentEntryVersion $contentEntryVersionUid")

            val transferJob = TransferJob(
                tjType = TransferJob.TYPE_DOWNLOAD,
                tjStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                tjTimeCreated = systemTimeInMillis(),
                tjEntityUid = contentEntryVersionUid,
                tjTableId = ContentEntryVersion.TABLE_ID,
                tjOiUid = offlineItemUid,
            )
            val jobUid = db.transferJobDao().insert(transferJob).toInt()
            val manifestTransferJobItem = TransferJobItem(
                tjiTjUid = jobUid,
                tjiSrc = contentEntryVersion.cevManifestUrl,
                tjiEntityUid = contentEntryVersion.cevUid,
                tjiTableId = ContentEntryVersion.TABLE_ID,
            )
            db.transferJobItemDao().insert(manifestTransferJobItem)

            transferJob.copy(
                tjUid = jobUid
            )
        }
    }

    companion object {

        const val DATA_ENDPOINT = "endpoint"

        const val DATA_JOB_UID = "jobUid"

        const val DATA_CONTENTENTRYVERSION_UID = "cevUid"

        const val UNIQUE_NAME_PREFIX = "contentmanifest-download-"

        fun uniqueNameFor(endpoint: Endpoint, transferJobId: Int) : String {
            return "$UNIQUE_NAME_PREFIX${endpoint.url}-$transferJobId"
        }

    }

}