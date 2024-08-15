package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.io.range
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.io.File
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlobUploadServerUseCaseTest {


    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private data class TestBlobUpload(
        val tmpFile: File,
        val sha256: String,
        val url: String,
    )

    private lateinit var cache: UstadCache

    private lateinit var json: Json

    private lateinit var tmpDir: File

    private lateinit var testUploads: List<TestBlobUpload>

    private lateinit var uploadRequest: BlobUploadRequest

    private lateinit var batchUuid: UUID

    private lateinit var learningSpace: LearningSpace

    private lateinit var saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase

    @BeforeTest
    fun setup() {
        cache = mock {  }
        json = Json {
            encodeDefaults = true
        }

        tmpDir = temporaryFolder.newFolder()

        val filesToUpload = (1..3).map {
            temporaryFolder.newFileFromResource(javaClass, "/com/ustadmobile/core/container/testfile${it}.png")
        }
        learningSpace = LearningSpace("https://endpoint.com/")
        val urlPrefix = "https://endpoint.com/api/blob/"

        batchUuid = UUID.randomUUID()

        testUploads = filesToUpload.map {
            val sha256= it.inputStream().use { fileIn ->
                fileIn.readSha256()
            }.encodeBase64()

            TestBlobUpload(
                tmpFile = it,
                sha256 = sha256,
                url = "$urlPrefix$sha256"
            )
        }

        uploadRequest = BlobUploadRequest(
            blobs = testUploads.map { testUpload ->
                BlobUploadRequestItem(
                    blobUrl = testUpload.url,
                    size = testUpload.tmpFile.length()
                )
            },
            batchUuid = batchUuid.toString()
        )
        saveLocalUrisAsBlobsUseCase = mock {  }
    }


    @Test
    fun givenNewRequest_whenInitializedAndBlobsUploaded_thenWillStoreEntries() {
        val batchUploadEndpoint = BlobUploadServerUseCase(
            httpCache = cache,
            tmpDir = Path(tmpDir.absolutePath),
            json = json,
            saveLocalUrisAsBlobsUseCase = saveLocalUrisAsBlobsUseCase,
        )

        cache.stub {
            on { getEntries(any()) }.thenAnswer { invocation ->
                emptyMap<String, CacheEntry>()
            }
        }

        runBlocking {
            val uploadResponse = batchUploadEndpoint
                .onStartUploadSession(uploadRequest)

            assertEquals(testUploads.size, uploadResponse.blobsToUpload.size)
            uploadResponse.blobsToUpload.forEach {  blobToUpload ->
                val testUpload = testUploads.first { it.url == blobToUpload.blobUrl }
                val bodyPath = Path(testUpload.tmpFile.absolutePath)

                batchUploadEndpoint.onStoreItem(
                    blobToUpload.blobUrl,
                    bodyPath = bodyPath,
                    requestHeaders = iHeadersBuilder {  }
                )

                verify(cache).store(
                    storeRequest = argWhere { entryToStoreList ->
                        entryToStoreList.any {
                            it.request.url == blobToUpload.blobUrl &&
                                    it.responseBodyTmpLocalPath == bodyPath
                        }
                    },
                    progressListener = anyOrNull()
                )
            }
        }


    }

    @Test
    fun givenPartialRequest_whenInitialzedAndBlobsUploaded_thenWillListRemainingItemsAndStoreEntries() {
        val tmpPath = Path(tmpDir.absolutePath)
        val batchUploadEndpoint = BlobUploadServerUseCase(
            httpCache = cache,
            tmpDir = tmpPath,
            json = json,
            saveLocalUrisAsBlobsUseCase = saveLocalUrisAsBlobsUseCase,
        )

        //Mark first url as completed - it should not appear in response.
        val firstUrl = testUploads.first().url
        cache.stub {
            on { getEntries(any()) }.thenAnswer { invocation ->
                mapOf(
                    firstUrl to CacheEntry(
                        key = firstUrl,
                        url = firstUrl,
                    )
                )
            }
        }

        runBlocking {
            val firstResponse = batchUploadEndpoint
                .onStartUploadSession(uploadRequest)

            assertEquals(2, firstResponse.blobsToUpload.size)

            //now simulate partial upload of the second
            val secondFileUploadUuid = firstResponse.blobsToUpload.first().uploadUuid
            val secondFilePath = Path(tmpPath, secondFileUploadUuid)
            SystemFileSystem.createDirectories(Path(tmpPath, batchUuid.toString()))
            val numOfBytesPartiallyUploaded = 10000L
            testUploads[1].tmpFile.inputStream().range(0, numOfBytesPartiallyUploaded - 1).use { partialIn ->
                SystemFileSystem.sink(secondFilePath).buffered().asOutputStream().use { fileOut ->
                    partialIn.copyTo(fileOut)
                    fileOut.flush()
                }
            }

            val secondResponse = batchUploadEndpoint
                .onStartUploadSession(uploadRequest)
            val responseStartFrom = secondResponse.blobsToUpload.first {
                it.blobUrl == testUploads[1].url
            }
            assertEquals(numOfBytesPartiallyUploaded, responseStartFrom.fromByte)
        }
    }

}