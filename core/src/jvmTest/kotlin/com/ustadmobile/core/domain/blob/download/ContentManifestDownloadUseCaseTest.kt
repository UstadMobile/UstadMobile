package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.ContentManifestEntry
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.requireFileSeparatorSuffix
import com.ustadmobile.core.util.newTestHttpClient
import com.ustadmobile.core.util.newTestOkHttpClient
import com.ustadmobile.core.util.stringvalues.asIStringValues
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.CacheLockJoin
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.TransferJob
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ContentManifestDownloadUseCaseTest {

    private lateinit var mockEnqueueBlobDownloadUseCase: EnqueueBlobDownloadClientUseCase

    private lateinit var httpClient: HttpClient

    private lateinit var db: UmAppDatabase

    private lateinit var mockWebServer: MockWebServer

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @BeforeTest
    fun setup() {
        val okHttpClient = newTestOkHttpClient(temporaryFolder, json = json)
        httpClient = okHttpClient.newTestHttpClient(json)
        db = DatabaseBuilder.databaseBuilder(
            dbClass = UmAppDatabase::class,
            dbUrl = "jdbc:sqlite::memory:",
            nodeId = 1L
        ).build()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        mockEnqueueBlobDownloadUseCase = mock { }
    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
        db.close()
        mockWebServer.shutdown()
    }

    @Test
    fun givenContentEntryVersionUidAndManifest_whenInvoked_thenWillCreateLockJoinsAndEnqueueBlobDownloadForAllItemsInManifest() {
        val manifest = ContentManifest(
            version = 1,
            metadata = emptyMap(),
            entries = (0..10).map { index ->
                ContentManifestEntry(
                    uri = "file/$index",
                    storageSize = 100,
                    bodyDataUrl = "http://server.com/$index",
                    integrity = "",
                    responseHeaders = emptyMap<String, List<String>>().asIStringValues()
                )
            }
        )

        val offlineItemUid = 1L

        mockWebServer.enqueue(
            MockResponse().setBody(json.encodeToString(manifest))
                .addHeader("content-type", "application/json")
        )

        val contentEntryVersionUid = runBlocking {
            db.contentEntryVersionDao().insertAsync(ContentEntryVersion(
                cevManifestUrl = mockWebServer.url("/manifest.json").toString()
            ))
        }

        val transferJobId = runBlocking {
            db.transferJobDao().insert(
                TransferJob(
                    tjOiUid = offlineItemUid,
                    tjType = TransferJob.TYPE_DOWNLOAD
                )
            ).toInt()
        }

        val tmpResponseFolder = temporaryFolder.newFolder("cache-tmp-partial-responses")
        val useCase = ContentManifestDownloadUseCase(
            enqueueBlobDownloadClientUseCase = mockEnqueueBlobDownloadUseCase,
            db = db,
            httpClient = httpClient,
            json = json,
            cacheTmpPath = { tmpResponseFolder.absolutePath.requireFileSeparatorSuffix() }
        )

        runBlocking {
            useCase(
                contentEntryVersionUid, transferJobId
            )

            verifyBlocking(mockEnqueueBlobDownloadUseCase) {
                invoke(
                    items = argWhere { downloadItems ->
                        manifest.entries.all { manifestEntry ->
                            downloadItems.any { it.url == manifestEntry.bodyDataUrl }
                        }
                    },
                    existingTransferJobId = eq(transferJobId)
                )
            }

            val cacheLockJoins = db.cacheLockJoinDao().findByTableIdAndEntityUid(
                tableId = ContentEntryVersion.TABLE_ID,
                entityUid = contentEntryVersionUid
            )

            manifest.entries.all { manifestEntry ->
                cacheLockJoins.any { cacheLockJoin ->
                    cacheLockJoin.cljUrl == manifestEntry.bodyDataUrl &&
                            cacheLockJoin.cljOiUid == offlineItemUid &&
                            cacheLockJoin.cljStatus == CacheLockJoin.STATUS_PENDING_CREATION
                }
            }

        }
    }

}