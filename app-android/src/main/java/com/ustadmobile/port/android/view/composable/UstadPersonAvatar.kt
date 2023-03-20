package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.ustadmobile.port.android.util.compose.collectDbAttachmentUriFlow
import kotlinx.coroutines.flow.map

@Composable
fun UstadPersonAvatar(
    personUid: Long,
    modifier: Modifier = Modifier,
) {
    val personPictureUri = collectDbAttachmentUriFlow(personUid) { db ->
        db.personPictureDao.findByPersonUidAsFlow(personUid).map { it?.personPictureUri }
    }

    if(personPictureUri != null) {
        SubcomposeAsyncImage(
            model = personPictureUri,
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
