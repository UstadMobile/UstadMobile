package com.ustadmobile.libcache.okhttp

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.NapierLoggingAdapter
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheImpl
import com.ustadmobile.libcache.base64.encodeBase64
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.CouponHeader
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.util.test.ResourcesDispatcher
import com.ustadmobile.util.test.initNapierLog
import kotlinx.io.files.Path
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import java.io.File
import java.security.MessageDigest
import java.time.Duration
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class UstadCacheInterceptorTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var cacheDir: File

    private lateinit var cacheDb: UstadCacheDb

    private lateinit var ustadCache: UstadCache

    private lateinit var cacheListener: UstadCache.CacheListener

    private fun ByteArray.sha256(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(this)
        return digest.digest()
    }

    @BeforeTest
    fun setup() {
        initNapierLog()
        val logger = NapierLoggingAdapter()
        cacheListener = mock { }
        cacheDir = tempDir.newFolder()
        cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        ustadCache = spy(
            UstadCacheImpl(
                storagePath = Path(cacheDir.absolutePath),
                db = cacheDb,
                mimeTypeHelper = FileMimeTypeHelperImpl(),
                logger = logger,
                listener = cacheListener,
            )
        )
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                UstadCacheInterceptor(
                    ustadCache, cacheDir, logger = logger,
                ))
            .callTimeout(Duration.ofSeconds(500))
            .connectTimeout(Duration.ofSeconds(500))
            .readTimeout(Duration.ofSeconds(500))
            .build()
    }

    fun UstadCache.verifyUrlStored(requestUrl: String) {
        verify(this, timeout(5000)).store(
            argWhere { storedEntries ->
                storedEntries.any { it.request.url ==  requestUrl }
            },
            anyOrNull()
        )
    }

    @Test
    fun givenEntryNotYetCached_whenRequested_thenWillRespondAndCacheIt() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "image/png")
            }

            it.start()
        }

        val requestUrl = "${mockWebServer.url("/testfile1.png")}"
        val response = okHttpClient.newCall(
            Request.Builder().url(requestUrl).build()
        ).execute()

        val responseBytes = response.body!!.bytes()
        val resourceBytes = javaClass.getResourceAsStream("/testfile1.png")!!
            .readAllBytes()
        Assert.assertArrayEquals(resourceBytes, responseBytes)
        ustadCache.verifyUrlStored(requestUrl)
    }

    @Test
    fun givenImmutableEntryWasCached_whenRequested_thenCacheWillHit() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "image/png")
                it.addHeader("cache-control", "immutable")
            }

            it.start()
        }

        val requestUrl = mockWebServer.url("/testfile1.png").toString()
        val request = Request.Builder().url(requestUrl).build()
        val initResponseBytes = okHttpClient.newCall(request).execute().use {
            it.body!!.bytes()
        }

        /*
         * Note: the verify call can pass immediately as soon as the UstadCache.store function is
         * called, at which time the caching of the actual request might not be complete. Therefor
         * we will wait for the listener to confirm that the request has been stored.
         */
        verify(cacheListener, timeout(5000)).onEntriesStored(
            argWhere { entries -> entries.any { it.request.url == requestUrl } }
        )

        val cachedResponse = okHttpClient.newCall(request).execute()
        val resourceBytes = javaClass.getResourceAsStream("/testfile1.png")!!.readAllBytes()
        val cacheResponseBytes = cachedResponse.use { it.body!!.bytes() }
        Assert.assertArrayEquals(resourceBytes, initResponseBytes)
        Assert.assertArrayEquals(initResponseBytes, cacheResponseBytes)

        //Should only have made one request
        assertEquals(1, mockWebServer.requestCount)

        //The cached response should have a sha256 header
        assertEquals(resourceBytes.sha256().encodeBase64(),
            cachedResponse.header(CouponHeader.COUPON_ACTUAL_SHA_256))
    }

    @Test
    fun givenEntryIsStaleAndValidatable_whenRequested_thenIsValidated() {

    }

    @Test
    fun givenRequestHasNoStoreHeader_whenRequested_thenIsNotCached() {

    }

    @Test
    fun givenResponseHasNoStoreHeader_whenRequested_thenIsNotCached() {

    }


}