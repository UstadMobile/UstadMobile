package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.onActiveAccountDirect
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*

class PDFTypePluginJvmTest {


    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    private lateinit var db: UmAppDatabase

    private lateinit var activeEndpoint: Endpoint

    @Before
    fun setup(){

        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
                ContainerStorageManager(listOf(tmpFolder.newFolder()))
            }
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()

        activeEndpoint = di.direct.instance<UstadAccountManager>().activeEndpoint
        db = di.onActiveAccountDirect().instance(tag = DoorTag.TAG_DB)
    }

    private fun importPDFUsingContentJobAndAssertValidOutput(
        sourceUri: DoorUri,
        compress: Boolean,
    ){
        val processContext = ContentJobProcessContext(sourceUri, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(
                "compress" to compress.toString()
            ), DummyContentJobItemTransactionRunner(db), di)

        val jobItem = ContentJobItem(sourceUri = sourceUri.toString(),
            cjiParentContentEntryUid = 42, cjiContentEntryUid = 42)
        val job = ContentJob(toUri = tmpFolder.newFolder().toDoorUri().toString()).also {
            it.params = Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                mapOf("compress" to "true"))
        }
        val jobAndItem = ContentJobItemAndContentJob().apply{
            this.contentJob = job
            this.contentJobItem = jobItem
        }

        runBlocking {
            db.contentJobItemDao.insertJobItem(jobItem)
            db.contentJobDao.insertAsync(job)
        }

        val pdfPlugin = PDFTypePluginJvm(Any(), activeEndpoint, di)

        runBlocking {
            pdfPlugin.processJob(jobAndItem, processContext,  { })
        }

        val container = runBlocking {
            db.containerDao.findContainersForContentEntryUid(42).first()
        }

        val containerEntries = db.containerEntryDao.findByContainer(container.containerUid)
        Assert.assertEquals("Container has one entry", 1, containerEntries.size)

        Assert.assertEquals("Container entry ends with correct file extension",
            "pdf", containerEntries.first().cePath?.substringAfterLast("."))

    }

    @Test
    fun givenIssue363ValidPDF_whenExtractMetadata_shouldMatch(){


        val inputStream = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/container/test2.pdf")!!
        val tempPDFFile = tmpFolder.newFile("test2.pdf")
        tempPDFFile.copyInputStreamToFile(inputStream)
        val pdfURI = DoorUri.parse(tempPDFFile.toURI().toString())
        val processContext = ContentJobProcessContext(pdfURI, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val pdfPlugin = PDFTypePluginJvm(Any(), Endpoint("http://localhost/dummy"), di)

        runBlocking {
            val metadataResult = pdfPlugin.extractMetadata(pdfURI, processContext)!!
            Assert.assertEquals("title match pdf", "test2.pdf",
                metadataResult.entry.title)
            Assert.assertEquals("contentType is PDF", ContentEntry.TYPE_PDF,
                metadataResult.entry.contentTypeFlag)
        }
    }


    @Test
    fun givenValidPDF_whenExtractMetadata_shouldMatch(){


        val inputStream = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
        val tempPDFFile = tmpFolder.newFile("validPDFMetadata.pdf")
        tempPDFFile.copyInputStreamToFile(inputStream)
        val pdfURI = DoorUri.parse(tempPDFFile.toURI().toString())
        val processContext = ContentJobProcessContext(pdfURI, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val pdfPlugin = PDFTypePluginJvm(Any(), Endpoint("http://localhost/dummy"), di)

        runBlocking {
            val metadataResult = pdfPlugin.extractMetadata(pdfURI, processContext)!!
            Assert.assertEquals("title match pdf", "A Valid PDF for testing",
                metadataResult.entry.title)
            Assert.assertEquals("contentType is PDF", ContentEntry.TYPE_PDF,
                metadataResult.entry.contentTypeFlag)
        }
    }

    @Test
    fun givenValidPDFLink_whenExtractMetadata_shouldMatch(){

        val accountManager: UstadAccountManager by di.instance()
        val pdfUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/container/validPDFMetadata.pdf").toString())
        val processContext = ContentJobProcessContext(pdfUri, tmpFolder.newFolder().toDoorUri(),
            mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val pdfPlugin = PDFTypePluginJvm(Any(), accountManager.activeEndpoint, di)

        runBlocking {
            val metadataResult = pdfPlugin.extractMetadata(pdfUri, processContext)!!
            Assert.assertEquals("title match pdf", "A Valid PDF for testing",
                metadataResult.entry.title)
            Assert.assertEquals("contentType is PDF", ContentEntry.TYPE_PDF,
                metadataResult.entry.contentTypeFlag)
        }
    }

    @Test
    fun givenInvalidPDF_whenExtractMetadata_shouldReturnNull(){


        val inputStream = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/container/invalidPDF.pdf")!!
        val tempPDFFile = tmpFolder.newFile("invalidPDF.pdf")
        tempPDFFile.copyInputStreamToFile(inputStream)
        val pdfURI = DoorUri.parse(tempPDFFile.toURI().toString())
        val processContext = ContentJobProcessContext(pdfURI, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val pdfPlugin = PDFTypePluginJvm(Any(), Endpoint("http://localhost/dummy"), di)

        runBlocking {
            val metadataResult: MetadataResult? = pdfPlugin.extractMetadata(pdfURI, processContext)
            Assert.assertNull(metadataResult)
        }
    }

    @Test
    fun givenInvalidPDFLink_whenExtractMetadata_shouldReturnNull(){

        val accountManager: UstadAccountManager by di.instance()
        val invalidPDFUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/container/invalidPDF.pdf").toString())
        val processContext = ContentJobProcessContext(invalidPDFUri, tmpFolder.newFolder().toDoorUri(),
            mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val pdfPlugin = PDFTypePluginJvm(Any(), accountManager.activeEndpoint, di)

        runBlocking {
            val metadataResult: MetadataResult? = pdfPlugin.extractMetadata(invalidPDFUri, processContext)
            Assert.assertNull(metadataResult)
        }
    }

    @Test
    fun givenValidPDF_whenImportedFromFileCalled_thenShouldAddToContainer() {
        val tempPDFFile = tmpFolder.newFile("validPDFMetadata.pdf")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
            .writeToFile(tempPDFFile)

        importPDFUsingContentJobAndAssertValidOutput(tempPDFFile.toDoorUri(), false)
    }

    @Test
    fun givenValidPDF_whenImportedFromUrl_thenShouldAddToContainer() {
        importPDFUsingContentJobAndAssertValidOutput(DoorUri.parse(
            mockWebServer.url(
                "/com/ustadmobile/core/container/validPDFMetadata.pdf").toString()),
            false)
    }


}