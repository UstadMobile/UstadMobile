package com.ustadmobile.core.contentformats.epub

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.ContentDispatcher
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.test.assertCachedBodyMatchesZipEntry
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EpubContentImporterCommonJvmTest : AbstractMainDispatcherTest() {

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

    private lateinit var ustadCache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var xml: XML

    private lateinit var xhtmlFixer: XhtmlFixer

    private lateinit var saveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var isTempFileCheckerUseCase: IsTempFileCheckerUseCase

    private lateinit var deleteUrisUseCase: DeleteUrisUseCase

    private lateinit var rootTmpPath: File

    private lateinit var activeEndpoint: Endpoint

    private lateinit var json: Json

    @Before
    fun setup(){
        endpointScope = EndpointScope()
        rootTmpPath = tmpFolder.newFolder("epub-import-tmp")
        di = DI {
            import(ustadTestRule.diModule)
        }
        json = di.direct.instance()

        val accountManager: UstadAccountManager by di.instance()
        activeEndpoint = accountManager.activeEndpoint

        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        val connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "NetworkSSID")
        db.connectivityStatusDao.insert(connectivityStatus)

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()
        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = di.direct.instance(),
            okHttpClient = di.direct.instance(),
        )


        ustadCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(tmpFolder.newFolder().absolutePath),
        ).build()
        xml = di.direct.instance()
        xhtmlFixer = XhtmlFixerJsoup(xml)

        isTempFileCheckerUseCase = IsTempFileCheckerUseCaseJvm(rootTmpPath)
        deleteUrisUseCase = DeleteUrisUseCaseCommonJvm(isTempFileCheckerUseCase)

        saveLocalUriAsBlobUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = activeEndpoint,
            cache = ustadCache,
            uriHelper = uriHelper,
            tmpDir = Path(rootTmpPath.absolutePath),
            deleteUrisUseCase = deleteUrisUseCase,
        )
        saveAndManifestUseCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(saveLocalUriAsBlobUseCase,
            FileMimeTypeHelperImpl())
    }



    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")!!
        val tempEpubFile = tmpFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json
        )

        runBlocking {
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())
            val metadata = epubPlugin.extractMetadata(
                epubUri, "childrens-literature.epub"
            )
            Assert.assertEquals("Got ContentEntry with expected title",
                    "Children's Literature",
                    metadata!!.entry.title)
        }
    }

    @Test
    fun givenEpubWithoutOpf_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val tmpEpubFile = tmpFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-no-opf.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-no-opf.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("epub does not contain opf") == true
                )
            }
        }
    }

    @Test
    fun givenEpubWithoutNav_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val tmpEpubFile = tmpFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-no-nav.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-no-nav.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("EPUB/nav.xhtml not found for") == true
                )
            }
        }
    }

    @Test
    fun givenEpubWithManifestItemsMissing_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        //Resource is missing epub.css
        val tmpEpubFile = tmpFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-missing-item.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-missing-item.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("Item(s) from manifest are missing") == true &&
                    e.message?.contains("epub.css") == true
                )
            }
        }
    }

    @Test
    fun givenValidEpubLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){
        val accountManager: UstadAccountManager by di.instance()

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json
        )

        runBlocking{
            val doorUri = DoorUri.parse(
                mockWebServer.url("/com/ustadmobile/core/contenttype/childrens-literature.epub")
                    .toString()
            )

            val uid = db.contentEntryDao.insert(ContentEntry().apply{
                title = "hello"
            })
            val contentEntryChildUid = 42L

            
            val jobItem = ContentEntryImportJob(
                sourceUri = doorUri.uri.toString(),
                cjiParentContentEntryUid = uid,
                cjiContentEntryUid = contentEntryChildUid
            )

            val result = epubPlugin.importContent(
                jobItem = jobItem,
                progressListener =  { }
            )
            assertEquals(contentEntryChildUid, result.cevContentEntryUid)

            withContext(Dispatchers.IO) {
                val tempEpubFile = tmpFolder.newFileFromResource(this::class.java,
                    "/com/ustadmobile/core/contenttype/childrens-literature.epub")


                val expectedUrlPrefix = "${activeEndpoint.url}api/content/${result.cevUid}/"
                val manifestResponse = ustadCache.retrieve(
                    requestBuilder("$expectedUrlPrefix${ContentConstants.MANIFEST_NAME}")
                )
                val manifest = json.decodeFromString(
                    ContentManifest.serializer(), manifestResponse!!.bodyAsSource()!!.readString()
                )


                ZipFile(tempEpubFile).use { zipFile ->
                    //verifies that we have the OPF
                    val opfPathInZip = result.cevOpenUri!!.substringAfterLast("api/content/${result.cevUid}/")
                    val manifestOpfEntry = manifest.entries.first {
                        it.uri == opfPathInZip
                    }


                    ustadCache.assertCachedBodyMatchesZipEntry(
                        url = manifestOpfEntry.bodyDataUrl,
                        zipFile = zipFile,
                        pathInZip = opfPathInZip
                    )

                    val opfResponse = ustadCache.retrieve(requestBuilder(manifestOpfEntry.bodyDataUrl))
                    val opfPackage = xml.decodeFromString(
                        deserializer = PackageDocument.serializer(),
                        string = opfResponse?.bodyAsSource()!!.readString()
                    )

                    //Assert that we have all content in the manifest with the expected mimetype
                    opfPackage.manifest.items.forEach { opfItem ->
                        val pathInZipFile = Path(opfPathInZip).parent?.let {
                            Path(it, opfItem.href)
                        } ?: Path(opfItem.href)
                        val pathInZipFileStr = pathInZipFile.toString()

                        val manifestItem = manifest.entries.first {
                            it.uri == pathInZipFileStr
                        }

                        assertEquals(opfItem.mediaType, manifestItem.responseHeaders["content-type"])

                        ustadCache.assertCachedBodyMatchesZipEntry(
                            url = manifestItem.bodyDataUrl,
                            zipFile = zipFile,
                            pathInZip = pathInZipFile.toString()
                        )
                    }
                }
            }
        }
    }

}