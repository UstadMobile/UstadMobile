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

@Composable
actual fun rememberUstadFilePickLauncher(
    fileExtensions: List<String>,
    mimeTypes: List<String>,
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    var filePickerVisible: Boolean by remember {
        mutableStateOf(false)
    }

    val localDi = localDI()

    val fileMimeTypeHelper: MimeTypeHelper by localDi.instance()

    FilePicker(
        show = filePickerVisible,
        fileExtensions = fileExtensions,
    ) { file ->
        filePickerVisible = false
        if(file != null) {
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

    return {
        filePickerVisible = true
    }
}