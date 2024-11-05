package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import java.io.File


@Composable
actual fun rememberUstadFolderPickLauncher(
    onFolderSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn {
    var directoryPickerVisible by remember { mutableStateOf(false) }

    DirectoryPicker(
        show = directoryPickerVisible,
    ) { directory ->
        directoryPickerVisible = false
        if (directory != null) {
            val folder = File(directory)
            val folderUri = folder.toURI().toString()
            val folderSize = folder.walkTopDown().sumOf { it.length() }

            onFolderSelected(
                UstadFilePickResult(
                    uri = folderUri,
                    fileName = folder.name,
                    mimeType = null,  // Folders do not have MIME types
                    size = folderSize
                )
            )
        }
    }

    return {
        directoryPickerVisible = true
    }
}