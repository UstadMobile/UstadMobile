package com.ustadmobile.view.clazz

import com.ustadmobile.lib.db.entities.CourseBlock
import mui.icons.material.AssignmentTurnedIn as AssignmentTurnedInIcon
import mui.icons.material.Book as BookIcon
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Forum as ForumIcon
import mui.icons.material.SvgIconComponent
import mui.icons.material.Title as TitleIcon

fun CourseBlock.iconComponent(): SvgIconComponent? {
    return when(this.cbType) {
        CourseBlock.BLOCK_MODULE_TYPE -> FolderIcon
        CourseBlock.BLOCK_DISCUSSION_TYPE -> ForumIcon
        CourseBlock.BLOCK_TEXT_TYPE -> TitleIcon
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> AssignmentTurnedInIcon
        CourseBlock.BLOCK_CONTENT_TYPE -> BookIcon
        else -> null
    }
}