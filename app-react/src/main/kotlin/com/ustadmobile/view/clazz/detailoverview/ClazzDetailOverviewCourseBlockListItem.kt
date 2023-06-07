package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadClazzAssignmentListItem
import com.ustadmobile.view.contententry.UstadContentEntryListItem
import com.ustadmobile.mui.ext.paddingCourseBlockIndent
import csstype.px
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import mui.icons.material.KeyboardArrowUp
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.Forum
import mui.icons.material.Folder
import mui.icons.material.Title


external interface ClazzDetailOverviewCourseBlockListItemProps : Props {

    var courseBlock: CourseBlockWithCompleteEntity?

    var onClickCourseBlock: (CourseBlock) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

val ClazzDetailOverviewCourseBlockListItem = FC<ClazzDetailOverviewCourseBlockListItemProps> { props ->
    val courseBlockVal = props.courseBlock

    when(courseBlockVal?.cbType ?: 0){
        CourseBlock.BLOCK_MODULE_TYPE  -> {

            val trailingIcon = if(courseBlockVal?.expanded == true)
                KeyboardArrowUp
            else
                KeyboardArrowDown

            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(courseBlockVal?.cbIndentLevel ?: 0)
                    }

                    onClick = { _ ->
                        courseBlockVal?.also { props.onClickCourseBlock(it) }
                    }

                    ListItemIcon {
                        Folder {
                            sx {
                                width = ICON_SIZE
                                height = ICON_SIZE
                            }
                        }
                    }

                    Box{
                        sx {
                            width = 10.px
                        }
                    }

                    ListItemText {
                        primary = ReactNode(courseBlockVal?.cbTitle ?: "")
                        secondary = ReactNode(courseBlockVal?.cbDescription ?: "")
                    }
                }

                secondaryAction = IconButton.create {
                    onClick = {_ ->
                        props.courseBlock?.also { props.onClickCourseBlock(it) }
                    }
                    + trailingIcon.create()
                }
            }
        }
        CourseBlock.BLOCK_DISCUSSION_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock?.cbIndentLevel ?: 0)
                    }

                    onClick = { _ ->
                        props.courseBlock?.also { props.onClickCourseBlock(it) }
                    }

                    ListItemIcon {
                        Forum {
                            sx {
                                width = ICON_SIZE
                                height = ICON_SIZE
                            }
                        }
                    }

                    Box{
                        sx {
                            width = 10.px
                        }
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock?.cbTitle ?: "")
                        secondary = ReactNode(
                            (props.courseBlock?.cbDescription ?: "").htmlToPlainText()
                        )
                    }
                }
            }
        }
        CourseBlock.BLOCK_TEXT_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(courseBlockVal?.cbIndentLevel ?: 0)
                    }

                    onClick = {_ ->
                        courseBlockVal?.also { props.onClickCourseBlock(it) }
                    }

                    ListItemIcon {
                        Title {
                            sx {
                                width = ICON_SIZE
                                height = ICON_SIZE
                            }
                        }
                    }

                    Box{
                        sx {
                            width = 10.px
                        }
                    }

                    ListItemText {
                        primary = ReactNode(props.courseBlock?.cbTitle ?: "")
//                        secondary = { Html(courseBlock.cbDescription) },
                    }
                }
            }
        }
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
            if(courseBlockVal != null) {
                UstadClazzAssignmentListItem {
                    courseBlock = courseBlockVal
                    onClickCourseBlock = props.onClickCourseBlock
                }
            }
        }
        CourseBlock.BLOCK_CONTENT_TYPE -> {
            courseBlockVal?.entry?.also { contentEntryItem ->
                UstadContentEntryListItem {
                    contentEntry = contentEntryItem
                    onClickContentEntry = props.onClickContentEntry
                    onClickDownloadContentEntry = props.onClickDownloadContentEntry
                }
            }

        }
    }
}
