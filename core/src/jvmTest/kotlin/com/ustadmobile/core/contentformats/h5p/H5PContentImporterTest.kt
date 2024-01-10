package com.ustadmobile.core.contentformats.h5p

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.contententry.ContentConstants.MANIFEST_NAME
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.test.assertZipIsManifested
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.xmlpullparserkmp.setInputString
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.zip.ZipFile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class H5PContentImporterTest : AbstractMainDispatcherTest() {

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

    private lateinit var saveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var isTempFileCheckerUseCase: IsTempFileCheckerUseCase

    private lateinit var deleteUrisUseCase: DeleteUrisUseCase

    private lateinit var rootTmpPath: File

    private lateinit var importerTmpPath: File

    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }
        rootTmpPath = temporaryFolder.newFolder("h5p-import-tmp-root")

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

        isTempFileCheckerUseCase = IsTempFileCheckerUseCaseJvm(rootTmpPath)
        deleteUrisUseCase = DeleteUrisUseCaseCommonJvm(isTempFileCheckerUseCase)

        saveLocalUriAsBlobUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = activeEndpoint,
            cache = ustadCache,
            uriHelper = uriHelper,
            tmpDir = Path(rootTmpPath.absolutePath),
            deleteUrisUseCase = deleteUrisUseCase,
        )
        saveAndManifestUseCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(saveLocalUriAsBlobUseCase)
        importerTmpPath = File(rootTmpPath, "h5pimporter-tmp")
    }

    @Test
    fun givenValidH5pFile_whenExtractMetadataCalled_thenMetadataShouldMatch() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(importerTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        val metadata = runBlocking {
            h5pPlugin.extractMetadata(h5pFile.toDoorUri(), "fill-in-the-blank-withmetadata.h5p")
        }

        assertEquals("I want to eat", metadata?.entry?.title)
        assertEquals("Bob Jones", metadata?.entry?.author)
        assertEquals(ContentEntry.LICENSE_TYPE_CC_BY, metadata?.entry?.licenseType)
    }

    @Test
    fun givenFileWithH5pExtensionNotValidH5p_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val invalidH5pFile = temporaryFolder.newFile()
        invalidH5pFile.writeText("Hello world")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(importerTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        runBlocking {
            try {
                h5pPlugin.extractMetadata(invalidH5pFile.toDoorUri(), "invalid.h5p")
                throw IllegalStateException("Should not get here")
            }catch(e: InvalidContentException) {
                assertNotNull(e)
            }
        }
    }

    @Test
    fun givenFileNotH5pFile_whenExtractMetadataCalled_thenShouldReturnNull() {
        val notH5p = temporaryFolder.newFile()

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(importerTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        runBlocking {
            assertNull(h5pPlugin.extractMetadata(notH5p.toDoorUri(), "file.pdf"))
        }
    }

    @Test
    fun givenValidH5pFile_whenStoreInCacheCalled_thenShouldStore() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = di.direct.instance(),
            tmpPath = Path(importerTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        val result = runBlocking {
            h5pPlugin.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = h5pFile.toDoorUri().toString(),
                ),
                progressListener =  { }
            )
        }

        val expectedUrlPrefix = "${activeEndpoint.url}api/content/${result.cevUid}/"
        val manifestResponse = ustadCache.retrieve(
            requestBuilder("$expectedUrlPrefix$MANIFEST_NAME")
        )
        val json: Json = di.direct.instance()
        val manifest = json.decodeFromString(
            ContentManifest.serializer(), manifestResponse!!.bodyAsSource()!!.readString()
        )

        ZipFile(h5pFile).use { zipFile ->
            ustadCache.assertZipIsManifested(manifest, zipFile, "h5p-folder/")
        }

        val h5pStandaloneAssets = temporaryFolder.newFileFromResource(this::class.java,
            "/h5p/h5p-standalone-3.6.0.zip")

        ZipFile(h5pStandaloneAssets).use { zipFile ->
            ustadCache.assertZipIsManifested(manifest, zipFile)
        }

        //Check we can parse the tincan xml
        val tinCanXmlResponse = ustadCache.retrieve(
            requestBuilder(manifest.entries.first { it.uri == "tincan.xml" }.bodyDataUrl)
        )
        val tinCanStr = tinCanXmlResponse?.bodyAsSource()?.asInputStream()?.readString()
        val xppFactory = XmlPullParserFactory.newInstance()
        val xpp = xppFactory.newPullParser()

        xpp.setInputString(tinCanStr!!)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        assertEquals("index.html", tinCanXml.launchActivity?.launchUrl)

        val htmlResponse = ustadCache.retrieve(
            requestBuilder(manifest.entries.first { it.uri == "index.html"}.bodyDataUrl)
        )
        assertNotNull(htmlResponse)
    }

}