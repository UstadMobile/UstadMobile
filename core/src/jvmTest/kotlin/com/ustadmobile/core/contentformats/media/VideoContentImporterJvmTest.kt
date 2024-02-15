package com.ustadmobile.core.contentformats.media

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCaseFfprobe
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.bodyAsString
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import net.bramp.ffmpeg.FFprobe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VideoContentImporterJvmTest : AbstractContentImporterTest() {

    private lateinit var ffProbe: FFprobe

    @BeforeTest
    fun setupVideoTest() {
        ffProbe = SysPathUtil.findCommandInPath("ffprobe")?.let {
            FFprobe(it.absolutePath)
        } ?: throw IllegalStateException("Cannot find ffmpeg in path. FFMPEG must be in path to run this test")

    }

    @Test
    fun givenValidVideo_whenExtractMetadataCalled_thenShouldReturnEntry() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            validateVideoFileUseCase = ValidateVideoFileUseCaseFfprobe(ffProbe),
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase
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
        val importer = VideoContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            validateVideoFileUseCase = ValidateVideoFileUseCaseFfprobe(ffProbe),
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
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

        val importer = VideoContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            validateVideoFileUseCase = ValidateVideoFileUseCaseFfprobe(ffProbe),
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
        )
        runBlocking {
            assertNull(importer.extractMetadata(txtFile.toDoorUri(), "file.txt"))
        }
    }

    @Test
    fun givenValidVideoFile_whenAddToCacheCalled_thenWillAddToCache() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            validateVideoFileUseCase = ValidateVideoFileUseCaseFfprobe(ffProbe),
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
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
            requestBuilder(result.cevManifestUrl!!)
        )
        val manifest = json.decodeFromString(
            ContentManifest.serializer(), manifestResponse!!.bodyAsString()!!
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
        val mediaInfoText = mediaInfoResponse?.bodyAsString()!!
        val mediaInfo: MediaContentInfo = json.decodeFromString(mediaInfoText)
        assertEquals("video/mp4", mediaInfo.sources.first().mimeType)
    }

}