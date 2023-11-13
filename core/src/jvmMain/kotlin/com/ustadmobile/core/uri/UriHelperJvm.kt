package com.ustadmobile.core.uri

import com.ustadmobile.core.io.await
import com.ustadmobile.door.DoorUri
import com.ustadmobile.libcache.FileMimeTypeHelperImpl
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.discardRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import okhttp3.OkHttpClient
import kotlin.io.path.toPath
import okhttp3.Request as OkHttpRequest

class UriHelperJvm(
    private val mimeTypeHelperImpl: FileMimeTypeHelperImpl,
    private val httpClient: HttpClient,
    private val okHttpClient: OkHttpClient,
    private val fileSystem: FileSystem = SystemFileSystem,
): UriHelper {

    override suspend fun getMimeType(uri: DoorUri): String? {
        val uriLowercase = uri.toString().lowercase()

        //In future: can use cache
        return if(uriLowercase.startsWith("http://") || uriLowercase.startsWith("https://")) {
            val response = httpClient.head(uri.toString())
            response.headers["content-type"].also {
                response.discardRemaining()
            }
        }else {
             mimeTypeHelperImpl.mimeTypeByUri(uri.toString())
        }
    }


    @Suppress("NewApi") //This is JVM only
    override suspend fun getSize(uri: DoorUri): Long {
        val uriLowercase = uri.toString().lowercase()

        return if(uriLowercase.startsWith("http://") || uriLowercase.startsWith("https://")) {
            val response = httpClient.head(uri.toString())
            response.headers["content-length"].also {
                response.discardRemaining()
            }?.toLongOrNull() ?: -1
        }else {
            withContext(Dispatchers.IO) {
                uri.uri.toPath().toFile().length()
            }
        }
    }

    override suspend fun getFileName(uri: DoorUri): String {
        return uri.toString().substringAfterLast("/").substringBefore("?")
    }


    override suspend fun openSource(uri: DoorUri): Source {
        val uriLowercase = uri.toString().lowercase()
        return if(uriLowercase.startsWith("file:")) {
            fileSystem.source(Path(uri.uri.path)).buffered()
        }else {
            //OkHttp will be cached
            val okRequest = OkHttpRequest.Builder()
                .url(uri.toString())
                .build()
            val call = okHttpClient.newCall(okRequest)

            val response = call.await()
            return withContext(Dispatchers.IO) {
                response.body?.byteStream()?.asSource()?.buffered()
            } ?: throw IllegalStateException("response as no body")
        }
    }
}
