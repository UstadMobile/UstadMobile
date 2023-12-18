package com.ustadmobile.libuicompose.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
actual fun UstadImageSelectButton(
    imageUri: String?,
    onImageUriChanged: (String?) -> Unit,
    modifier: Modifier
) {

    IconButton(onClick = {}){
        Icon(
            Icons.Default.AddAPhoto,
            contentDescription = stringResource(MR.strings.add)
        )
    }

}