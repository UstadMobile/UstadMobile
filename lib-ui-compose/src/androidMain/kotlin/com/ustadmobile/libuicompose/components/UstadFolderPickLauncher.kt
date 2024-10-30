package com.ustadmobile.libuicompose.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.launch


@Composable
actual fun rememberUstadFolderPickLauncher(
    onFolderSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val documentFile = DocumentFile.fromTreeUri(context, uri)
                if (documentFile != null && documentFile.isDirectory) {
                    val folderName = documentFile.name ?: "Unknown Folder"
                    var folderSize = 0L
                    for (file in documentFile.listFiles()) {
                        folderSize += file.length()
                    }

                    onFolderSelected(
                        UstadFilePickResult(
                            uri = uri.toString(),
                            fileName = folderName,
                            mimeType = null, // Folder itself has no MIME type
                            size = folderSize
                        )
                    )
                }
            }
        }
    }

    return {
        activityLauncher.launch(null)  // No MIME filters required for folder selection
    }
}