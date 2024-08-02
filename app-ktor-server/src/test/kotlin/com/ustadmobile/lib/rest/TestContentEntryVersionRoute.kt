package com.ustadmobile.lib.rest

import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.ihttp.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readBytes
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestContentEntryVersionRoute {

    private lateinit var ustadCache: UstadCache

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        ustadCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(temporaryFolder.newFolder().absolutePath),
        ).build()
    }

    private fun testContentEntryVersionRoute(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) {
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }
            application {
                routing {
                    CacheRoute(ustadCache)
                }
            }

            block()
        }
    }


    @Test
    fun givenEntryIsCached_whenRequested_thenResponseShouldMatch() {
        val testFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/lib/rest/file.html")
        val fileUrl = "http://localhost/api/content/1234/file.html"


        val request = requestBuilder {
            url = fileUrl
        }

        val response = HttpPathResponse(
            path = Path(testFile.absolutePath),
            fileSystem = SystemFileSystem,
            mimeType = "text/html",
            request = request,
        )

        ustadCache.store(
            listOf(
                CacheEntryToStore(request, response)
            )
        )

        testContentEntryVersionRoute {
            val serverResponse = client.get(fileUrl) {
                //Note: because we are using the test client, there is no real host. So we need to set this to match the request.
                header("X-Forwarded-Host", "localhost")
            }

            Assert.assertEquals("text/html", serverResponse.headers["content-type"])
            Assert.assertEquals(testFile.length().toString(), serverResponse.headers["content-length"])
            val responseBytes = serverResponse.readBytes()
            Assert.assertArrayEquals(testFile.readBytes(), responseBytes)
        }
    }

    @Test
    fun givenEntryNotCached_whenRequested_thenShouldReturn404() {
        testContentEntryVersionRoute {
            val serverResponse = client.get("http://localhost/api/content/1234/file.html")
            Assert.assertEquals(404, serverResponse.status.value)
        }
    }

}