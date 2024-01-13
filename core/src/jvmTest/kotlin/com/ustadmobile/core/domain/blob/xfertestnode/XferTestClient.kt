package com.ustadmobile.core.domain.blob.xfertestnode

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportersDiModuleJvm
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.flow.filter
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import java.io.Closeable
import kotlin.time.Duration.Companion.seconds

/**
 * Contains the dependencies required to act as a client for blob transfer test purposes e.g.
 * UploadClient, EnqueueBlobUploadClient, ChunkedUploadClient, etc. These dependencies can then be
 * tested with XferTestServer.
 */
class XferTestClient(
    val node: XferTestNode,
) : Closeable {

    val di: DI

    init {
        di = DI {
            extend(node.di)

            bind<Scheduler>() with singleton {
                val schedulerFactory = StdSchedulerFactory()
                schedulerFactory.scheduler.also {
                    it.context.put("di", di)
                }
            }

            bind<ChunkedUploadClientUseCaseKtorImpl>() with scoped(node.endpointScope).singleton {
                ChunkedUploadClientUseCaseKtorImpl(
                    node.httpClient, node.uriHelper
                )
            }

            bind<BlobUploadClientUseCase>() with scoped(node.endpointScope).singleton {
                BlobUploadClientUseCaseJvm(
                    chunkedUploadUseCase = instance<ChunkedUploadClientUseCaseKtorImpl>(),
                    httpClient = node.httpClient,
                    httpCache = node.httpCache,
                    db = instance(tag = DoorTag.TAG_DB),
                    repo = instance(tag = DoorTag.TAG_DB), //TODO: Change this to actual repo
                    endpoint = context,
                )
            }

            bind<UpdateFailedTransferJobUseCase>() with scoped(node.endpointScope).singleton {
                UpdateFailedTransferJobUseCase(db = instance(tag = DoorTag.TAG_DB))
            }

            bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(node.endpointScope).singleton {
                SaveLocalUriAsBlobAndManifestUseCaseJvm(
                    saveLocalUrisAsBlobsUseCase = instance()
                )
            }

            bind<EnqueueBlobUploadClientUseCase>() with scoped(node.endpointScope).singleton {
                EnqueueBlobUploadClientUseCaseJvm(
                    scheduler = instance(),
                    endpoint = context,
                    db = instance(tag = DoorTag.TAG_DB),
                    cache = instance()
                )
            }

            import(ContentImportersDiModuleJvm)

            bind<ImportContentEntryUseCase>() with scoped(node.endpointScope).singleton {
                ImportContentEntryUseCase(
                    db = instance(tag = DoorTag.TAG_DB),
                    importersManager = instance(),
                    enqueueBlobUploadClientUseCase = instance<EnqueueBlobUploadClientUseCase>(),
                    httpClient = node.httpClient,
                )
            }

            onReady {
                instance<Scheduler>().start()
            }
        }
    }


    suspend fun waitForContentUploadCompletion(
        endpoint: Endpoint,
        contentEntryVersionUid: Long,
        timeout: Int = 15,
    ) {
        val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
        val transferJobFlow = db.doorFlow(arrayOf("TransferJob", "TransferJobItem")) {
            db.transferJobDao.findJobByEntityAndTableUid(
                tableId = ContentEntryVersion.TABLE_ID,
                entityUid = contentEntryVersionUid,
            )
        }

        transferJobFlow.filter {
            it.size == 1 && it.first().tjStatus == TransferJobItemStatus.STATUS_COMPLETE_INT
        }.test(timeout = timeout.seconds, name = "Transfer job for #$contentEntryVersionUid should complete") {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    override fun close() {
        di.direct.instance<Scheduler>().shutdown()
        node.close()
    }

}