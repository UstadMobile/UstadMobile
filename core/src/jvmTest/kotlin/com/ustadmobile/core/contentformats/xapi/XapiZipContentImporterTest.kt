package com.ustadmobile.core.contentformats.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.ContentDispatcher
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.test.assertZipIsManifested
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsString
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class XapiZipContentImporterTest :AbstractMainDispatcherTest() {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    private lateinit var db: UmAppDatabase

    private lateinit var ustadCache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var saveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var isTempFileCheckerUseCase: IsTempFileCheckerUseCase

    private lateinit var deleteUrisUseCase: DeleteUrisUseCase

    private lateinit var rootTmpPath: File

    private lateinit var activeEndpoint: Endpoint

    private lateinit var compressListUseCase: CompressListUseCase

    @Before
    fun setup(){
        endpointScope = EndpointScope()
        rootTmpPath = temporaryFolder.newFolder("xapi-import-tmp-root")

        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        activeEndpoint = accountManager.activeEndpoint

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()

        ustadCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(temporaryFolder.newFolder().absolutePath),
        ).build()

        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = di.direct.instance(),
            okHttpClient = di.direct.instance(),
        )

        isTempFileCheckerUseCase = IsTempFileCheckerUseCaseJvm(rootTmpPath)
        deleteUrisUseCase = DeleteUrisUseCaseCommonJvm(isTempFileCheckerUseCase)

        saveLocalUriAsBlobUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = activeEndpoint,
            cache = ustadCache,
            uriHelper = uriHelper,
            tmpDir = Path(rootTmpPath.absolutePath),
            deleteUrisUseCase = deleteUrisUseCase,
        )
        compressListUseCase = CompressListUseCase(
            compressVideoUseCase = null,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            compressImageUseCase = null,
        )

        saveAndManifestUseCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(saveLocalUriAsBlobUseCase,
            FileMimeTypeHelperImpl())
    }

    @Test
    fun givenValidTinCanFormatFile_whenGetContentEntryCalled_thenShouldReadMetaData() {
        val tempFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")

        val xapiPlugin =  XapiZipContentImporter(
            endpoint = Endpoint("http://localhost/dummy/"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )

        val metadata = runBlocking {
            xapiPlugin.extractMetadata(tempFile.toDoorUri(), "ustad-tincan.zip")
        }!!

        Assert.assertEquals("Got expected title",
            "Ustad Mobile", metadata.entry.title)
        Assert.assertEquals("Got expected description",
            "Ustad Mobile sample tincan", metadata.entry.description)
    }

    @Test
    fun givenInvalidTinCanXmlFile_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val tempFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan-invalid.zip")

        val xapiPlugin =  XapiZipContentImporter(
            endpoint = Endpoint("http://localhost/dummy/"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )

        runBlocking {
            try {
                xapiPlugin.extractMetadata(tempFile.toDoorUri(), "ustad-tincan.zip")
                throw IllegalStateException("Should not get here")
            }catch(e: InvalidContentException) {
                assertNotNull(e)
            }
        }
    }

    @Test
    fun givenFileNotTincanZip_whenExtractMetadataCalled_thenWillReturnNull() {
        val tempFile = temporaryFolder.newFile()
        tempFile.writeText("Hello World")
        val xapiPlugin =  XapiZipContentImporter(
            endpoint = Endpoint("http://localhost/dummy/"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )
        runBlocking {
            assertNull(xapiPlugin.extractMetadata(tempFile.toDoorUri(), "file.zip"))
        }
    }


    @Test
    fun givenValidXapiLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded() {
        val tempFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")

        val endpoint = Endpoint("http://localhost/dummy/")

        val xapiPlugin =  XapiZipContentImporter(
            endpoint = endpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )

        val result = runBlocking {
            xapiPlugin.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = tempFile.toDoorUri().toString(),
                ),
                progressListener = { }
            )
        }

        val expectedUrlPrefix = "${endpoint.url}api/content/${result.cevUid}/"

        val json : Json = di.direct.instance()
        val manifestResponse = ustadCache.retrieve(
            iRequestBuilder("$expectedUrlPrefix${ContentConstants.MANIFEST_NAME}")
        )
        val manifest = json.decodeFromString(
            ContentManifest.serializer(), manifestResponse!!.bodyAsString()!!
        )

        ZipFile(tempFile).use { zipFile ->
            ustadCache.assertZipIsManifested(manifest, zipFile)
        }
    }
}