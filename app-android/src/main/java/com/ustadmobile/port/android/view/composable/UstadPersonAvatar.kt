package com.ustadmobile.port.android.view.composable

import android.net.Uri
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.ustadmobile.port.android.util.compose.collectAttachmentUri
import com.ustadmobile.port.android.util.compose.rememberActiveDatabase


@Composable
fun UstadPersonAvatar(
    personUid: Long,
    modifier: Modifier = Modifier,
) {
    val db = rememberActiveDatabase()
    val personPicFlow = remember(personUid, db) {
        db?.personPictureDao?.findByPersonUidAsFlow(personUid)
    }

    val attachmentUri: Uri? by personPicFlow.collectAttachmentUri(db) {
        it?.personPictureUri
    }

    if(attachmentUri != null) {
        SubcomposeAsyncImage(
            model = attachmentUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    }else {
        Icon(
            modifier = modifier,
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null
        )
    }

}
