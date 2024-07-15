package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.OfflineItem

/**
 * Entry point to make a content entry available offline. This will:
 *  1) Create an OfflineItem in the database
 *  2) Invoke EnqueueContentManifestDownloadUseCase
 */
class MakeContentEntryAvailableOfflineUseCase(
    private val repo: UmAppDatabase,
    private val nodeIdAndAuth: NodeIdAndAuth,
    private val enqueueContentManifestDownloadUseCase: EnqueueContentManifestDownloadUseCase,
) {

    suspend operator fun invoke(contentEntryUid: Long) {
        val latestContentEntryVersion = repo.contentEntryVersionDao()
            .findLatestVersionUidByContentEntryUidEntity(contentEntryUid)

        repo.withDoorTransactionAsync {
            val offlineItemUid = repo.offlineItemDao().insertAsync(
                OfflineItem(
                    oiNodeId = nodeIdAndAuth.nodeId,
                    oiContentEntryUid = contentEntryUid,
                    oiActive = true,
                )
            )

            if(latestContentEntryVersion != null) {
                enqueueContentManifestDownloadUseCase(
                    contentEntryVersionUid = latestContentEntryVersion.cevUid,
                    offlineItemUid = offlineItemUid,
                )
            }
        }
    }

}