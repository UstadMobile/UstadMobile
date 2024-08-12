package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture

@Composable
fun UstadCourseBlockHeader(
    block: CourseBlock?,
    picture: CourseBlockPicture?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        UstadBlockIcon(
            title = block?.cbTitle ?: "",
            courseBlock = block,
            pictureUri = picture?.cbpPictureUri,
            modifier = Modifier.size(80.dp),
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = block?.cbTitle ?: "",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
        )
    }
}
