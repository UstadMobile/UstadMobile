package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.core.contentformats.directory.ListDirectoryUriUseCase.ListDirectoryItem
import com.ustadmobile.door.DoorUri
import java.io.File
import java.net.URI

class ListDirectoryUriUseCaseJvmImpl : ListDirectoryUriUseCase {

    override fun invoke(directoryUri: String): List<ListDirectoryItem> {
        val file = uriToFile(directoryUri)
        return listDirectoryContent(file)
    }


    private fun listDirectoryContent(file: File): List<ListDirectoryItem> {
        if (!file.exists() || !file.isDirectory) {
            throw InvalidDirectoryUriException("The provided URI is not a valid directory.")
        }
        return file.listFiles()?.map {
            ListDirectoryItem(
                uri = DoorUri(it.toURI()),
                fileName = it.name
            )
        } ?: emptyList()
    }

    override fun isDirectory(directoryUri: String): Boolean {
        val file = uriToFile(directoryUri)
        return file.exists() && file.isDirectory
    }

    private fun uriToFile(directoryUri: String): File {
        return try {
            val uri = URI(directoryUri)
            if (uri.scheme == "file") File(uri) else File(directoryUri)
        } catch (e: Exception) {
            // Fall back to treating the URI as a direct file path if URI parsing fails
            File(directoryUri)
        }
    }

    companion object {
        const val FILENAME = "filename"
    }
}