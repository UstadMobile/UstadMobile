package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.ustadmobile.libcache.headers.MimeTypeHelper
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import java.io.File

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.SwingUtilities

@Composable
actual fun rememberUstadFilePickLauncher(
    options: PickFileOptions,
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    var filePickerVisible: Boolean by remember { mutableStateOf(false) }
    val localDi = localDI()
    val fileMimeTypeHelper: MimeTypeHelper by localDi.instance()
    val coroutineScope = rememberCoroutineScope()

    when (options.pickType) {
        PickType.FILE -> {
            FilePicker(
                show = filePickerVisible,
                fileExtensions = options.fileExtensions,
            ) { file ->
                filePickerVisible = false
                if (file != null) {
                    val fileObj = File(file.path)
                    val fileUri = fileObj.toURI().toString()
                    onFileSelected(
                        UstadFilePickResult(
                            uri = fileUri,
                            fileName = fileObj.name,
                            size = fileObj.length(),
                            mimeType = fileMimeTypeHelper.mimeTypeByUri(fileUri)
                        )
                    )
                }
            }
        }
        PickType.FOLDER -> {
            if (filePickerVisible) {
                coroutineScope.launch(Dispatchers.IO) {
                    val folder = selectFolder()
                    if (folder != null) {
                        val folderUri = folder.toURI().toString()
                        onFileSelected(
                            UstadFilePickResult(
                                uri = folderUri,
                                fileName = folder.name,
                                size = -1, // Folders don't have a size
                                mimeType = null // Folders don't have a mime type
                            )
                        )
                    }
                    filePickerVisible = false
                }
            }
        }
    }

    return { pickerOptions ->
        filePickerVisible = true
    }
}

private fun selectFolder(): File? {
    var selectedFolder: File? = null
    SwingUtilities.invokeAndWait {
        val fileDialog = FileDialog(null as Frame?, "Select Folder", FileDialog.LOAD).apply {
            isMultipleMode = false
            file = null
            setFilenameFilter { _, name -> true } // Accept all names
            isVisible = true
        }

        if (fileDialog.directory != null) {
            selectedFolder = File(fileDialog.directory)
        }
    }
    return selectedFolder
}

// Backward compatibility function
@Composable
actual fun rememberUstadFilePickLauncher(
    fileExtensions: List<String>,
    mimeTypes: List<String>,
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    return rememberUstadFilePickLauncher(
        options = PickFileOptions(fileExtensions, mimeTypes),
        onFileSelected = onFileSelected
    )
}