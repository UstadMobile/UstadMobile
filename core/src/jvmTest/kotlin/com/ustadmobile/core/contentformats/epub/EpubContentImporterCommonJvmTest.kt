package com.ustadmobile.core.contentformats.epub

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.ContentDispatcher
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.assertCachedBodyMatchesZipEntry
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UMFileUtil
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
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.net.URL
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

    @Before
    fun setup(){
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
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
    }



    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")!!
        val tempEpubFile = tmpFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = Endpoint("http://localhost/dummy"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
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
            endpoint = Endpoint("http://localhost/dummy"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
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
            endpoint = Endpoint("http://localhost/dummy"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
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
            endpoint = Endpoint("http://localhost/dummy"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
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
    fun givenValidEpubFile_whenAddToCacheCalled_thenShouldStore() {
        val tempEpubFile = tmpFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/childrens-literature.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = Endpoint("http://localhost/dummy/"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
        )
        val contentEntryUid = 42L

        runBlocking {
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())

            val result = epubPlugin.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = epubUri.toString(),
                    cjiContentEntryUid = contentEntryUid,
                ),
                progressListener = { },
            )

            withContext(Dispatchers.IO) {
                ZipFile(tempEpubFile).use { zipFile ->
                    ustadCache.assertCachedBodyMatchesZipEntry(
                        url = result.cevUrl!!,
                        zipFile = zipFile,
                        pathInZip = result.cevUrl!!.substringAfterLast("api/content/${result.cevUid}/")
                    )
                }
            }
            assertEquals(contentEntryUid, result.cevContentEntryUid)
        }
    }

    @Test
    fun givenValidEpubLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){
        val accountManager: UstadAccountManager by di.instance()

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = Endpoint("http://localhost/dummy/"),
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
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

                ZipFile(tempEpubFile).use { zipFile ->
                    //verifies that we have the OPF
                    val opfPathInZip = result.cevUrl!!.substringAfterLast("api/content/${result.cevUid}/")
                    val opfUrl = URL(result.cevUrl!!)
                    ustadCache.assertCachedBodyMatchesZipEntry(
                        url = result.cevUrl!!,
                        zipFile = zipFile,
                        pathInZip = opfPathInZip
                    )

                    val opfResponse = ustadCache.retrieve(requestBuilder(result.cevUrl!!))
                    val opfPackage = xml.decodeFromString(
                        deserializer = PackageDocument.serializer(),
                        string = opfResponse?.bodyAsSource()!!.readString()
                    )

                    //Assert that we have all content in the manifest with the expected mimetype
                    opfPackage.manifest.items.forEach { item ->
                        val response = ustadCache.retrieve(
                            requestBuilder(UMFileUtil.resolveLink(result.cevUrl!!, item.href))
                        )
                        assertEquals(item.mediaType, response?.headers?.get("content-type"))

                        val pathInZipFile = Path(opfPathInZip).parent?.let {
                            Path(it, item.href)
                        } ?: Path(item.href)

                        ustadCache.assertCachedBodyMatchesZipEntry(
                            url = URL(opfUrl, item.href).toString(),
                            zipFile = zipFile,
                            pathInZip = pathInZipFile.toString()
                        )
                    }
                }
            }
        }
    }

}