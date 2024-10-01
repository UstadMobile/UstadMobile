package com.ustadmobile.libuicompose.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.getFileNameAndSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun rememberUstadFilePickLauncher(
    options: PickFileOptions,
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        handleSelectedUri(uri, context, coroutineScope, onFileSelected)
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        handleSelectedFolderUri(uri, context, coroutineScope, onFileSelected)
    }

    return { pickerOptions ->
        when (pickerOptions.pickType) {
            PickType.FILE -> {
                val mimeTypes = pickerOptions.mimeTypes.ifEmpty {
                    if (pickerOptions.fileExtensions.isNotEmpty()) {
                        pickerOptions.fileExtensions.map { "application/$it" }
                    } else {
                        listOf("*/*")
                    }
                }
                fileLauncher.launch(mimeTypes.toTypedArray())
            }
            PickType.FOLDER -> folderLauncher.launch(null)
        }
    }
}

private fun handleSelectedUri(
    uri: Uri?,
    context: Context,
    coroutineScope: CoroutineScope,
    onFileSelected: (UstadFilePickResult) -> Unit
) {
    if (uri != null) {
        coroutineScope.launch {
            try {
                val (fileName, fileSize) = context.contentResolver.getFileNameAndSize(uri)
                onFileSelected(
                    UstadFilePickResult(
                        uri = uri.toString(),
                        fileName = fileName,
                        mimeType = context.contentResolver.getType(uri),
                        size = fileSize,
                    )
                )
            } catch (e: Exception) {
                // Log the exception for debugging purposes
                Log.e("FilePickLauncher", "Failed to get file details for URI: $uri", e)
                // Handle or inform about the error as needed
            }
        }
    } else {
        // Handle case where URI is null (should not normally happen)
        Log.e("FilePickLauncher", "URI is null when handling file selection")
        // Optionally, inform the user or log this scenario
    }
}

private fun handleSelectedFolderUri(
    uri: Uri?,
    context: Context,
    coroutineScope: CoroutineScope,
    onFileSelected: (UstadFilePickResult) -> Unit
) {
    if (uri != null) {
        coroutineScope.launch {
            try {
                val folderPath = uri.toString()  // Example: "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.example.app%2Ffiles%2FDocuments"
                val folderName = uri.lastPathSegment ?: ""
                onFileSelected(
                    UstadFilePickResult(
                        uri = folderPath,
                        fileName = folderName,
                        mimeType = "application/octet-stream", // Set an appropriate MIME type for folders
                        size = 0 // Set size to 0 or handle differently for folders
                    )
                )
            } catch (e: Exception) {
                // Log the exception for debugging purposes
                Log.e("FolderPickLauncher", "Failed to get folder details for URI: $uri", e)
                // Handle or inform about the error as needed
            }
        }
    } else {
        // Handle case where URI is null (should not normally happen)
        Log.e("FolderPickLauncher", "URI is null when handling folder selection")
        // Optionally, inform the user or log this scenario
    }
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
