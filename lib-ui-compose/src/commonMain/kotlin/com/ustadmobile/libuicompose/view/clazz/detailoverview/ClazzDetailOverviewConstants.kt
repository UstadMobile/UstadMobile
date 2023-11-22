package com.ustadmobile.libuicompose.view.clazz.detailoverview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Title
import com.ustadmobile.lib.db.entities.CourseBlock

object ClazzDetailOverviewConstants {

    val BLOCK_ICON_MAP = mapOf(
        CourseBlock.BLOCK_MODULE_TYPE to Icons.Filled.FolderOpen,
        CourseBlock.BLOCK_ASSIGNMENT_TYPE to Icons.Filled.AssignmentTurnedIn,
        CourseBlock.BLOCK_CONTENT_TYPE to Icons.Filled.SmartDisplay,
        CourseBlock.BLOCK_TEXT_TYPE to Icons.Filled.Title,
        CourseBlock.BLOCK_DISCUSSION_TYPE to Icons.Filled.Forum
    )

}