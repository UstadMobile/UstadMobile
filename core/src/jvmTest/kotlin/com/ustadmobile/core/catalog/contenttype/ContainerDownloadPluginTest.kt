package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.ContainerDownloadTestCommon.Companion.makeDownloadJobAndJobItem
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerManifest
import com.ustadmobile.core.io.ext.FILE_EXTENSION_CE_JSON
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.retriever.Retriever
import com.ustadmobile.retriever.RetrieverRequest
import com.ustadmobile.retriever.RetrieverStatusUpdateEvent
import com.ustadmobile.retriever.fetcher.RetrieverListener
import com.ustadmobile.retriever.io.FileChecksums
import com.ustadmobile.retriever.io.parseIntegrity
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
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

    private lateinit var manifestUrl: String

    private lateinit var contentEntry: ContentEntry

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

        contentEntry = ContentEntry().apply {
            contentEntryUid = clientDb.contentEntryDao.insert(this)
        }

        container = Container().apply {
            fileSize = 10000
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = clientDb.containerDao.insert(this)
        }

        manifestUrl = endpoint.url("/Container/Manifest/${container.containerUid}")

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

        //Add a single md5 which has multiple entries in the same container (edge case, but it happens)
        containerEntriesList[containerEntriesList.size - 1].containerEntryFile =
            containerEntriesList[containerEntriesList.size - 2].containerEntryFile

        containerEntryUrlList = containerEntriesList.map {
            it.downloadUrl()
        }.distinct()
    }

    private fun ContainerEntryWithContainerEntryFile.downloadUrl() : String{
        return endpoint.url(
            "/Container/FileByMd5/${URLEncoder.encode(containerEntryFile!!.cefMd5, "UTF-8")}")
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

    fun Retriever.onRequestManifestAnswerWriteFile() {
        stub {
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
        }
    }

    fun Retriever.onRetrieveContainerEntryFilesThenAnswer(block: (RetrieverRequest) -> Int) {
        stub {
            val containerEntryUrls = containerEntriesList.map {
                endpoint.url("/Container/FileByMd5/${URLEncoder.encode(it.containerEntryFile?.cefMd5, "UTF-8")}")
            }

            onBlocking {
                retrieve(argWhere { requestList ->
                    requestList.all { it.originUrl in containerEntryUrls }
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
    }

    private fun Retriever.verifyRetrievedManifest() {
        verifyBlocking(this) {
            retrieve(argWhere { requestList ->
                requestList.any { it.originUrl == manifestUrl }
            }, any())
        }
    }

    private fun makeProcessContext() : ContentJobProcessContext{
        return ContainerDownloadTestCommon.makeContentJobProcessContext(temporaryFolder,
            clientDb, clientDi)
    }

    @Suppress("UNCHECKED_CAST") //No way to avoid this on a mock invocation
    @Test
    fun givenValidContentEntryUid_whenProcessJobCalled_thenShouldUseRetrieverToGetManifestThenFiles() {
        val downloadPlugin = ContainerDownloadPlugin(Any(), endpoint, clientDi)

        val jobAndJobItem = makeDownloadJobAndJobItem(contentEntry, container, downloadDestDir,
            clientDb)

        mockRetriever.onRequestManifestAnswerWriteFile()
        mockRetriever.onRetrieveContainerEntryFilesThenAnswer { Retriever.STATUS_SUCCESSFUL }

        val processResult = runBlocking {
            downloadPlugin.processJob(jobAndJobItem, makeProcessContext(), mock { })
        }

        assertEquals(JobStatus.COMPLETE, processResult.status)

        mockRetriever.verifyRetrievedManifest()

        verifyBlocking(mockRetriever) {
            retrieve(argWhere { requestList ->
                requestList.map { it.originUrl }.containsAll(containerEntryUrlList)
                    && requestList.size == containerEntryUrlList.size
            }, any())
        }

        //verify that the entries are added to the database
        containerEntriesList.forEach {  entry ->
            entry.assertIsMatchingInClientDb()
        }
    }

    /**
     * Simulate a situation where we already have some of the needed ContainerEntryFile entities
     * e.g. another container already downloaded has the same file.
     */
    @Test
    fun givenSomeContainerFileMd5sAlreadyPresent_whenProcessJobCalled_thenShouldOnlyDownloadMissingEntriesAndLinkAllContainerPaths() {
        val existingContainerFiles = containerEntriesList.subList(0, 10).mapNotNull {
            it.containerEntryFile
        }
        clientDb.containerEntryFileDao.insertList(existingContainerFiles)

        val downloadPlugin = ContainerDownloadPlugin(Any(), endpoint, clientDi)

        val jobAndJobItem = makeDownloadJobAndJobItem(contentEntry, container, downloadDestDir,
            clientDb)

        mockRetriever.onRequestManifestAnswerWriteFile()
        mockRetriever.onRetrieveContainerEntryFilesThenAnswer { Retriever.STATUS_SUCCESSFUL }

        val processResult = runBlocking {
            downloadPlugin.processJob(jobAndJobItem, makeProcessContext(), mock { })
        }

        assertEquals(JobStatus.COMPLETE, processResult.status)
        mockRetriever.verifyRetrievedManifest()

        val remainingUrls = containerEntriesList.subList(10, containerEntriesList.size).map {
            it.downloadUrl()
        }
        verifyBlocking(mockRetriever) {
            retrieve(argWhere { requestList ->
                requestList.size == (containerEntryUrlList.size - existingContainerFiles.size)
                    && requestList.map { it.originUrl }.containsAll(remainingUrls)
            }, any())
        }

        //verify that the entries are added to the database
        containerEntriesList.forEach {  entry ->
            entry.assertIsMatchingInClientDb()
        }
    }

    /**
     * Simulate a situation where the previous download was interrupted. Nothing has been inserted
     * into the database yet, but there are json files on the disk from completed ContainerEntryFile
     * downloads
     */
    @Test
    fun givenSomeFilesAlreadyDownloadedInDir_whenProcessJobCalled_thenShouldOnlyDownloadRemainingEntries() {
        val alreadyDownloadedContainers = containerEntriesList.subList(0, 10).mapNotNull {
            it.containerEntryFile
        }

        val json: Json = clientDi.direct.instance()
        val containerDestDir = File(downloadDestDir, container.containerUid.toString())
        containerDestDir.takeIf { !it.exists() }?.mkdirs()
        alreadyDownloadedContainers.forEach { entryFile ->
            val ceJsonFileName = entryFile.cefMd5?.base64EncodedToHexString()!! + FILE_EXTENSION_CE_JSON
            File(containerDestDir, ceJsonFileName).writeText(
                json.encodeToString(ContainerEntryFile.serializer(), entryFile))
        }

        val downloadPlugin = ContainerDownloadPlugin(Any(), endpoint, clientDi)

        val jobAndJobItem = makeDownloadJobAndJobItem(contentEntry, container, downloadDestDir,
            clientDb)

        mockRetriever.onRequestManifestAnswerWriteFile()
        mockRetriever.onRetrieveContainerEntryFilesThenAnswer { Retriever.STATUS_SUCCESSFUL }

        val processResult = runBlocking {
            downloadPlugin.processJob(jobAndJobItem, makeProcessContext(), mock { })
        }

        assertEquals(JobStatus.COMPLETE, processResult.status)
        mockRetriever.verifyRetrievedManifest()

        val remainingUrls = containerEntriesList.subList(10, containerEntriesList.size).map {
            it.downloadUrl()
        }
        verifyBlocking(mockRetriever) {
            retrieve(argWhere { requestList ->
                requestList.size == (containerEntryUrlList.size - alreadyDownloadedContainers.size)
                    && requestList.map { it.originUrl }.containsAll(remainingUrls)
            }, any())
        }

        //verify that the entries are added to the database
        containerEntriesList.forEach {  entry ->
            entry.assertIsMatchingInClientDb()
        }
    }

    @Test
    fun givenValidSourceUri_whenExtractMetadataCalled_thenShouldReturnContentEntry() {
        val clientRepo: UmAppDatabase = clientDi.on(endpoint).direct
            .instance(tag = DoorTag.TAG_REPO)
        val contentEntry = ContentEntry().apply {
            title = "Hello World"
            leaf = true
            contentEntryUid = clientRepo.contentEntryDao.insert(this)
        }


        val containerDownloadContentJob = ContainerDownloadPlugin(Any(), endpoint, clientDi)

        val contentEntryDeepLink = contentEntry.toDeepLink(endpoint)

        val metaDataExtracted = runBlocking {
            containerDownloadContentJob.extractMetadata(
                DoorUri.parse(contentEntryDeepLink), mock {  })
        }

        Assert.assertEquals("Content title matches", contentEntry.title,
            metaDataExtracted?.entry?.title)
    }


    //Test to make sure that if the ContentJobItem has only the sourceUri that everything works as
    //expected
    @Test
    fun givenValidSourceUri_whenProcessJobCalled_thenShouldSetContentEntryUidAndContainerUid() {
        val mockListener = mock<ContentJobProgressListener> { }

        val job = makeDownloadJobAndJobItem(null, null,
            temporaryFolder.newFolder(), clientDb, contentEntry.toDeepLink(endpoint))

        mockRetriever.onRequestManifestAnswerWriteFile()
        mockRetriever.onRetrieveContainerEntryFilesThenAnswer { Retriever.STATUS_SUCCESSFUL }

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)


        val downloadJob = ContainerDownloadPlugin(Any(), endpoint, clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }

        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
            result.status)

        val contentJobItemInDb = clientDb.contentJobItemDao.findRootJobItemByJobId(job.contentJobItem?.cjiUid ?: 0L)
        Assert.assertEquals("ContentEntryUid was set from sourceUri", contentEntry.contentEntryUid,
            contentJobItemInDb?.cjiContentEntryUid)
        Assert.assertEquals("ContainerUid was set to most recent container after looking up content entry",
            container.containerUid, contentJobItemInDb?.cjiContainerUid)
    }

    fun givenNoContainerAvailable_whenProcessJobCalled_thenNotDownloadAnythingAndFail() {

    }





}