package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.clazz.gradebook.thumbnailUri
import com.ustadmobile.lib.db.composites.CourseBlockAndGradebookDisplayDetails
import com.ustadmobile.libuicompose.components.ScaledListItem
import com.ustadmobile.libuicompose.components.UstadBlockIcon
import com.ustadmobile.libuicompose.components.scaledTextStyle

/**
 * Make a box where text will appear vertically aligned to the bottom of the box.
 *
 * Technique: Create a box with one centered item. The centered item must use the entire space.
 */
@Composable
fun GradebookCourseBlockHeader(
    courseBlock: CourseBlockAndGradebookDisplayDetails,
    width: Dp,
    height: Dp,
    scale: Float,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        Column(
            modifier = Modifier
                //Required, otherwise the width will be constrainted by the parent, which is
                //incorrect because it will be rotated
                .wrapContentWidth(unbounded = true)
                .width(height)
                .height(width)
                .rotate(-90f)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            ScaledListItem(
                headlineContent = {
                    Text(
                        text = courseBlock.block?.cbTitle ?: "",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = scaledTextStyle(scale),
                    )
                },
                leadingContent = {
                    UstadBlockIcon(
                        title = courseBlock.block?.cbTitle ?: "",
                        courseBlock = courseBlock.block,
                        contentEntry = courseBlock.contentEntry,
                        pictureUri = courseBlock.thumbnailUri,
                        modifier = Modifier.size(40.dp * scale)
                    )
                },
                scale =  scale,
            )
        }
    }
}