package com.ustadmobile.libuicompose.view.clazz

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.libuicompose.components.UstadAsyncImage

@Composable
fun CourseImage(
    coursePicture: CoursePicture?,
    clazzName: String?,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier,
) {
    val imageUri = coursePicture?.coursePictureUri
    if(imageUri != null) {
        UstadAsyncImage(
            uri = imageUri,
            contentDescription = "",
            contentScale = contentScale,
            modifier = modifier,
        )
    }else {
        Image(
            painter = painterForDefaultCourseImage(clazzName),
            contentDescription = "",
            contentScale = contentScale,
            modifier = modifier
        )
    }
}