package com.ustadmobile.core.contentformats.media

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.compress.CompressionType
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsString
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VideoContentImporterJvmTest : AbstractContentImporterTest() {

    private lateinit var extractMediaUseCase: ExtractMediaMetadataUseCase

    private lateinit var executeMediaInfoUseCase: ExecuteMediaInfoUseCase

    private lateinit var validateVideoUseCase: ValidateVideoFileUseCase

    private lateinit var getStoragePathUseCaseMock: GetStoragePathForUrlUseCase

    @BeforeTest
    fun setupVideoTest() {
        val mediaInfoPath = SysPathUtil.findCommandInPath("mediainfo")
            ?: throw IllegalStateException("Cannot find mediainfo in path. MediaInfo must be in path to run this test")
        getStoragePathUseCaseMock = mock {
            onBlocking { invoke(any(), any(), any(), any()) }.thenAnswer { invocation ->
                GetStoragePathForUrlUseCase.GetStoragePathResult(
                    fileUri = invocation.arguments.first() as String,
                    compression = CompressionType.NONE,
                )
            }
        }

        executeMediaInfoUseCase = ExecuteMediaInfoUseCase(
            mediaInfoPath = mediaInfoPath.absolutePath,
            workingDir = File(System.getProperty("user.dir")),
            json = json,
        )

        extractMediaUseCase = ExtractMediaMetadataUseCaseMediaInfo(
            executeMediaInfoUseCase=  executeMediaInfoUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
        )

        validateVideoUseCase = ValidateVideoFileUseCase(
            extractMediaMetadataUseCase = extractMediaUseCase,
        )
    }

    @Test
    fun givenValidVideo_whenExtractMetadataCalled_thenShouldReturnEntry() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")

        val importer = VideoContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            validateVideoFileUseCase = validateVideoUseCase,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
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
            validateVideoFileUseCase = validateVideoUseCase,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
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
            validateVideoFileUseCase = validateVideoUseCase,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
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
            validateVideoFileUseCase = validateVideoUseCase,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
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
            iRequestBuilder(result.cevManifestUrl!!)
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
            iRequestBuilder(
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