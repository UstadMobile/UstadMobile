package com.ustadmobile.view.clazzedit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.lib.db.entities.CourseBlock
import mui.material.*
import mui.icons.material.Folder
import mui.icons.material.Article
import mui.icons.material.Collections
import mui.icons.material.Assignment
import mui.icons.material.Forum
import react.FC
import react.Props
import react.ReactNode


external interface AddCourseDialogProps: Props {
    var open: Boolean

    var onClose: ((event: dynamic, reason: String) -> Unit)?

    var onClickAddBlock: (Int) -> Unit
}


val AddCourseBlockDialog = FC<AddCourseDialogProps> { props ->
    val strings = useStringsXml()

    Dialog {
        open = props.open

        onClose = props.onClose

        List {
            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_MODULE_TYPE)
                    }
                    ListItemIcon {
                        Folder()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.module])
                        secondary = ReactNode(strings[MessageID.course_module])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_TEXT_TYPE)
                    }
                    ListItemIcon {
                        Article()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.text])
                        secondary = ReactNode(strings[MessageID.formatted_text_to_show_to_course_participants])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_CONTENT_TYPE)
                    }
                    ListItemIcon {
                        Collections()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.content])
                        secondary = ReactNode(strings[MessageID.add_course_block_content_desc])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_ASSIGNMENT_TYPE)
                    }
                    ListItemIcon {
                        Assignment()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.assignments])
                        secondary = ReactNode(strings[MessageID.add_assignment_block_content_desc])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        props.onClickAddBlock(CourseBlock.BLOCK_DISCUSSION_TYPE)
                    }
                    ListItemIcon {
                        Forum()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.discussion_board])
                        secondary = ReactNode(strings[MessageID.add_discussion_board_desc])
                    }
                }
            }
        }
    }
}
