package com.ustadmobile.core.directory

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.ustadmobile.core.contentformats.directory.InvalidDirectoryUriException
import com.ustadmobile.core.contentformats.directory.ListDirectoryUriUseCase
import com.ustadmobile.door.DoorUri

class ListDirectoryUriUseCaseAndroidImpl(private val context: Context) : ListDirectoryUriUseCase {

    override fun invoke(directoryUri: String): List<ListDirectoryUriUseCase.ListDirectoryItem> {
        val documentFile = DocumentFile.fromTreeUri(context, android.net.Uri.parse(directoryUri))
        return listDirectoryContent(documentFile)
    }


    private fun listDirectoryContent(documentFile: DocumentFile?): List<ListDirectoryUriUseCase.ListDirectoryItem> {
        if (documentFile == null || !documentFile.isDirectory) {
            throw InvalidDirectoryUriException("The provided URI is not a valid directory.")
        }
        return documentFile.listFiles().map {
            ListDirectoryUriUseCase.ListDirectoryItem(
                uri = DoorUri(it.uri),
                fileName = it.name ?: FILENAME
            )
        }
    }

    override fun isDirectory(directoryUri: String): Boolean {
        val uri = android.net.Uri.parse(directoryUri)
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        return documentFile?.isDirectory == true
    }

    companion object {
        const val FILENAME = "filename"
    }
}