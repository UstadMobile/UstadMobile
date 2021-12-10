package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.UploadSessionParams
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.network.containeruploader.ContainerUploaderRequest2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.withUtf8Charset
import com.ustadmobile.door.util.NullOutputStream
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.kodein.di.*
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import io.github.aakira.napier.Napier
import com.ustadmobile.core.io.ext.readFully
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly


actual class ContainerUploader2 actual constructor(
    val request: ContainerUploaderRequest2,
    val chunkSize: Int,
    val endpoint: Endpoint,
    override val di: DI
) : DIAware{

    private val httpClient: HttpClient by di.instance()

    private val okHttpClient: OkHttpClient by di.instance()

    actual suspend fun upload(): Int = withContext(Dispatchers.IO){
        lateinit var uploadSessionParams: UploadSessionParams
        var pipeIn: PipedInputStream? = null
        var pipeOut: PipedOutputStream? = null

        var bytesUploaded: Long = 0
        var bytesToUpload: Long = -1

        var exception: Exception? = null
        var response: Response? = null

        try {
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            uploadSessionParams = httpClient.post<UploadSessionParams>(
                    "${endpoint.url}ContainerUpload2/${request.uploadUuid}/init") {
                body = defaultSerializer().write(request.entriesToUpload,
                        ContentType.Application.Json.withUtf8Charset())
            }

            if(uploadSessionParams.md5sRequired.isNotEmpty()) {
                val concatResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse2(
                        uploadSessionParams.md5sRequired,
                        mapOf("range" to listOf("bytes=${uploadSessionParams.startFrom}-")), db)

                bytesToUpload = concatResponse.actualContentLength

                val buffer = ByteArray(chunkSize)
                var bytesRead = 0

                pipeIn = PipedInputStream()
                pipeOut = PipedOutputStream(pipeIn)

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        concatResponse.writeTo(pipeOut)
                    }catch(e: Exception) {
                        Napier.e("Exception writing concat response to pipe", e)
                    }finally {
                        pipeOut.close()
                    }
                }

                while(coroutineContext.isActive && pipeIn.readFully(buffer).also { bytesRead = it } != -1) {
                    val request = Request.Builder()
                        .url("${endpoint.url}ContainerUpload2/${request.uploadUuid}/data")
                        .put(buffer.toRequestBody(MEDIA_TYPE_OCTET_STREAM, 0, bytesRead))
                        .build()

                    response = okHttpClient.newCall(request).execute()

                    if(response.code != 204 && response.code != 200) {
                        throw IOException("Data upload response: ${response.code}")
                    }
                    response.closeQuietly()

                    bytesUploaded += bytesRead
                }
            }
        }catch(e: Exception) {
            exception = e
            e.printStackTrace()
        }finally {
            //if we are here because we got canceled, read anything remainder to null so that the
            //write job will not get stuck
            pipeIn?.copyTo(NullOutputStream())
            pipeIn?.close()
            response?.closeQuietly()

            try {
                httpClient.get<Unit>("${endpoint.url}ContainerUpload2/${request.uploadUuid}/close")
            }catch(e: Exception){
                //do nothing
            }
        }

        return@withContext if(bytesUploaded == bytesToUpload) {
            JobStatus.COMPLETE
        }else if(exception != null){
            JobStatus.FAILED
        }else {
            JobStatus.PAUSED
        }
    }

    companion object {

        val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream".toMediaType()

        const val DEFAULT_CHUNK_SIZE = 200 * 1024 //200K
    }

}