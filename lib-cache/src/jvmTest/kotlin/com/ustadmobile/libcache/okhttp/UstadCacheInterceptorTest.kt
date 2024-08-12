package com.ustadmobile.libcache.okhttp

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.libcache.CachePaths
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheImpl
import com.ustadmobile.libcache.assertTempDirectoryIsEmptied
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_ETAG_IS_INTEGRITY
import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_X_INTERCEPTOR_PARTIAL_FILE
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.RangeInputStream
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.partial.ContentRange.Companion.parseRangeHeader
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
import com.ustadmobile.util.test.ResourcesDispatcher
import com.ustadmobile.util.test.initNapierLog
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.io.File
import java.security.MessageDigest
import java.time.Duration
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UstadCacheInterceptorTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var cacheRootDir: File

    private lateinit var cachePathsProvider: CachePathsProvider

    private lateinit var interceptorTmpDir: File

    private lateinit var cacheDb: UstadCacheDb

    private lateinit var ustadCache: UstadCacheImpl

    private lateinit var cacheListener: UstadCache.CacheListener

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

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
        cacheRootDir = tempDir.newFolder("cachedir")
        cachePathsProvider = CachePathsProvider {
            Path(cacheRootDir.absolutePath).let {
                CachePaths(
                    tmpWorkPath = Path(it, "tmpWork"),
                    persistentPath = Path(it, "persistent"),
                    cachePath = Path(it, "cache"),
                )
            }
        }
        interceptorTmpDir = tempDir.newFolder("interceptor-tmp")
        cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        ustadCache = spy(
            UstadCacheImpl(
                pathsProvider = cachePathsProvider,
                db = cacheDb,
                logger = logger,
                listener = cacheListener,
            )
        )
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                UstadCacheInterceptor(
                    ustadCache, { interceptorTmpDir} ,
                    logger = logger,
                    json = json,
                ))
            .callTimeout(Duration.ofSeconds(500))
            .connectTimeout(Duration.ofSeconds(500))
            .readTimeout(Duration.ofSeconds(500))
            .build()
    }

    private fun UstadCache.verifyUrlStored(requestUrl: String) {
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

        val cacheResponse = ustadCache.retrieve(iRequestBuilder(requestUrl))
        assertEquals(CompressionType.NONE,
            CompressionType.byHeaderVal(cacheResponse!!.headers["content-encoding"]),
            "Non-compressable mime type should not be encoded"
        )

        interceptorTmpDir.assertTempDirectoryIsEmptied()
    }

    @Test
    fun givenCompressableEntryNotYetCachedNotEncoded_whenRequested_thenWillRespondAndCacheIt() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "application/javascript")
            }

            it.start()
        }

        val requestUrl = "${mockWebServer.url("/ustadmobile-epub.js")}"
        val response = okHttpClient.newCall(
            Request.Builder().url(requestUrl).build()
        ).execute()
        val responseBytes = response.body!!.bytes()
        val resourceBytes = javaClass.getResourceAsStream("/ustadmobile-epub.js")!!
            .readAllBytes()
        Assert.assertArrayEquals(resourceBytes, responseBytes)

        //Now check the cached response is compressed
        val cacheResponse = ustadCache.retrieve(
            iRequestBuilder(requestUrl) {
                header("accept-encoding", "gzip, br, deflate")
            }
        )
        assertNotEquals(CompressionType.NONE, CompressionType.byHeaderVal(
            cacheResponse!!.headers["content-encoding"]),
            "compression type should not be none - e.g. should be compressed")
        val cacheResponseBytes = cacheResponse.bodyAsUncompressedSourceIfContentEncoded()!!.readByteArray()
        Assert.assertArrayEquals(resourceBytes, cacheResponseBytes)
    }


    @Test
    fun givenCompressableEntryNotYetCachedAlreadyEncoded_whenRequested_thenWillRespondAndCacheIt() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass, contentEncoding = "gzip") {
                it.addHeader("content-type", "application/javascript")
            }

            it.start()
        }

        val requestUrl = "${mockWebServer.url("/ustadmobile-epub.js")}"
        val response = okHttpClient.newCall(
            Request.Builder().url(requestUrl).build()
        ).execute()
        val responseBytes = response.body!!.bytes()
        val resourceBytes = javaClass.getResourceAsStream("/ustadmobile-epub.js")!!
            .readAllBytes()
        Assert.assertArrayEquals(resourceBytes, responseBytes)

        //Now check the cached response is compressed
        val cacheResponse = ustadCache.retrieve(
            iRequestBuilder(requestUrl) {
                header("accept-encoding", "gzip, deflate, br")
            }
        )
        assertNotEquals(CompressionType.NONE, CompressionType.byHeaderVal(
            cacheResponse!!.headers["content-encoding"]),
            "compression type should not be none - e.g. should be compressed")
        val cacheResponseBytes = cacheResponse.bodyAsUncompressedSourceIfContentEncoded()!!.readByteArray()
        Assert.assertArrayEquals(resourceBytes, cacheResponseBytes)
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

        assertEquals(
            sha256Integrity(resourceBytes.sha256()), cachedResponse.headers["etag"],
            message = "Cached response should have etag set to the sha256 integrity string")
        assertEquals("true", cachedResponse.header(HEADER_ETAG_IS_INTEGRITY))
        interceptorTmpDir.assertTempDirectoryIsEmptied()
    }

    @Test
    fun givenEntryIsStaleAndValidatable_whenRequested_thenIsValidated() {
        val etagVal = "etagVal"
        val resourceBytes = javaClass.getResourceAsStream("/testfile1.png")!!.readAllBytes()

        val mockWebServer = MockWebServer().also {
            it.dispatcher = object: ResourcesDispatcher(javaClass) {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val response = super.dispatch(request)
                        .setHeader("etag", etagVal)
                    return if(request.headers["if-none-match"] == etagVal) {
                        //Validation response will not normally contain content-length and content-type headers
                        response.setBody("").setResponseCode(304)
                    }else {
                        response.setHeader("content-type", "image/png")
                            .setHeader("content-length", resourceBytes.size.toString())
                    }
                }
            }

            it.start()
        }

        val requestUrl = mockWebServer.url("/testfile1.png").toString()
        val request = Request.Builder().url(requestUrl).build()
        val initResponseBytes = okHttpClient.newCall(request).execute().use {
            it.body!!.bytes()
        }

        verify(cacheListener, timeout(5000)).onEntriesStored(
            argWhere { entries -> entries.any { it.request.url == requestUrl } }
        )

        ustadCache.commit()
        val storedEntryAfterRequest = cacheDb.cacheEntryDao.findEntryAndBodyByKey(
            Md5Digest().urlKey(requestUrl))

        val cachedResponse = okHttpClient.newCall(request).execute()
        val cachedResponseBytes = cachedResponse.use {
            it.body!!.bytes()
        }


        Assert.assertArrayEquals(resourceBytes, initResponseBytes)
        Assert.assertArrayEquals(initResponseBytes, cachedResponseBytes)

        assertEquals(2, mockWebServer.requestCount)

        //The second request should have an if-none-match header
        mockWebServer.takeRequest()
        val validationRequest = mockWebServer.takeRequest()
        assertEquals(etagVal, validationRequest.getHeader("if-none-match"))

        ustadCache.commit()
        val storedEntryAfterValidation = cacheDb.cacheEntryDao.findEntryAndBodyByKey(
            Md5Digest().urlKey(requestUrl))

        assertNotNull(storedEntryAfterValidation)
        assertNotNull(storedEntryAfterRequest)
        assertTrue(storedEntryAfterValidation.lastValidated > storedEntryAfterRequest.lastValidated,
            "Last validated time in cache db should be updated")
        val headersAfterValidation = IHttpHeaders.fromString(
            storedEntryAfterValidation.responseHeaders)
        assertEquals("image/png", headersAfterValidation["content-type"])
        assertEquals(resourceBytes.size.toString(), headersAfterValidation["content-length"])

        interceptorTmpDir.assertTempDirectoryIsEmptied()
    }

    @Test
    fun givenRequestNotStorable_whenRequested_thenIsNotStored() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "image/png")
                it.addHeader("cache-control", "immutable")
            }

            it.start()
        }

        val requestUrl = mockWebServer.url("/testfile1.png").toString()
        val request = Request.Builder()
            .url(requestUrl)
            .addHeader("cache-control", "no-store")
            .build()
        val responseBytes = okHttpClient.newCall(request).execute().use {
            it.body!!.bytes()
        }
        verifyNoInteractions(ustadCache)
        Assert.assertArrayEquals(
            javaClass.getResourceAsStream("/testfile1.png")!!.readAllBytes(),
            responseBytes
        )
        interceptorTmpDir.assertTempDirectoryIsEmptied()
    }

    @Test
    fun givenResponseHasNoStoreHeader_whenRequested_thenIsNotStored() {
        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "image/png")
                it.addHeader("cache-control", "no-store")
            }

            it.start()
        }

        val requestUrl = mockWebServer.url("/testfile1.png").toString()
        val request = Request.Builder()
            .url(requestUrl)
            .build()
        val responseBytes = okHttpClient.newCall(request).execute().use {
            it.body!!.bytes()
        }
        verify(ustadCache, times(0)).store(anyOrNull(), anyOrNull())
        Assert.assertArrayEquals(
            javaClass.getResourceAsStream("/testfile1.png")!!.readAllBytes(),
            responseBytes
        )
        interceptorTmpDir.assertTempDirectoryIsEmptied()
    }


    @Test
    fun givenResponsePartiallyStored_whenRequestedWithResumeUuid_thenWillResume() {
        val resourcePath = "/testfile1.png"
        val resumeDir = tempDir.newFolder()
        val resumeFile = File(resumeDir, "partial")
        val resumeFileMetadata = File(resumeDir, "partial.json")
        val etag = "aa11"
        resumeFileMetadata.writeText(
            json.encodeToString(UstadCacheInterceptor.PartialFileMetadata(
                etag = etag,
                lastModified = null
            ))
        )
        resumeFile.outputStream().use { resumeFileOut ->
            RangeInputStream(
                this::class.java.getResourceAsStream(resourcePath)!!, 0, 999
            ).copyTo(resumeFileOut)
            resumeFileOut.flush()
        }

        val mockWebServer = MockWebServer().also {
            it.dispatcher = ResourcesDispatcher(javaClass) {
                it.addHeader("content-type", "image/png")
                it.addHeader("etag", etag)
            }

            it.start()
        }

        val requestUrl = "${mockWebServer.url(resourcePath)}"
        val response = okHttpClient.newCall(
            Request.Builder().url(requestUrl)
                .addHeader(HEADER_X_INTERCEPTOR_PARTIAL_FILE, resumeFile.toString())
                .build()
        ).execute()
        response.body?.bytes() //Read body


        val cacheResponse = ustadCache.retrieve(
            iRequestBuilder(requestUrl) {
                header("accept-encoding", "gzip, br, deflate")
            }
        )
        assertNotNull(cacheResponse)

        val cacheResponseBytes = cacheResponse
            .bodyAsUncompressedSourceIfContentEncoded()!!.readByteArray()
        val resourceBytes = this::class.java.getResourceAsStream(resourcePath)!!.readAllBytes()
        Assert.assertArrayEquals(
            resourceBytes,
            cacheResponseBytes)

        val request = mockWebServer.takeRequest()
        assertEquals(etag, request.headers["if-range"])

        val rangeHeader =  parseRangeHeader(request.headers["range"]!!,
            resourceBytes.size.toLong())
        assertEquals(1000L, rangeHeader.fromByte)
    }


}