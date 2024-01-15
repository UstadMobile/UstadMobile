package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.blob.BlobTransferProgressUpdate
import com.ustadmobile.core.domain.blob.BlobTransferStatusUpdate
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import com.ustadmobile.core.io.await
import io.github.aakira.napier.Napier
import kotlinx.coroutines.isActive
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.transferjobitem.TransferJobItemStatusUpdater
import com.ustadmobile.door.ext.withDoorTransactionAsync
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class BlobDownloadClientUseCaseCommonJvm(
    private val okHttpClient: OkHttpClient,
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?,
) : BlobDownloadClientUseCase{

    data class DownloadQueueItem(
        val transferJobItem: BlobTransferJobItem,
    )

    private suspend fun downloadItemsFromChannelProcessor(
        channel: ReceiveChannel<DownloadQueueItem>,
        onProgress: (BlobTransferProgressUpdate) -> Unit,
        onStatusUpdate: (BlobTransferStatusUpdate) -> Unit
    ) = coroutineScope {
        val buffer = ByteArray(8192)
        async {
            for(queueItem in channel) {
                val logPrefix = "BlobDownloadClientUseCaseCommonJvm: ${queueItem.transferJobItem.blobUrl}"
                //Pull the item through OkHttp. This will pull it through the lib-cache interceptor.
                Napier.v { "$logPrefix : start download"}
                try {
                    onStatusUpdate(
                        BlobTransferStatusUpdate(
                            transferItem = queueItem.transferJobItem,
                            status = TransferJobItemStatus.STATUS_IN_PROGRESS_INT,
                        )
                    )

                    val request = Request.Builder()
                        .url(queueItem.transferJobItem.blobUrl)
                        .build()

                    val response = okHttpClient.newCall(request).await()

                    var totalBytesRead = 0L
                    var bytesRead = 0

                    response.body?.byteStream()?.use { inStream ->
                        while(isActive && inStream.read(buffer).also { bytesRead = it } != -1) {
                            totalBytesRead += bytesRead
                            onProgress(
                                BlobTransferProgressUpdate(
                                    transferItem = queueItem.transferJobItem,
                                    bytesTransferred = totalBytesRead
                                )
                            )
                        }
                    }

                    Napier.v { "$logPrefix : completed"}
                    onStatusUpdate(
                        BlobTransferStatusUpdate(
                            transferItem = queueItem.transferJobItem,
                            status = TransferJobItemStatus.STATUS_COMPLETE_INT,
                        )
                    )
                }catch(e: Throwable) {
                    Napier.i("$logPrefix : Exception downloading", e)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        items: List<BlobTransferJobItem>,
        onProgress: (BlobTransferProgressUpdate) -> Unit,
        onStatusUpdate: (BlobTransferStatusUpdate) -> Unit
    ) {
        coroutineScope {
            val receiveChannel = produce(
                capacity = Channel.UNLIMITED
            ) {
                items.forEach { send(DownloadQueueItem(it)) }
                close()
            }

            val jobs = (0..4).map {
                downloadItemsFromChannelProcessor(
                    channel = receiveChannel,
                    onProgress = onProgress,
                    onStatusUpdate = onStatusUpdate,
                )
            }

            jobs.awaitAll()
        }
    }

    override suspend fun invoke(transferJobUid: Int) {
        val logPrefix = "BlobDownloadClientUseCaseCommonJvm (#$transferJobUid)"
        val transferJob = db.transferJobDao.findByUid(transferJobUid)
            ?: throw IllegalArgumentException("$logPrefix: TransferJob #$transferJobUid does not exist")
        val transferJobItems = db.transferJobItemDao.findByJobUid(transferJobUid)

        coroutineScope {
            //Here, if needed, can check size of items where size is unknown use a head request
            //Should update status of the job itself

            val transferItems = transferJobItems.map {
                BlobTransferJobItem(
                    blobUrl = it.tjiSrc!!,
                    transferJobItemUid = it.tjiUid,
                )
            }
            val transferJobItemStatusUpdater = TransferJobItemStatusUpdater(db, repo, this)
            try {
                invoke(
                    items = transferItems,
                    onProgress = transferJobItemStatusUpdater::onProgressUpdate,
                    onStatusUpdate = transferJobItemStatusUpdater::onStatusUpdate,
                )

                val numIncompleteItems = db.withDoorTransactionAsync {
                    transferJobItemStatusUpdater.commit(transferJobUid)
                    transferJobItemStatusUpdater.onFinished()
                    db.transferJobItemDao.findNumberJobItemsNotComplete(transferJobUid)
                }

                if(numIncompleteItems != 0) {
                    throw IllegalStateException("BlobDownloadClientUseCaseCommonJvm: not complete.")
                }
                Napier.d { "$logPrefix complete!"}
            }catch(e: Throwable) {
                Napier.e("$logPrefix Exception. Attempt has failed", e)
                withContext(NonCancellable) {
                    transferJobItemStatusUpdater.onFinished()
                }

                throw e
            }
        }

    }
}