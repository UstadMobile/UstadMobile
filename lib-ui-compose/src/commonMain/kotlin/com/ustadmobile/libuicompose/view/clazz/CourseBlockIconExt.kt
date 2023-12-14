package com.ustadmobile.libuicompose.view.clazz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.CourseBlock

@Composable
fun CourseBlock.iconContent() {
    val icon = when(cbType) {
        CourseBlock.BLOCK_MODULE_TYPE -> Icons.Default.Folder
        CourseBlock.BLOCK_TEXT_TYPE -> Icons.Default.Article
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> Icons.Default.Assignment
        CourseBlock.BLOCK_CONTENT_TYPE -> Icons.Default.Collections
        CourseBlock.BLOCK_DISCUSSION_TYPE -> Icons.Default.Forum
        else -> null
    }
    if(icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }
}
