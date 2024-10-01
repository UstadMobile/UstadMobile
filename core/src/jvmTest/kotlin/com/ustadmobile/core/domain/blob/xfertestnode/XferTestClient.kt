package com.ustadmobile.core.domain.blob.xfertestnode

import app.cash.turbine.test
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.LearningSpaceScope
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
import com.ustadmobile.core.domain.upload.DEFAULT_CHUNK_SIZE
import com.ustadmobile.core.util.ext.getOrGenerateNodeIdAndAuth
import com.ustadmobile.core.util.ext.retryWait
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import io.github.aakira.napier.Napier
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
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

/**
 * Contains the dependencies required to act as a client for blob transfer test purposes e.g.
 * UploadClient, EnqueueBlobUploadClient, ChunkedUploadClient, etc. These dependencies can then be
 * tested with XferTestServer.
 *
 * @param schedulerRetryWait The retry time. In production this is 10 seconds, for testing, this is
 * 500ms by default.
 * @param uploadChunkSize the chunk size for uploads. This can be reduced to allow testing of resumption
 * with smaller uploads.
 */
class XferTestClient(
    val node: XferTestNode,
    schedulerRetryWait: Long = 500,
    uploadChunkSize: Int = DEFAULT_CHUNK_SIZE,
) : Closeable {

    val di: DI

    private val reposToClose = mutableListOf<UmAppDatabase>()

    init {
        di = DI {
            extend(node.di)

            bind<Scheduler>() with singleton {
                val schedulerFactory = StdSchedulerFactory()
                schedulerFactory.scheduler.also {
                    it.context.put("di", di)
                    it.retryWait = schedulerRetryWait
                }
            }

            bind<ChunkedUploadClientUseCaseKtorImpl>() with scoped(node.learningSpaceScope).singleton {
                ChunkedUploadClientUseCaseKtorImpl(
                    node.httpClient, node.uriHelper
                )
            }

            bind<NodeIdAndAuth>() with scoped(LearningSpaceScope.Default).singleton {
                val settings: Settings = instance()
                val contextIdentifier: String = sanitizeDbNameFromUrl(context.url)
                settings.getOrGenerateNodeIdAndAuth(contextIdentifier)
            }

            bind<Settings>() with singleton {
                PropertiesSettings(
                    delegate = Properties(),
                    onModify = {
                        //do nothing
                    }
                )
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(node.learningSpaceScope).singleton {
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
                db.asRepository(
                    RepositoryConfig.repositoryConfig(
                        context = Any(),
                        endpoint = "${context.url}UmAppDatabase/",
                        nodeId = nodeIdAndAuth.nodeId,
                        auth = nodeIdAndAuth.auth,
                        httpClient = instance(),
                        okHttpClient = instance(),
                        json = instance()
                    )
                ).also {
                    reposToClose.add(it)
                }
            }

            bind<BlobUploadClientUseCase>() with scoped(node.learningSpaceScope).singleton {
                BlobUploadClientUseCaseJvm(
                    chunkedUploadUseCase = instance<ChunkedUploadClientUseCaseKtorImpl>(),
                    httpClient = node.httpClient,
                    httpCache = node.httpCache,
                    json = instance(),
                    db = instance(tag = DoorTag.TAG_DB),
                    repo = instance(tag = DoorTag.TAG_REPO),
                    learningSpace = context,
                    chunkSize = uploadChunkSize,
                )
            }

            bind<UpdateFailedTransferJobUseCase>() with scoped(node.learningSpaceScope).singleton {
                UpdateFailedTransferJobUseCase(db = instance(tag = DoorTag.TAG_DB))
            }

            bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(node.learningSpaceScope).singleton {
                SaveLocalUriAsBlobAndManifestUseCaseJvm(
                    saveLocalUrisAsBlobsUseCase = instance(),
                    mimeTypeHelper = FileMimeTypeHelperImpl(),
                )
            }

            bind<EnqueueBlobUploadClientUseCase>() with scoped(node.learningSpaceScope).singleton {
                EnqueueBlobUploadClientUseCaseJvm(
                    scheduler = instance(),
                    learningSpace = context,
                    db = instance(tag = DoorTag.TAG_DB),
                    cache = instance()
                )
            }

            import(ContentImportersDiModuleJvm)

            bind<ImportContentEntryUseCase>() with scoped(node.learningSpaceScope).singleton {
                ImportContentEntryUseCase(
                    db = instance(tag = DoorTag.TAG_DB),
                    importersManager = instance(),
                    enqueueBlobUploadClientUseCase = instance<EnqueueBlobUploadClientUseCase>(),
                    createRetentionLocksForManifestUseCase = instance(),
                    httpClient = node.httpClient,
                    json = instance(),
                )
            }

            onReady {
                instance<Scheduler>().start()
            }
        }
    }


    suspend fun waitForContentUploadCompletion(
        learningSpace: LearningSpace,
        contentEntryVersionUid: Long,
        timeout: Int = 15,
    ) {
        val db: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
        val transferJobFlow = db.doorFlow(arrayOf("TransferJob", "TransferJobItem")) {
            db.transferJobDao().findJobByEntityAndTableUid(
                tableId = ContentEntryVersion.TABLE_ID,
                entityUid = contentEntryVersionUid,
            )
        }

        transferJobFlow.filter {
            it.size == 1 && it.first().tjStatus == TransferJobItemStatus.STATUS_COMPLETE_INT
        }.test(timeout = timeout.seconds, name = "Transfer job for #$contentEntryVersionUid should complete") {
            val transferJob = awaitItem().first()
            Napier.d("XferTestClient: TransferJob #${transferJob.tjUid} is complete(status = ${transferJob.tjStatus})")
            cancelAndIgnoreRemainingEvents()
        }
    }

    override fun close() {
        di.direct.instance<Scheduler>().shutdown()
        reposToClose.forEach {
            it.close()
        }
        node.close()
    }

}