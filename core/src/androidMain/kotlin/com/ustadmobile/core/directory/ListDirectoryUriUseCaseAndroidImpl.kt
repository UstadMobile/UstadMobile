package com.ustadmobile.core.directory

import android.content.Context
import android.net.Uri
import com.ustadmobile.core.contentformats.directory.ListDirectoryUriUseCase
import com.ustadmobile.core.util.ext.appendSubTreePath
import com.ustadmobile.core.util.ext.toDocumentFileIncludingSubpath
import com.ustadmobile.door.DoorUri

/**
 * DocumentFile URIs on Android are more difficult - and as per docs:
 *
 * "Each document has a unique identifier within that provider. This identifier is an opaque
 * implementation detail of the provider, and as such it must not be parsed."
 *
 * When the user selects a directory from the provider, then a URI is returned for the selected
 * directory. The subfolder URIs returned by DocumentFile cannot be used to lookup a corresponding
 * DocumentFile
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