package com.ustadmobile.core.contentformats.media

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.video.VideoContentImporterJvm
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import net.bramp.ffmpeg.FFprobe
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VideoContentImporterJvmTest : AbstractMainDispatcherTest() {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var activeEndpoint: Endpoint

    private lateinit var db: UmAppDatabase

    private lateinit var ustadCache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var ffProbe: FFprobe

    private lateinit var json: Json

    private lateinit var saveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var isTempFileCheckerUseCase: IsTempFileCheckerUseCase

    private lateinit var deleteUrisUseCase: DeleteUrisUseCase

    private lateinit var rootTmpPath: File


    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }
        rootTmpPath = temporaryFolder.newFolder("video-import-test")

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        json = di.direct.instance()

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

        ffProbe = SysPathUtil.findCommandInPath("ffprobe")?.let {
            FFprobe(it.absolutePath)
        } ?: throw IllegalStateException("Cannot find ffmpeg in path. FFMPEG must be in path to run this test")

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
    }

    @Test
    fun givenValidVideo_whenExtractMetadataCalled_thenShouldReturnEntry() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )
        val metadataResult = runBlocking {
            importer.extractMetadata(videoFile.toDoorUri(), "BigBuckBunny.mp4")
        }
        assertEquals("BigBuckBunny.mp4", metadataResult?.entry?.title)
        assertEquals(ContentEntry.TYPE_VIDEO, metadataResult?.entry?.contentTypeFlag)
    }

    @Test
    fun givenInvalidFileWithRecognizedExtension_whenExtractMetadataCalled_thenWillThrowInvalidContentException() {
        val invalidVideoFile = temporaryFolder.newFile()
        invalidVideoFile.writeText("Hello World")
        val importer = VideoContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )
        runBlocking {
            try {
                importer.extractMetadata(invalidVideoFile.toDoorUri(), "BigBuckBunny.mp4")
                throw IllegalStateException("Should not get here")
            }catch(e: Throwable) {
                assertTrue(e is InvalidContentException)
            }
        }
    }

    @Test
    fun givenNonVideoFile_whenExtractMetadataCalled_thenWillReturnNull() {
        val txtFile = temporaryFolder.newFile()
        txtFile.writeText("Hello World")

        val importer = VideoContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )
        runBlocking {
            assertNull(importer.extractMetadata(txtFile.toDoorUri(), "file.txt"))
        }
    }

    @Test
    fun givenValidVideoFile_whenAddToCacheCalled_thenWillAddToCache() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpPath.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        val result = runBlocking {
            importer.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = videoFile.toDoorUri().toString(),
                    cjiOriginalFilename = "BigBuckBunny.mp4"
                ),
                progressListener = { }
            )
        }
        val manifestResponse = ustadCache.retrieve(
            requestBuilder(result.cevSitemapUrl!!)
        )
        val manifest = json.decodeFromString(
            ContentManifest.serializer(), manifestResponse!!.bodyAsSource()!!.readString()
        )
        val videoManifestItem = manifest.entries.first {
            it.uri == "video"
        }
        ustadCache.assertCachedBodyMatchesFileContent(
            url = videoManifestItem.bodyDataUrl,
            file = videoFile
        )
        assertEquals("video/mp4", videoManifestItem.responseHeaders["content-type"])

        val mediaInfoResponse = ustadCache.retrieve(
            requestBuilder(
                url = manifest.entries.first {
                    it.uri == "media.json"
                }.bodyDataUrl
            )
        )
        val mediaInfoText = mediaInfoResponse?.bodyAsSource()?.readString()!!
        val mediaInfo: MediaContentInfo = json.decodeFromString(mediaInfoText)
        assertEquals("video/mp4", mediaInfo.sources.first().mimeType)

    }

}