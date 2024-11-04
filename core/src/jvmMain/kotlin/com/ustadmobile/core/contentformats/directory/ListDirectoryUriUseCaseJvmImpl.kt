package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.core.contentformats.directory.ListDirectoryUriUseCase.ListDirectoryItem
import com.ustadmobile.door.DoorUri
import java.io.File

class ListDirectoryUriUseCaseJvmImpl : ListDirectoryUriUseCase {

    override fun invoke(directoryUri: String): List<ListDirectoryItem> {
        val file = File(directoryUri)
        return listDirectoryContent(file)
    }


    private fun listDirectoryContent(file: File): List<ListDirectoryItem> {
        if (!file.exists() || !file.isDirectory) {
            throw InvalidDirectoryUriException("The provided URI is not a valid directory.")
        }
        return file.listFiles()?.map {
            ListDirectoryItem(
                uri = DoorUri(it.toURI()),
                fileName = it.name ?: FILENAME
            )
        } ?: emptyList()
    }

    override fun isDirectory(directoryUri: String): Boolean {
        val file = File(directoryUri)
        return file.exists() && file.isDirectory
    }

    companion object {
        const val FILENAME = "filename"
    }
}