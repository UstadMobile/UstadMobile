package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.CouponHeader
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.headers.requireIntegrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.useAndReadySha256
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.StringResponse
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class UstadCacheJvmTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    private fun UstadCache.assertCanStoreAndRetrieveFileAsCacheHit(
        testFile: File,
        testUrl: String,
        mimeType: String,
        cacheDb: UstadCacheDb,
        addIntegrityHeaders: Boolean = false,
    ) {
        val request = requestBuilder {
            url = testUrl
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
                        extraHeaders = if(addIntegrityHeaders) {
                            val contentSha256 = testFile.inputStream().asSource()
                                .buffered().useAndReadySha256()
                            headersBuilder {
                                header("etag", sha256Integrity(contentSha256))
                                header(CouponHeader.HEADER_ETAG_IS_INTEGRITY, "true")
                            }
                        }else {
                            null
                        }
                    )
                )
            ),
        )

        //Check response body content matches
        val cacheResponse = retrieve(request)
        val bodyBytes = cacheResponse!!.bodyAsSource()!!.asInputStream().readAllBytes()
        Assert.assertArrayEquals(testFile.readBytes(), bodyBytes)

        val dataSha256 = MessageDigest.getInstance("SHA-256").also {
            it.update(testFile.readBytes())
        }.digest()

        val integrityHeaderVal = cacheResponse.headers.requireIntegrity()
        Assert.assertEquals(sha256Integrity(dataSha256), integrityHeaderVal)
        assertEquals(testFile.length(), cacheResponse.headers["content-length"]?.toLong())
        assertEquals(mimeType, cacheResponse.headers["content-type"])

        //Body entity should have size set (to size the cache and find entries to evict).
        val md5Digest = Md5Digest()
        val cacheEntry = cacheDb.cacheEntryDao.findEntryAndBodyByKey(
            md5Digest.urlKey(request.url)
        )
        assertEquals(testFile.length(), cacheEntry?.storageSize ?: -1)
    }

    private fun assertFileCanBeCachedAndRetrieved(
        testFile: File,
        testUrl: String,
        mimeType: String,
    ) {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb
        )
        ustadCache.assertCanStoreAndRetrieveFileAsCacheHit(
            testFile =testFile,
            testUrl = testUrl,
            mimeType = mimeType,
            cacheDb = cacheDb
        )
    }

    @Test
    fun givenFileStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        assertFileCanBeCachedAndRetrieved(
            testFile = tempDir.newFileFromResource(this::class.java, "/testfile1.png"),
            testUrl = "http://www.server.com/file.png",
            mimeType = "image/png"
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
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb
        )

        val url = "http://server.com/file.css"
        val payloads = listOf("font-weight: bold", "font-weight: bold !important")
        payloads.forEach { payload ->
            ustadCache.store(listOf(
                requestBuilder(url).let {
                    CacheEntryToStore(
                        request = it,
                        response = StringResponse(
                            request = it,
                            mimeType = "text/css",
                            body = payload
                        )
                    )
                }
            ))
        }

        val response = ustadCache.retrieve(requestBuilder(url))
        val responseBytes = response?.bodyAsSource()?.asInputStream()?.readAllBytes()
        val responseStr = responseBytes?.let { String(it) }
        assertEquals(payloads.last(), responseStr)
    }


    @Test
    fun givenEntryNotStored_whenRetrieved_thenWillReturnNull() {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb
        )

        val url = "http://server.com/file.css"
        assertNull(ustadCache.retrieve(requestBuilder(url)))
    }

    @Test
    fun givenResponseIsNotUpdated_whenStored_thenWillUpdateLastAccessAndValidationTime() {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
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
                mimeType = "text/css",
                cacheDb = cacheDb,
                addIntegrityHeaders = true
            )
            cacheDb.cacheEntryDao.findEntryAndBodyByKey(md5Digest.urlKey(url))
        }
        assertTrue(entryAfterStored.last()!!.lastValidated > entryAfterStored.first()!!.lastValidated,
            message = "Last validated time should be updated after ")

        //Cache tmp directory should not have any leftover files.
        val cacheTmpDir = File(cacheDir, "tmp")
        assertTrue(cacheDir.exists())
        assertEquals(0, cacheTmpDir.list()!!.size)
    }

}