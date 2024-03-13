package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libcache.headers.MimeTypeHelper
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import java.net.URI
import kotlin.io.path.toPath

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

    val di = localDI()
    val mimeTypeHelper: MimeTypeHelper by di.instance()

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
                            val uriObj = URI(fileUri)
                            val fileObj = uriObj.toPath().toFile()

                            onFileDropped(
                                UstadFilePickResult(
                                    uri = fileUri,
                                    fileName = fileUri.substringAfterLast("/"),
                                    size = fileObj.length(),
                                    mimeType = mimeTypeHelper.mimeTypeByUri(fileUri),
                                )
                            )
                        }
                    }
                }
            )
    ) {
        content()
        if(isDragging) {
            Box(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    Text(
                        text= stringResource(MR.strings.drop_files_to_import),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

            }
        }
    }
}