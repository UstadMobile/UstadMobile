package com.ustadmobile.core.directory

import android.content.Context
import android.net.Uri
import com.ustadmobile.core.contentformats.directory.ListDirectoryUriUseCase
import com.ustadmobile.core.util.ext.appendSubTreePath
import com.ustadmobile.core.util.ext.toDocumentFileIncludingSubpath
import com.ustadmobile.door.DoorUri

/**
 * DocumentFile URIs on Android are more difficult - only the root Uri that we receive via
 * ACTION_OPEN_DOCUMENT_TREE will work with DocumentFile.fromTreeUri . See
 * com.ustadmobile.core.util.ext.UriExt for additional notes on the workaround approach.
 */
class ListDirectoryUriUseCaseAndroidImpl(private val context: Context) : ListDirectoryUriUseCase {

    override fun invoke(directoryUri: String): List<ListDirectoryUriUseCase.ListDirectoryItem> {
        val documentFile = Uri.parse(directoryUri).toDocumentFileIncludingSubpath(context)

        return documentFile.listFiles().mapNotNull {
            val nameVal = it.name

            when {
                it.isDirectory && nameVal != null -> {
                    ListDirectoryUriUseCase.ListDirectoryItem(
                        uri = DoorUri(Uri.parse(directoryUri).appendSubTreePath(nameVal)),
                        fileName = nameVal,
                    )
                }

                it.isFile -> {
                    ListDirectoryUriUseCase.ListDirectoryItem(
                        uri = DoorUri(it.uri),
                        fileName = nameVal ?: UNKNOWN_FILENAME
                    )
                }

                else -> {
                    null
                }
            }
        }
    }

    override fun isDirectory(directoryUri: String): Boolean {
        try {
            return invoke(directoryUri).isNotEmpty()
        }catch(e: Throwable) {
            return false
        }
    }

    companion object {
        const val UNKNOWN_FILENAME = "unknown"
    }
}