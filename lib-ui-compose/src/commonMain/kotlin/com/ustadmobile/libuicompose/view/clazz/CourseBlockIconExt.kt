package com.ustadmobile.libuicompose.view.clazz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Title
import androidx.compose.ui.graphics.vector.ImageVector
import com.ustadmobile.lib.db.entities.CourseBlock

fun imageVectorForCourseBlockType(
    blockType: Int
) : ImageVector? {
    return when(blockType) {
        CourseBlock.BLOCK_MODULE_TYPE -> Icons.Default.Folder
        CourseBlock.BLOCK_TEXT_TYPE -> Icons.Default.Title
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> Icons.AutoMirrored.Filled.Assignment
        CourseBlock.BLOCK_CONTENT_TYPE -> Icons.Default.Collections
        CourseBlock.BLOCK_DISCUSSION_TYPE -> Icons.Default.Forum
        else -> null
    }
}

val CourseBlock.blockTypeImageVector: ImageVector?
    get() = imageVectorForCourseBlockType(cbType)
