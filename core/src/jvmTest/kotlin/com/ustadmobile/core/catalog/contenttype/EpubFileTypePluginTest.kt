package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.assertCachedBodyMatchesZipEntry
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.util.zip.ZipFile

class EpubFileTypePluginTest : AbstractMainDispatcherTest() {

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
    }



    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")!!
        val tempEpubFile = tmpFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val epubPlugin = EpubTypePluginCommonJvm(
            endpoint = Endpoint("http://localhost/dummy"),
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
        )

        runBlocking {
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())
            val metadata = epubPlugin.extractMetadata(
                epubUri, "childrens-literature.epub"
            )
            Assert.assertEquals("Got ContentEntry with expected title",
                    "A Textbook of Sources for Teachers and Teacher-Training Classes",
                    metadata!!.entry.title)
        }
    }

    @Test
    fun givenValidEpubFile_whenAddToCacheCalled_thenShouldStore() {
        val tempEpubFile = tmpFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/childrens-literature.epub")

        val epubPlugin = EpubTypePluginCommonJvm(
            endpoint = Endpoint("http://localhost/dummy/"),
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml
        )

        runBlocking {
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())

            val result = epubPlugin.addToCache(
                jobItem = ContentJobItemAndContentJob().apply {
                    contentJobItem = ContentJobItem(
                        sourceUri = epubUri.toString(),
                    )
                    contentJob = ContentJob()
                },
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


            //TODO: Epub plugin should be adapted to cache using the OPF Manifest, not the zip.
        }
    }

    @Test
    fun givenValidEpubLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){
        val accountManager: UstadAccountManager by di.instance()

        val epubPlugin = EpubTypePluginCommonJvm(
            endpoint = Endpoint("http://localhost/dummy/"),
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml
        )

        runBlocking{
            val doorUri = DoorUri.parse(
                mockWebServer.url("/com/ustadmobile/core/contenttype/childrens-literature.epub")
                    .toString()
            )

            val uid = db.contentEntryDao.insert(ContentEntry().apply{
                title = "hello"
            })
            
            val jobItem = ContentJobItem(
                sourceUri = doorUri.uri.toString(),
                cjiParentContentEntryUid = uid
            )

            val job = ContentJob()
            val jobAndItem = ContentJobItemAndContentJob().apply{
                this.contentJob = job
                this.contentJobItem = jobItem
            }
            val result = epubPlugin.addToCache(
                jobItem = jobAndItem,
                progressListener =  { }
            )

            withContext(Dispatchers.IO) {
                val tempEpubFile = tmpFolder.newFileFromResource(this::class.java,
                    "/com/ustadmobile/core/contenttype/childrens-literature.epub")

                ZipFile(tempEpubFile).use { zipFile ->
                    ustadCache.assertCachedBodyMatchesZipEntry(
                        url = result.cevUrl!!,
                        zipFile = zipFile,
                        pathInZip = result.cevUrl!!.substringAfterLast("api/content/${result.cevUid}/")
                    )
                }
            }
        }
    }

}