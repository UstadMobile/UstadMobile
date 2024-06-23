package com.ustadmobile.libuicompose.view.clazz.gradebook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.components.UstadBlockIcon

/**
 * Make a box where text will appear vertically aligned to the bottom of the box.
 *
 * Technique: Create a box with one centered item. The centered item must use the entire space.
 */
@Composable
fun GradebookCourseBlockHeader(
    courseBlock: CourseBlock,
    width: Dp,
    height: Dp,
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
            ListItem(
                headlineContent = { Text(courseBlock.cbTitle ?: "") },
                leadingContent = {
                    UstadBlockIcon(
                        title = courseBlock.cbTitle ?: "",
                        courseBlock = courseBlock,
                        contentEntry = null,
                        pictureUri = null,
                    )
                }
            )
        }
    }
}