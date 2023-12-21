package com.ustadmobile.lib.rest.upload

import com.ustadmobile.core.domain.upload.HEADER_IS_FINAL_CHUNK
import com.ustadmobile.core.domain.upload.HEADER_UPLOAD_UUID
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.response.respondText
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.UUID
import kotlin.math.min

class UploadRouteTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenFileUploadedInMultipleChunks_whenUploadComplete_thenOnCompleteCalledWithValidFile() {
        val uploadDir = temporaryFolder.newFolder()
        val uploadCompleteable = CompletableDeferred<CompletedUpload>()
        val uploadBytes = this::class.java.getResourceAsStream(
            "/com/ustadmobile/lib/rest/h5pimportroute/image.jpg"
        )!!.readAllBytes()
        val finalCallMessage = "Hello World"

        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }

            routing {
                UploadRoute(
                    uploadDir = { uploadDir },
                    path = "upload",
                    onUploadCompleted = {
                        it.call.respondText(finalCallMessage)
                        uploadCompleteable.complete(it)
                    }
                )
            }

            val chunkSize = 20_000
            val uploadUuid = UUID.randomUUID().toString()
            for(i in 0 .. (uploadBytes.size / chunkSize)) {
                val start =  i * chunkSize
                val end = min(start + chunkSize, uploadBytes.size)

                //IntRange end is inclusive, so use end-1
                val slice = uploadBytes.sliceArray(IntRange(start, end - 1))
                if(end == uploadBytes.size) {
                    //this is final chunk - get the final call message
                    val response = client.post("/upload") {
                        header(HEADER_UPLOAD_UUID, uploadUuid)
                        header(HEADER_IS_FINAL_CHUNK, true.toString())
                        contentType(ContentType.Application.OctetStream)

                        setBody(slice)
                    }.bodyAsText()
                    Assert.assertEquals(finalCallMessage, response)
                }else {
                    client.post("/upload") {
                        header(HEADER_UPLOAD_UUID, uploadUuid)
                        contentType(ContentType.Application.OctetStream)
                        setBody(slice)
                    }
                }
            }

            val completedUpload = withTimeout(5000) {
                uploadCompleteable.await()
            }

            Assert.assertArrayEquals(uploadBytes, completedUpload.file.readBytes())
        }
    }

}