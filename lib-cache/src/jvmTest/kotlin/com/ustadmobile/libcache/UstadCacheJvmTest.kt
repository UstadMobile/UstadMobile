package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.CouponHeader
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpFileResponse
import com.ustadmobile.libcache.response.bodyAsStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.security.MessageDigest
import java.util.Base64
import kotlin.test.assertEquals


class UstadCacheJvmTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun givenFilesStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheJvm(cacheDir, cacheDb)
        val testFile = tempDir.newFile()
        testFile.outputStream().use { outputStream ->
            this::class.java.getResourceAsStream("/testfile1.png")!!.copyTo(outputStream)
            outputStream.flush()
        }


        val progressListener = object: StoreProgressListener {
            override fun onProgress() {
                //nothing yet
            }
        }

        runBlocking {
            val request = requestBuilder {
                url = "http://server.com/file.png"
            }

            ustadCache.store(
                listOf(
                    CacheEntryToStore(
                        request = request,
                        response = HttpFileResponse(
                            file = testFile,
                            mimeType = "image/png",
                            request = request,
                        )
                    )
                ),
                progressListener
            )

            //Check response body content matches
            val cacheResponse = ustadCache.retrieve(request)
            val bodyBytes = cacheResponse!!.bodyAsStream().readAllBytes()
            Assert.assertArrayEquals(testFile.readBytes(), bodyBytes)

            val dataSha256 = MessageDigest.getInstance("SHA-256").also {
                it.update(testFile.readBytes())
            }.digest()

            val sha256Header = cacheResponse.headers[CouponHeader.COUPON_ACTUAL_SHA_256]!!
            val headerSha256 = Base64.getDecoder().decode(sha256Header)
            Assert.assertArrayEquals(dataSha256, headerSha256)
            assertEquals(testFile.length(), cacheResponse.headers["content-length"]?.toLong())
            assertEquals("image/png", cacheResponse.headers["content-type"])
        }

    }

}