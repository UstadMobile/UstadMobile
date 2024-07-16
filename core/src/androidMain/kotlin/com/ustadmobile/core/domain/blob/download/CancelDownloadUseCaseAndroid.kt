package com.ustadmobile.core.domain.blob.download

import android.content.Context
import androidx.work.WorkManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import io.github.aakira.napier.Napier

class CancelDownloadUseCaseAndroid(
    private val appContext: Context,
    private val endpoint: Endpoint,
    private val db: UmAppDatabase,
): CancelDownloadUseCase {

    override suspend fun invoke(
        transferJobId: Int,
        offlineItemUid: Long,
    ) {
        Napier.i("Canceling download: $transferJobId / $offlineItemUid")

        //This will cancel both the download content entry manifest job and the blob download job.
        WorkManager.getInstance(appContext).cancelAllWorkByTag("offlineitem-${endpoint.url}-${offlineItemUid}")

        //mark transferJob as cancelled and offline item as inactive (which will release any retention locks)
        db.withDoorTransactionAsync {
            db.transferJobDao().updateStatus(transferJobId, TransferJobItemStatus.STATUS_CANCELLED)
            db.offlineItemDao().updateActiveByOfflineItemUid(offlineItemUid, false)
        }


    }
}