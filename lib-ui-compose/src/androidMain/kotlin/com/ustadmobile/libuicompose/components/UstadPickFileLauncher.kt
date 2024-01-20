package com.ustadmobile.libuicompose.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.door.ext.getFileName
import kotlinx.coroutines.launch


@Composable
actual fun rememberUstadFilePickLauncher(
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if(uri != null) {
            coroutineScope.launch {
                val fileName = context.contentResolver.getFileName(uri)
                onFileSelected(
                    UstadFilePickResult(
                        uri = uri.toString(),
                        fileName =fileName
                    )
                )
            }
        }
    }

    return {
        activityLauncher.launch(arrayOf("*/*"))
    }
}