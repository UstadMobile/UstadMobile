package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.UploadSessionParams
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest2
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
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class ContainerUploader2(val request: ContainerUploaderRequest2,
                         val chunkSize: Int = 200*1024,
                         val endpoint: Endpoint,
                         override val di: DI) : DIAware{

    private val httpClient: HttpClient by di.instance()


    suspend fun upload(): Int = withContext(Dispatchers.IO){
        lateinit var uploadSessionParams: UploadSessionParams
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream(pipeIn)

        var bytesUploaded: Long = 0
        var bytesToUpload: Long = -1

        try {
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            uploadSessionParams = httpClient.post<UploadSessionParams>(
                    "${endpoint.url}ContainerUpload2/${request.uploadUuid}/init") {
                body = defaultSerializer().write(request.entriesToUpload,
                        ContentType.Application.Json.withUtf8Charset())
            }

            val concatResponse = db.containerEntryFileDao.generateConcatenatedFilesResponse2(
                    uploadSessionParams.md5sRequired,
                    mapOf("range" to listOf("bytes=${uploadSessionParams.startFrom}-")), db)

            bytesToUpload = concatResponse.actualContentLength

            val buffer = ByteArray(chunkSize)
            var bytesRead = 0

            GlobalScope.launch(Dispatchers.IO) {
                concatResponse.writeTo(pipeOut)
                pipeOut.close()
            }

            while(coroutineContext.isActive && pipeIn.read(buffer).also { bytesRead = it } != -1) {
                val urlConnection = (URL("${endpoint.url}ContainerUpload2/${request.uploadUuid}/data")
                        .openConnection() as HttpURLConnection).also {
                    it.doOutput = true
                    it.requestMethod = "PUT"
                    it.connectTimeout = 10000
                }

                urlConnection.outputStream.also {
                    it.write(buffer, 0, bytesRead)
                    it.flush()
                    it.close()
                }

                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if(responseCode != 204 && responseCode != 200) {
                    throw IOException("Data upload response: $responseCode")
                }

                bytesUploaded += bytesRead
            }
        }catch(e: Exception) {
            e.printStackTrace()
        }finally {
            //if we are here because we got canceled, read anything remainder to null so that the
            //write job will not get stuck
            pipeIn.copyTo(NullOutputStream())
            pipeIn.close()

            try {
                httpClient.get<Unit>("${endpoint.url}ContainerUpload2/${request.uploadUuid}/close")
            }catch(e: Exception){
                //do nothing
            }
        }

        return@withContext if(bytesUploaded == bytesToUpload) {
            JobStatus.COMPLETE
        }else {
            JobStatus.PAUSED
        }
    }

}