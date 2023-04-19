package com.ustadmobile.view.clazzdetailoverview

import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadClazzAssignmentListItem
import com.ustadmobile.mui.components.UstadContentEntryListItem
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

    var courseBlock: CourseBlockWithCompleteEntity

    var onClickCourseDiscussion: (CourseDiscussion?) -> Unit

    var onClickCourseExpandCollapse: (CourseBlockWithCompleteEntity) -> Unit

    var onClickTextBlock: (CourseBlockWithCompleteEntity) -> Unit

    var onClickAssignment: (ClazzAssignmentWithMetrics?) -> Unit

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

val ClazzDetailOverviewCourseBlockListItem = FC<ClazzDetailOverviewCourseBlockListItemProps> { props ->

    when(props.courseBlock.cbType){
        CourseBlock.BLOCK_MODULE_TYPE  -> {

            val trailingIcon = if(props.courseBlock.expanded)
                KeyboardArrowUp
            else
                KeyboardArrowDown

            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                    onClick = {
                        props.onClickCourseExpandCollapse(props.courseBlock)
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
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
                        secondary = ReactNode(props.courseBlock.cbDescription ?: "")
                    }
                }

                secondaryAction = IconButton.create {
                    onClick = {
                        props.onClickCourseExpandCollapse(props.courseBlock)
                    }
                    + trailingIcon.create()
                }
            }
        }
        CourseBlock.BLOCK_DISCUSSION_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                    onClick = {
                        props.onClickCourseDiscussion(props.courseBlock.courseDiscussion)
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
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
                        secondary = ReactNode(props.courseBlock.cbDescription ?: "")
                    }
                }
            }
        }
        CourseBlock.BLOCK_TEXT_TYPE -> {
            ListItem {
                ListItemButton {

                    sx {
                        padding = paddingCourseBlockIndent(props.courseBlock.cbIndentLevel)
                    }

                    onClick = {
                        props.onClickTextBlock(props.courseBlock)
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
                        primary = ReactNode(props.courseBlock.cbTitle ?: "")
//                        secondary = { Html(courseBlock.cbDescription) },
                    }
                }
            }
        }
        CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
            UstadClazzAssignmentListItem {
                courseBlock = props.courseBlock
                onClickAssignment = props.onClickAssignment
            }
        }
        CourseBlock.BLOCK_CONTENT_TYPE -> {
            UstadContentEntryListItem {
                contentEntry = props.courseBlock.entry
                    ?: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
                onClickContentEntry = props.onClickContentEntry
                onClickDownloadContentEntry = props.onClickDownloadContentEntry
            }
        }
    }
}
