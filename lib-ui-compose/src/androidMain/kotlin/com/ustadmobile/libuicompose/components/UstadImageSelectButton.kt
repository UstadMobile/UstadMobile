package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File

@Composable
actual fun UstadImageSelectButton(
    imageUri: String?,
    onImageUriChanged: (String?) -> Unit,
    modifier: Modifier
) {
    val di = localDI()
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if(uri != null) {
            onImageUriChanged(uri.toString())
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if(uri != null) {
            onImageUriChanged(uri.toString())
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var cameraImgPath: String? by rememberSaveable {
        mutableStateOf(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        if(it)
            onImageUriChanged(cameraImgPath)
    }

    var dialogVisible: Boolean by remember {
        mutableStateOf(false)
    }

    Box {
        AsyncImage(
            model = imageUri,
            contentScale = ContentScale.Crop,
            //Size as per https://m3.material.io/components/icon-buttons/specs
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentDescription = null
        )

        val content: @Composable () -> Unit = {
            Icon(
                Icons.Default.AddAPhoto, contentDescription = stringResource(MR.strings.add)
            )
        }

        if(imageUri == null) {
            FilledTonalIconButton(
                onClick = { dialogVisible = true },
                content = content
            )
        }else {
            IconButton(
                onClick = { dialogVisible = true},
                content = content
            )
        }
    }

    if(dialogVisible) {
        Dialog(
            onDismissRequest = {
                dialogVisible = false
            }
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                ListItem(
                    modifier = Modifier.clickable {
                        dialogVisible = false
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    headlineContent = {
                        Text(stringResource(MR.strings.select_new_photo_from_gallery))
                    }
                )

                ListItem(
                    modifier = Modifier.clickable {
                        dialogVisible = false
                        fileLauncher.launch(arrayOf("image/*"))
                    },
                    headlineContent = {
                        Text(stringResource(MR.strings.select_file))
                    }
                )

                ListItem(
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            val tmpDir: File = di.direct.instance(tag = DiTag.TAG_TMP_DIR)
                            withContext(Dispatchers.IO) {
                                tmpDir.takeIf { !it.exists() }?.mkdirs()
                            }
                            dialogVisible = false
                            val cameraTmpFile = File(tmpDir, "camera-${systemTimeInMillis()}.tmp")
                            val cameraPathUri = FileProvider.getUriForFile(context, "${context.packageName}.provider",
                                cameraTmpFile)
                            cameraImgPath = cameraPathUri.toString()
                            cameraLauncher.launch(cameraPathUri)
                        }

                    },
                    headlineContent = {
                        Text(stringResource(MR.strings.take_new_photo_from_camera))
                    }
                )

                if(imageUri != null) {
                    ListItem(
                        modifier = Modifier.clickable {
                            dialogVisible = false
                            onImageUriChanged(null)
                        },
                        headlineContent = {
                            Text(stringResource(MR.strings.remove_photo))
                        }
                    )
                }
            }
        }
    }

}