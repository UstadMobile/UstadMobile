package com.ustadmobile.core.domain.upload

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.util.UUID
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

class ChunkedUploadServerUseCaseTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenFileUploadedInMultipleChunks_whenFinalRequestMade_thenOnCompleteCalledWithValidFile() {
        val chunkSize = 20_000
        val uploadBytes = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/container/testfile1.png"
        )!!.readAllBytes()
        val uploadDir = temporaryFolder.newFolder()
        val useCase = ChunkedUploadServerUseCaseJvm { uploadDir }
        val finalCallMessage = "Hello World"

        val uploadUuid = UUID.randomUUID().toString()
        val completedUploadDeferred =
            CompletableDeferred<CompletedChunkedUpload>()
        val onUploadCompleteFn: (CompletedChunkedUpload) -> ChunkedUploadResponse = {
            completedUploadDeferred.complete(it)
            ChunkedUploadResponse(
                statusCode = 200, body = finalCallMessage, headers = emptyMap(), contentType = "text/plain"
            )
        }
        val uploadHeaders = mapOf(
            HEADER_UPLOAD_UUID to listOf(uploadUuid),
            HEADER_IS_FINAL_CHUNK to listOf(true.toString()),
        )

        for(i in 0 .. (uploadBytes.size / chunkSize)) {
            val start =  i * chunkSize
            val end = min(start + chunkSize, uploadBytes.size)

            //IntRange end is inclusive, so use end-1
            val slice = uploadBytes.sliceArray(IntRange(start, end - 1))

            if(end == uploadBytes.size) {
                //this is final chunk - get the final call message
                val response = runBlocking {
                    useCase.onChunkReceived(
                        ChunkedUploadRequest(
                            headers = uploadHeaders,
                            chunkData = slice,
                        ),
                        onUploadComplete = onUploadCompleteFn
                    )
                }

                Assert.assertEquals(finalCallMessage, response.body)
            }else {
                runBlocking {
                    useCase.onChunkReceived(
                        request = ChunkedUploadRequest(
                            headers = uploadHeaders,
                            chunkData = slice,
                        ),
                        onUploadComplete = onUploadCompleteFn
                    )
                }
            }
        }

        val completedUpload = runBlocking {
            withTimeout(5000) {
                completedUploadDeferred.await()
            }
        }

        val completedUploadBytes = SystemFileSystem.source(completedUpload.path).buffered()
            .readByteArray()

        assertTrue(uploadBytes.contentEquals(completedUploadBytes))
    }

}