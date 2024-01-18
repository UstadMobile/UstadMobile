package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import java.io.File

@Composable
actual fun rememberUstadFilePickLauncher(
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    var filePickerVisible: Boolean by remember {
        mutableStateOf(false)
    }

    FilePicker(
        show = filePickerVisible,
    ) { file ->
        filePickerVisible = false
        if(file != null) {
            val fileObj = File(file.path)
            onFileSelected(
                UstadFilePickResult(
                    uri = fileObj.toURI().toString(),
                    fileName = fileObj.name,
                )
            )
        }
    }

    return {
        filePickerVisible = true
    }
}