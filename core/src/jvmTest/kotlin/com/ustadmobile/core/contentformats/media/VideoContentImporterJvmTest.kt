package com.ustadmobile.core.contentformats.media

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
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

    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

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
    }

    @Test
    fun givenValidVideo_whenExtractMetadataCalled_thenShouldReturnEntry() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterJvm(
            endpoint = activeEndpoint,
            di = di,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
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
            di = di,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
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
            di = di,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
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
            di = di,
            cache = ustadCache,
            ffprobe = ffProbe,
            uriHelper = uriHelper,
            json = json,
        )

        val jobAndItem = ContentJobItemAndContentJob().apply {
            contentJob = ContentJob()
            contentJobItem = ContentJobItem(
                sourceUri = videoFile.toDoorUri().toString(),
                cjiOriginalFilename = "BigBuckBunny.mp4"
            )
        }

        val result = runBlocking {
            importer.addToCache(
                jobItem = jobAndItem,
                progressListener = { }
            )
        }

        val mediaInfoResponse = ustadCache.retrieve(requestBuilder(result.cevUrl!!))
        val mediaInfoText = mediaInfoResponse?.bodyAsSource()?.readString()!!
        val mediaInfo: MediaContentInfo = json.decodeFromString(mediaInfoText)

        assertEquals("video/mp4", mediaInfo.sources.first().mimeType)

        val videoUrl = mediaInfo.sources.first().url

        ustadCache.assertCachedBodyMatchesFileContent(
            url = videoUrl,
            file = videoFile,
        )

        val response = ustadCache.retrieve(requestBuilder(videoUrl))
        assertEquals("video/mp4", response?.headers?.get("content-type"))
    }

}