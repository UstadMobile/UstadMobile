package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.HttpHeader
import com.ustadmobile.libcache.headers.requireIntegrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.StringResponse
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class UstadCacheJvmTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var rootDir: File

    private lateinit var temporaryFolderPathsProvider: CachePathsProvider

    private lateinit var cachePaths: CachePaths

    @BeforeTest
    fun setup(){
        rootDir = tempDir.newFolder()
        val rootPath = Path(rootDir.absolutePath)
        cachePaths = CachePaths(
            tmpWorkPath = Path(rootPath, "tmpWork"),
            persistentPath = Path(rootPath, "persistent"),
            cachePath = Path(rootPath, "cache")
        )
        temporaryFolderPathsProvider = CachePathsProvider {
            cachePaths
        }
    }

    private fun UstadCache.assertCanStoreAndRetrieveFileAsCacheHit(
        testFile: File,
        testUrl: String,
        mimeType: String,
        expectedContentEncoding: String? = null,
        requestHeaders: List<HttpHeader> = emptyList(),
    ) {
        val request = requestBuilder {
            url = testUrl
            requestHeaders.forEach {
                header(it.name, it.value)
            }
        }

        store(
            listOf(
                CacheEntryToStore(
                    request = request,
                    response = HttpPathResponse(
                        path = Path(testFile.absolutePath),
                        fileSystem = SystemFileSystem,
                        mimeType = mimeType,
                        request = request,
                    )
                )
            ),
        )

        //Check response body content matches
        val cacheResponse = retrieve(request)
        assertNotNull(cacheResponse, "cache response for $testUrl is not null")
        val bodyBytesRaw = cacheResponse.bodyAsSource()!!.readByteArray()

        val bodyBytesDecoded = ByteArrayInputStream(bodyBytesRaw).uncompress(
            CompressionType.byHeaderVal(cacheResponse.headers["content-encoding"])
        ).readAllBytes()

        Assert.assertArrayEquals(testFile.readBytes(), bodyBytesDecoded)

        val dataSha256 = MessageDigest.getInstance("SHA-256").also {
            it.update(testFile.readBytes())
        }.digest()

        val integrityHeaderVal = cacheResponse.headers.requireIntegrity()
        Assert.assertEquals(sha256Integrity(dataSha256), integrityHeaderVal)

        //If content-encoding was set, then content-length will not be the same as input file
        assertEquals(bodyBytesRaw.size.toLong(), cacheResponse.headers["content-length"]?.toLong())
        assertEquals(mimeType, cacheResponse.headers["content-type"])

        if(expectedContentEncoding != null) {
            val numEncodingHeaders = cacheResponse.headers.getAllByName("content-encoding").size
            if(expectedContentEncoding == "identity") {
                assertTrue(cacheResponse.headers["content-encoding"].let { it == null || it == "identity" },
                    "Content-encoding for $testUrl should be identity - can have no header, or can be set to identity")
                assertTrue(numEncodingHeaders == 1 || numEncodingHeaders == 0)
            }else {
                assertEquals(expectedContentEncoding, cacheResponse.headers["content-encoding"],
                    "Content-encoding for $testUrl should be $expectedContentEncoding")
                assertEquals(1, numEncodingHeaders)
            }
        }

    }

    private fun assertFileCanBeCachedAndRetrieved(
        testFile: File,
        testUrl: String,
        mimeType: String,
        expectContentEncoding: String? = null,
        requestHeaders: List<HttpHeader> = emptyList(),
        createLock: Boolean = false
    ) {
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb
        )

        if(createLock) {
            ustadCache.addRetentionLocks(listOf(EntryLockRequest(testUrl)))
        }

        ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
            testFile =testFile,
            testUrl = testUrl,
            mimeType = mimeType,
            expectedContentEncoding = expectContentEncoding,
            requestHeaders = requestHeaders,
        )


        val cacheEntryInDb = cacheDb.cacheEntryDao.findEntryAndBodyByKey(Md5Digest()
            .urlKey(testUrl))
        assertNotNull(cacheEntryInDb)
        val expectedPath = if(createLock) {
            cachePaths.persistentPath
        }else {
            cachePaths.cachePath
        }

        assertTrue(cacheEntryInDb.storageUri.startsWith(expectedPath.toString()),
            "Cache entry is stored in expected directory (createLock=$createLock, " +
                    "expected path = $expectedPath, actual dir = ${cacheEntryInDb.storageUri}")
    }

    @Test
    fun givenNonCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndNotCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity"
        )
    }

    @Test
    fun givenLockedEntryStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndSavedInPersistentPath() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png",
            expectContentEncoding = "identity",
            createLock = true,
        )
    }

    @Test
    fun givenCompressableFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "gzip",
            requestHeaders = listOf(HttpHeader("accept-encoding", "gzip, br, deflate"))
        )
    }

    @Test
    fun givenCompressableFileStored_whenRequestMadeWithoutAcceptEncoding_thenWillBeRetrievedAsCacheHitAndBeCompressed() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/ustadmobile-epub.js"),
            testUrl = "http://www.server.com/ustadmobile-epub.js",
            mimeType = "application/javascript",
            expectContentEncoding = "identity",
        )
    }

    @Test
    fun givenEmptyFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFile(),
            testUrl = "http://www.server.com/blank.txt",
            mimeType = "text/plain"
        )
    }

    @Test
    fun givenResponseIsUpdated_whenRetrieved_thenLatestResponseWillBeReturned(){
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb
        )

        val url = "http://server.com/file.css"
        val payloads = listOf("font-weight: bold", "font-weight: bold !important")
        payloads.forEachIndexed { index, payload ->
            ustadCache.store(listOf(
                requestBuilder(url).let {
                    CacheEntryToStore(
                        request = it,
                        response = StringResponse(
                            request = it,
                            mimeType = "text/css",
                            body = payload,
                        )
                    )
                }
            ))
        }

        val response = ustadCache.retrieve(requestBuilder(url))
        val responseBytes = response?.bodyAsUncompressedSourceIfContentEncoded()
            ?.asInputStream()?.readAllBytes()
        val responseStr = responseBytes?.let { String(it) }
        assertEquals(payloads.last(), responseStr)
    }


    @Test
    fun givenEntryNotStored_whenRetrieved_thenWillReturnNull() {
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb
        )

        val url = "http://server.com/file.css"
        assertNull(ustadCache.retrieve(requestBuilder(url)))
    }

    @Test
    fun givenResponseIsNotUpdated_whenStored_thenWillUpdateLastAccessAndValidationTime() {
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            pathsProvider = temporaryFolderPathsProvider,
            db = cacheDb
        )

        val url = "http://server.com/file.css"
        val tmpFile = tempDir.newFile().also {
            it.writeText("font-weight: bold")
        }

        val md5Digest = Md5Digest()
        val entryAfterStored = (1..2).map {
            ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
                testFile = tmpFile,
                testUrl = url,
                mimeType = "text/css"
            )
            cacheDb.cacheEntryDao.findEntryAndBodyByKey(md5Digest.urlKey(url))
        }
        assertTrue(entryAfterStored.last()!!.lastValidated > entryAfterStored.first()!!.lastValidated,
            message = "Last validated time should be updated after ")

        //Cache tmp directory should not have any leftover files.
        val cacheTmpDir = File(rootDir, "tmpWork")

        assertTrue(cacheTmpDir.exists())
        assertEquals(0, cacheTmpDir.list()!!.size)
    }


}