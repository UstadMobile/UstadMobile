package com.ustadmobile.core.domain.file

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Response

class BlobFileUseCaseJs : BlobFileUseCase {

    override suspend operator fun invoke(
        blobUrl: String,
        filename: String,
        mimeType: String?
    ): ByteArray? {
        return try {
            val response = window.fetch(blobUrl).await()
            val arrayBuffer = response.arrayBuffer().await()
            val uint8Array = js("new Uint8Array(arrayBuffer)")
            val length = uint8Array.length

            // Create a ByteArray and copy values directly
            val byteArray = ByteArray(length)

            // Use a JavaScript loop to copy values directly
            js("""
                for (var i = 0; i < length; i++) {
                    byteArray[i] = uint8Array[i];
                }
            """)

            byteArray
        } catch (e: Exception) {
            console.log("Error reading blob: ${e.message}")
            null
        }
    }
}

class BlobFileUseCaseStub : BlobFileUseCase {
    override suspend operator fun invoke(
        blobUrl: String,
        filename: String,
        mimeType: String?
    ): ByteArray? {
        return null
    }
}