package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import java.io.File
import java.net.URI

//Kamel doesn't like using a URI that is not http/https.
@Composable
fun asyncPainterForUri(
    uri: URI
): Resource<Painter> {
    return if(uri.scheme == "file") {
        asyncPainterResource(File(uri))
    }else {
        asyncPainterResource(Url(uri.toString()))
    }
}

//See
// https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Image_And_Icons_Manipulations/README.md
@Composable
actual fun UstadImageSelectButton(
    imageUri: String?,
    onImageUriChanged: (String?) -> Unit,
    modifier: Modifier
) {
    val imageUriObj = remember(imageUri) {
        imageUri?.let { URI(it) }
    }

    var showFilePicker by remember {
        mutableStateOf(false)
    }

    FilePicker(
        show = showFilePicker,
        fileExtensions = listOf("jpg", "png", "webp")
    ) {
        showFilePicker = false
        if(it != null) {
            onImageUriChanged(File(it.path).toURI().toString())
        }
    }

    Box {
        if(imageUriObj != null) {
            KamelImage(
                resource = asyncPainterForUri(imageUriObj),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape),
            )
        }

        val content: @Composable () -> Unit = {
            Icon(
                Icons.Default.AddAPhoto, contentDescription = stringResource(MR.strings.add)
            )
        }

        if(imageUri == null) {
            FilledTonalIconButton(
                onClick = { showFilePicker = true },
                content = content
            )
        }else {
            IconButton(
                onClick = { showFilePicker = true },
                content = content
            )
        }
    }
}