package com.ustadmobile.core.contentformats.h5p

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.test.assertZipIsCached
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.xmlpullparserkmp.setInputString
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.xmlpull.v1.XmlPullParserFactory
import java.util.zip.ZipFile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class H5PContentImportPluginTest : AbstractMainDispatcherTest() {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var ustadCache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var activeEndpoint: Endpoint

    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        activeEndpoint = accountManager.activeEndpoint

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
    fun givenValidH5pFile_whenExtractMetadataCalled_thenMetadataShouldMatch() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImportPlugin(
            endpoint = activeEndpoint,
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
        )

        val metadata = runBlocking {
            h5pPlugin.extractMetadata(h5pFile.toDoorUri(), "fill-in-the-blank-withmetadata.h5p")
        }

        assertEquals("I want to eat", metadata.entry.title)
        assertEquals("Bob Jones", metadata.entry.author)
        assertEquals(ContentEntry.LICENSE_TYPE_CC_BY, metadata.entry.licenseType)
    }

    @Test
    fun givenValidH5pFile_whenStoreInCacheCalled_thenShouldStore() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImportPlugin(
            endpoint = activeEndpoint,
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
        )

        val contentJobAndItem = ContentJobItemAndContentJob().apply {
            contentJobItem = ContentJobItem(
                sourceUri = h5pFile.toDoorUri().toString(),
            )
            contentJob = ContentJob()
        }

        val result = runBlocking {
            h5pPlugin.addToCache(
                jobItem = contentJobAndItem,
                progressListener =  { }
            )
        }

        val expectedUrlPrefix = "${activeEndpoint.url}api/content/${result.cevUid}/"

        ZipFile(h5pFile).use { zipFile ->
            ustadCache.assertZipIsCached(
                urlPrefix = "${expectedUrlPrefix}h5p-folder/",
                zip = zipFile
            )
        }

        val h5pStandaloneAssets = temporaryFolder.newFileFromResource(this::class.java,
            "/h5p/h5p-standalone-3.6.0.zip")
        ZipFile(h5pStandaloneAssets).use { zipFile ->
            ustadCache.assertZipIsCached(
                urlPrefix = expectedUrlPrefix,
                zip = zipFile
            )
        }

        //Check we can parse the tincan xml
        val tinCanXmlResponse = ustadCache.retrieve(requestBuilder(result.cevUrl!!))
        val tinCanStr = tinCanXmlResponse?.bodyAsSource()?.asInputStream()?.readString()
        val xppFactory = XmlPullParserFactory.newInstance()
        val xpp = xppFactory.newPullParser()

        xpp.setInputString(tinCanStr!!)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        assertEquals("index.html", tinCanXml.launchActivity?.launchUrl)

        val htmlResponse = ustadCache.retrieve(
            requestBuilder(UMFileUtil.resolveLink(result.cevUrl!!, "index.html"))
        )
        assertNotNull(htmlResponse)
    }

}