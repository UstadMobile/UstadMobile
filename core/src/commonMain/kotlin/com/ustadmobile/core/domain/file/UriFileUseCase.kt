package com.ustadmobile.core.domain.file


import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import kotlinx.io.readTo
import org.kodein.di.DI
import org.kodein.di.instance

class UriFileUseCase(
    private val di: DI
) {
    private val uriHelper: UriHelper by di.instance()

    data class UriFileData(
        val bytes: ByteArray?,
        val filename: String,
        val mimeType: String
    )

    /**
     * Reads file data from a URI and returns bytes, filename, and mime type
     */
    @OptIn(ExperimentalStdlibApi::class)
    suspend operator fun invoke(uriStr: String): UriFileData {
        val fileBytes = try {
            val doorUri = DoorUri.parse(uriStr)
            val fileSize = uriHelper.getSize(doorUri)

            if (fileSize <= 0) {
                null
            } else {
                val bytes = ByteArray(fileSize.toInt())
                uriHelper.openSource(doorUri).use { input ->
                    input.readTo(bytes, 0, fileSize.toInt())
                }
                bytes
            }
        } catch (e: Exception) {
            null
        }

        val filename = try {
            val doorUri = DoorUri.parse(uriStr)
            uriHelper.getFileName(doorUri) ?: uriStr.substringAfterLast('/', "file")
        } catch (e: Exception) {
            uriStr.substringAfterLast('/', "file")
        }

        val mimeType = try {
            val doorUri = DoorUri.parse(uriStr)
            uriHelper.getMimeType(doorUri) ?: getMimeTypeFromExtension(uriStr)
        } catch (e: Exception) {
            getMimeTypeFromExtension(uriStr)
        }

        return UriFileData(fileBytes, filename, mimeType)
    }

    /**
     * Get MIME type from file extension
     */
    private fun getMimeTypeFromExtension(filename: String): String {
        return when {
            filename.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            filename.endsWith(".png", ignoreCase = true) -> "image/png"
            filename.endsWith(".jpg", ignoreCase = true) ||
                    filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            filename.endsWith(".zip", ignoreCase = true) -> "application/zip"
            filename.endsWith(".json", ignoreCase = true) -> "application/json"
            else -> "application/octet-stream"
        }
    }

    companion object {
        private const val DEFAULT_FILENAME = "file"
    }
}