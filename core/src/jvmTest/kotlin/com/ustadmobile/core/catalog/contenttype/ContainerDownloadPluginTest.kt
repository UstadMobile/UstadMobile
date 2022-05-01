package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.ContainerDownloadTestCommon.Companion.makeDownloadJobAndJobItem
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerManifest
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.retriever.Retriever
import com.ustadmobile.retriever.RetrieverRequest
import com.ustadmobile.retriever.RetrieverStatusUpdateEvent
import com.ustadmobile.retriever.fetcher.RetrieverListener
import com.ustadmobile.retriever.io.FileChecksums
import com.ustadmobile.retriever.io.parseIntegrity
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.singleton
import org.mockito.kotlin.*
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ContainerDownloadPluginTest {


    private lateinit var clientDb: UmAppDatabase

    private lateinit var clientRepo: UmAppDatabase

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    private lateinit var downloadDestDir: File

    private lateinit var clientDi: DI

    private lateinit var mockRetriever: Retriever

    private lateinit var endpoint: Endpoint

    private lateinit var containerEntriesList: List<ContainerEntryWithContainerEntryFile>

    private lateinit var containerEntryUrlList: List<String>

    private lateinit var container: Container

    @Before
    fun setup() {
        downloadDestDir = temporaryFolder.newFolder()

        mockRetriever = mock { }
        clientDi = DI {
            import(ustadTestRule.diModule)
            bind<Retriever>() with singleton {
                mockRetriever
            }
        }

        clientDb = clientDi.directActiveDbInstance()
        clientRepo = clientDi.directActiveRepoInstance()
        endpoint = clientDi.activeEndpoint()

        container = Container().apply {
            fileSize = 10000
            containerUid = clientDb.containerDao.insert(this)
        }

        val md5Digest = MessageDigest.getInstance("MD5")
        val sha256Digest = MessageDigest.getInstance("SHA-256")

        //To mix up the list - add at least one item that shares the same md5
        containerEntriesList = (0..19).map { index ->
            val cefData = "ContainerEntryWithContainerEntryFile-$index"
            val defBytes = cefData.toByteArray()
            ContainerEntryWithContainerEntryFile().also {
                it.cePath = "entry-$index"
                it.ceContainerUid = container.containerUid
                it.containerEntryFile = ContainerEntryFile().also { ceFile ->
                    ceFile.cefMd5 = md5Digest.digest(defBytes).encodeBase64()
                    ceFile.ceTotalSize = defBytes.size.toLong()
                    ceFile.ceCompressedSize = defBytes.size.toLong()
                    ceFile.cefIntegrity = "sha256-${sha256Digest.digest(defBytes).encodeBase64()}"
                }
            }
        }

        containerEntryUrlList = containerEntriesList.map {
            endpoint.url(
                "/Container/FileByMd5/${URLEncoder.encode(it.containerEntryFile!!.cefMd5, "UTF-8")}")
        }
    }

    private fun ContainerEntryWithContainerEntryFile.assertIsMatchingInClientDb() {
        val entryInDb = clientDb.containerEntryDao.findByPathInContainer(
            container.containerUid, cePath!!)
        assertNotNull(entryInDb, "Entry is database is not null")
        assertEquals(containerEntryFile?.cefIntegrity,
            entryInDb.containerEntryFile?.cefIntegrity,
            "Entry in db has matching integrity")
        assertEquals(containerEntryFile?.cefMd5,
            entryInDb.containerEntryFile?.cefMd5,
            "Entry in db has matching MD5")
        assertEquals(containerEntryFile?.ceCompressedSize,
            entryInDb.containerEntryFile?.ceCompressedSize,
            "Entry in db has matching compressed size")
        assertEquals(containerEntryFile?.ceTotalSize,
            entryInDb.containerEntryFile?.ceTotalSize,
            "Entry in db has matching total size")
        assertEquals(containerEntryFile?.cefCrc32,
            entryInDb.containerEntryFile?.cefCrc32,
            "Entry in db has matching CRC32")
    }

    @Suppress("UNCHECKED_CAST") //No way to avoid this on a mock invocation
    @Test
    fun givenValidContentEntryUid_whenProcessJobCalled_thenShouldUseRetrieverToGetManifestThenFiles() {
        val downloadPlugin = ContainerDownloadPlugin(Any(), endpoint, clientDi)
        val contentEntry = ContentEntry().apply {
            contentEntryUid = clientDb.contentEntryDao.insert(this)
        }


        val manifestUrl = endpoint.url("/Container/Manifest/${container.containerUid}")
        val jobAndJobItem = makeDownloadJobAndJobItem(contentEntry, container, downloadDestDir,
            clientDb)

        mockRetriever.stub {
            onBlocking {
                retrieve(argWhere { requestList ->
                    requestList.any { it.originUrl == manifestUrl }
                }, any())
            }.thenAnswer {
                val requestArgs = it.arguments.first() as List<RetrieverRequest>
                val containerManifest = ContainerManifest.fromContainerEntryWithContainerEntryFiles(
                    containerEntriesList)
                File(requestArgs.first().destinationFilePath)
                    .writeText(containerManifest.toManifestString())
            }

            onBlocking {
                retrieve(argWhere { requestList ->
                    !requestList.any { it.originUrl == manifestUrl }
                }, any())
            }.thenAnswer {
                val requestList = it.arguments[0] as List<RetrieverRequest>
                val listener = it.arguments[1] as RetrieverListener
                requestList.forEachIndexed { index, request ->
                    val containerEntry = containerEntriesList.find {
                        it.containerEntryFile?.cefIntegrity == request.sriIntegrity
                    } ?: throw IllegalArgumentException("Unknown entry")
                    val integrity = parseIntegrity(
                        containerEntry.containerEntryFile!!.cefIntegrity!!)

                    val checksums = FileChecksums(integrity.second, null, null,
                        containerEntry.containerEntryFile?.cefCrc32 ?: -1L)
                    runBlocking {
                        listener.onRetrieverStatusUpdate(RetrieverStatusUpdateEvent(index,
                            request.originUrl, Retriever.STATUS_SUCCESSFUL, checksums))
                    }
                }
                Unit
            }
        }

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)

        val processResult = runBlocking {
            downloadPlugin.processJob(jobAndJobItem, processContext, mock { })
        }

        assertEquals(JobStatus.COMPLETE, processResult.status)

        verifyBlocking(mockRetriever) {
            retrieve(argWhere { requestList ->
                requestList.any { it.originUrl == manifestUrl }
            }, any())
        }

        verifyBlocking(mockRetriever) {
            retrieve(argWhere { requestList ->
                requestList.map { it.originUrl }.containsAll(containerEntryUrlList)
            }, any())
        }

        //verify that the entries are added to the database
        containerEntriesList.forEach {  entry ->
            entry.assertIsMatchingInClientDb()
        }
    }

    fun givenSomeContainerFileMd5sAlreadyPresent_whenProcessJobCalled_thenShouldOnlyDownloadMissingEntries() {

    }

    fun givenSomeFilesAlreadyDownloadedInDir_whenProcessJobCalled_thenShouldOnlyDownloadRemainingEntries() {

    }

    fun givenNoContainerAvailable_whenProcessJobCalled_thenNotDownloadAnythingAndFail() {

    }





}