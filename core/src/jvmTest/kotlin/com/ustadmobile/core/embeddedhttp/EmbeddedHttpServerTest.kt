package com.ustadmobile.core.embeddedhttp

import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.util.newTestOkHttpClient
import com.ustadmobile.ihttp.okhttp.request.asOkHttpRequest
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.ihttp.request.IHttpRequest
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import okhttp3.Response as OkHttpResponse
import okhttp3.Request as OKHttpRequest

class EmbeddedHttpServerTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var mockUseCase: ContentEntryVersionServerUseCase

    private val endpointUrl = "http://endpoint.com/"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @BeforeTest
    fun setup() {
        okHttpClient = newTestOkHttpClient(temporaryFolder, json = json)
        mockUseCase = mock { }
    }

    private fun ContentEntryVersionServerUseCase.stubTextResponse(text: String) {
        stub {
            onBlocking { invoke(any(), any(), any()) }.thenAnswer {
                val httpCacheRequest = it.arguments.first() as IHttpRequest
                OkHttpResponse.Builder()
                    .request(httpCacheRequest.asOkHttpRequest())
                    .body(text.toResponseBody("text/plain".toMediaType()))
                    .code(200)
                    .message("OK")
                    .header("content-length", text.encodeToByteArray().size.toString())
                    .protocol(Protocol.HTTP_1_1)
                    .build()
            }
        }
    }

    @Test
    fun givenValidRequest_whenHttpRequestMade_thenWillInvokeUseCase() {
        val responseStr = "Hello World"
        mockUseCase.stubTextResponse(responseStr)
        val httpServer = EmbeddedHttpServer(
            port  = 0,
            contentEntryVersionServerUseCase =  { mockUseCase },
            staticUmAppFilesDir = temporaryFolder.newFolder(),
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            xapiServerUseCase = { mock { /* not used for this test */ } }
        )
        httpServer.start()
        val contentEntryVersionUid = 1234L

        //We must double encode the endpoint - nanohttpd will decode it, and then we can't figure out
        // whats the endpoint and what's the path
        val endpointSegment = UrlEncoderUtil.encode(UrlEncoderUtil.encode(endpointUrl))
        val url = "http://127.0.0.1:${httpServer.listeningPort}/e/$endpointSegment/api/content/$contentEntryVersionUid/path/file.txt"
        val response = okHttpClient.newCall(OKHttpRequest.Builder().url(url).build())
            .execute()

        val bodyStr = response.body!!.byteStream().readString()
        assertEquals(responseStr, bodyStr)
        verifyBlocking(mockUseCase) {
            invoke(any(), eq(contentEntryVersionUid), eq("path/file.txt"))
        }
    }

}