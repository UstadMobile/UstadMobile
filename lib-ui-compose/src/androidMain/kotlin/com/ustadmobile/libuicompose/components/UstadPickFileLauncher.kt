package com.ustadmobile.libuicompose.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.getFileNameAndSize
import kotlinx.coroutines.launch


@Composable
actual fun rememberUstadFilePickLauncher(
    fileExtensions: List<String>,
    mimeTypes: List<String>,
    onFileSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if(uri != null) {
            coroutineScope.launch {
                val (fileName, fileSize) = context.contentResolver.getFileNameAndSize(uri)
                onFileSelected(
                    UstadFilePickResult(
                        uri = uri.toString(),
                        fileName = fileName,
                        mimeType = context.contentResolver.getType(uri),
                        size = fileSize,
                    )
                )
            }
        }
    }

    return {
        activityLauncher.launch(mimeTypes.ifEmpty { listOf("*/*") }.toTypedArray())
    }
}