package com.ustadmobile.libuicompose.view.clazz.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.FolderOpen
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.view.contententry.list.ClazzAssignmentConstants.CONTENT_ENTRY_TYPE_ICON_MAP

object ClazzEditConstants {

    val BLOCK_ICON_MAP = mapOf(
        CourseBlock.BLOCK_MODULE_TYPE to Icons.Outlined.FolderOpen,
        CourseBlock.BLOCK_ASSIGNMENT_TYPE to Icons.Filled.AssignmentTurnedIn,
        CourseBlock.BLOCK_CONTENT_TYPE to Icons.Filled.SmartDisplay,
        CourseBlock.BLOCK_TEXT_TYPE to Icons.Filled.Title,
        CourseBlock.BLOCK_DISCUSSION_TYPE to Icons.Filled.Forum
    )

    val BLOCK_AND_ENTRY_ICON_MAP = BLOCK_ICON_MAP + CONTENT_ENTRY_TYPE_ICON_MAP
}
