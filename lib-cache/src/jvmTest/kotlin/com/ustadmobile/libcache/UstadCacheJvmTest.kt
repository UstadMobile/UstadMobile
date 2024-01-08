package com.ustadmobile.libcache

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.headers.requireIntegrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.md5.Md5Digest
import com.ustadmobile.libcache.md5.urlKey
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.StringResponse
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNull


class UstadCacheJvmTest {


    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun givenFilesStored_whenRequestMade_thenWillBeRetrievedAsCacheHit() {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb,
            mimeTypeHelper = FileMimeTypeHelperImpl()
        )
        val testFile = tempDir.newFile()
        testFile.outputStream().use { outputStream ->
            this::class.java.getResourceAsStream("/testfile1.png")!!.copyTo(outputStream)
            outputStream.flush()
        }


        val request = requestBuilder {
            url = "http://server.com/file.png"
        }

        ustadCache.store(
            listOf(
                CacheEntryToStore(
                    request = request,
                    response = HttpPathResponse(
                        path = Path(testFile.absolutePath),
                        fileSystem = SystemFileSystem,
                        mimeType = "image/png",
                        request = request,
                    )
                )
            ),
        )

        //Check response body content matches
        val cacheResponse = ustadCache.retrieve(request)
        val bodyBytes = cacheResponse!!.bodyAsSource()!!.asInputStream().readAllBytes()
        Assert.assertArrayEquals(testFile.readBytes(), bodyBytes)

        val dataSha256 = MessageDigest.getInstance("SHA-256").also {
            it.update(testFile.readBytes())
        }.digest()

        val integrityHeaderVal = cacheResponse.headers.requireIntegrity()
        Assert.assertEquals(sha256Integrity(dataSha256), integrityHeaderVal)
        assertEquals(testFile.length(), cacheResponse.headers["content-length"]?.toLong())
        assertEquals("image/png", cacheResponse.headers["content-type"])

        //Body entity should have size set (to size the cache and find entries to evict).
        val md5Digest = Md5Digest()
        val cacheEntry = cacheDb.cacheEntryDao.findEntryAndBodyByKey(
            md5Digest.urlKey(request.url)
        )
        assertEquals(testFile.length(), cacheEntry?.storageSize ?: 0)
    }

    @Test
    fun givenZipStored_whenRequestMade_thenAllEntriesCanBeRetrieved() {
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb,
            mimeTypeHelper = FileMimeTypeHelperImpl()
        )
        val urlPrefix = "https://endpoint/content/ebook/"

        val zipFile = tempDir.newFile().also { file ->
            FileOutputStream(file).use { outStream ->
                this::class.java.getResourceAsStream("/childrens-literature.epub")!!.copyTo(outStream)
                outStream.flush()
            }
        }

        ustadCache.storeZip(
            zipSource = FileInputStream(zipFile).asSource().buffered(),
            urlPrefix = urlPrefix,
        )

        val mimeHelper = FileMimeTypeHelperImpl()

        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            lateinit var zipEntry: ZipEntry
            while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                if(zipEntry.isDirectory)
                    continue

                val entryBytes = zipIn.readAllBytes()
                val cacheResponse = ustadCache.retrieve(requestBuilder {
                    url = "$urlPrefix${zipEntry.name}"
                })
                val responseBytes = cacheResponse!!.bodyAsSource()!!.asInputStream().readAllBytes()
                Assert.assertArrayEquals(entryBytes, responseBytes)

                val expectedMimeType = mimeHelper.guessByExtension(zipEntry.name.substringAfterLast("."))
                    ?: "application/octet-stream"
                assertEquals(
                    expected = expectedMimeType,
                    actual = cacheResponse.headers["content-type"] ,
                    message = "Cache response for ${zipEntry.name} has expected mime type ($expectedMimeType)"
                )

                assertEquals(
                    expected = entryBytes.size,
                    actual = cacheResponse.headers["content-length"]?.toInt(),
                    message = "Cache response for ${zipEntry.name} has expected size"
                )
            }
        }
    }

    @Test
    fun givenResponseIsUpdated_whenRetrieved_thenLatestResponseWillBeReturned(){
        val cacheDir = tempDir.newFolder()
        val cacheDb = DatabaseBuilder.databaseBuilder(
            UstadCacheDb::class, "jdbc:sqlite::memory:", 1L)
            .build()
        val ustadCache = UstadCacheImpl(
            storagePath = Path(cacheDir.absolutePath),
            db = cacheDb,
            mimeTypeHelper = FileMimeTypeHelperImpl()
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
            db = cacheDb,
            mimeTypeHelper = FileMimeTypeHelperImpl()
        )

        val url = "http://server.com/file.css"
        assertNull(ustadCache.retrieve(requestBuilder(url)))
    }

}