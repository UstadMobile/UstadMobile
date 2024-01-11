package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun UstadFileDropZone(
    onFileDropped: (UstadFilePickResult) -> Unit,
    modifier: Modifier,
    content: @Composable () ->  Unit,
) {
    var isDragging by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .let {
                if(isDragging) {
                    it.border(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                }else {
                    it
                }
            }
            .onExternalDrag(
                enabled = true,
                onDragStart = {
                    isDragging = true
                },
                onDragExit = {
                    isDragging = false
                },
                onDrop = { state ->
                    isDragging = false
                    val data = state.dragData
                    if(data is DragData.FilesList) {
                        val fileList = data.readFiles()
                        val fileUri = fileList.firstOrNull()
                        if(fileUri != null) {
                            onFileDropped(
                                UstadFilePickResult(
                                    uri = fileUri,
                                    fileName = fileUri.substringAfterLast("/")
                                )
                            )
                        }
                    }
                }
            )
    ) {
        content()
    }
}