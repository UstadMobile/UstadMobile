package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.test.assertZipIsCached
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.util.zip.ZipFile

class XapiZipContentImportPluginTest :AbstractMainDispatcherTest() {

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

    @Before
    fun setup(){
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
                ContainerStorageManager(listOf(temporaryFolder.newFolder()))
            }
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        val connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "NetworkSSID")
        db.connectivityStatusDao.insert(connectivityStatus)

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
    }

    @Test
    fun givenValidTinCanFormatFile_whenGetContentEntryCalled_thenShouldReadMetaData() {
        val tempFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")

        val xapiPlugin =  XapiZipContentImportPlugin(
            endpoint = Endpoint("http://localhost/dummy/"),
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
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
    fun givenValidXapiLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded() {
        val tempFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")

        val endpoint = Endpoint("http://localhost/dummy/")

        val xapiPlugin =  XapiZipContentImportPlugin(
            endpoint = endpoint,
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
        )

        val contentJobAndItem = ContentJobItemAndContentJob().apply {
            contentJobItem = ContentJobItem(
                sourceUri = tempFile.toDoorUri().toString(),
            )
            contentJob = ContentJob()
        }

        val result = runBlocking {
            xapiPlugin.addToCache(
                jobItem = contentJobAndItem,
                progressListener = { }
            )
        }

        val expectedUrlPrefix = "${endpoint.url}api/content/${result.cevUid}/"

        ZipFile(tempFile).use { zipFile ->
            ustadCache.assertZipIsCached(
                urlPrefix = expectedUrlPrefix,
                zip = zipFile,
            )
        }
    }
}