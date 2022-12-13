package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionBlockEditUiState
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadDateTimeEditField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import kotlinx.html.currentTimeMillis
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.create

external interface CourseDiscussionBlockEditProps: Props {
    var uiState: CourseDiscussionBlockEditUiState
    var onSiteChanged: (Site?) -> Unit
    var onClickPost: (DiscussionPostWithDetails) -> Unit
    var onDeleteClick: (DiscussionPostWithDetails) -> Unit
    var onClickAddItem: () -> Unit
    var onCourseBlockChange: (CourseBlock?) -> Unit
}

val CourseDiscussionBlockEditComponent2 = FC<CourseDiscussionBlockEditProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.courseDiscussion?.courseDiscussionTitle ?: ""
                label = strings[MessageID.title]
                error = props.uiState.courseDiscussionTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    //TODO
                }
            }

            UstadTextEditField {
                value = props.uiState.courseDiscussion?.courseDiscussionDesc ?: ""
                label = strings[MessageID.description]
                error = props.uiState.courseDiscussionDescError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    //TODO
                }
            }

            UstadCourseBlockEdit {
                uiState = props.uiState.courseBlockEditUiState
                onCourseBlockChange = props.onCourseBlockChange
            }


            /*
                onChange = {
                    props.onSiteChanged(
                        props.uiState.site?.shallowCopy {
                            siteName = it
                        })
                }

                 */
        }



        Typography {
            variant = TypographyVariant.h6
            +strings[MessageID.posts
            ]
        }

        List {
            ListItem {
                disablePadding = true

                ListItemButton {
                    onClick = {
                        props.onClickAddItem()
                    }

                    ListItemIcon {
                        Add {}
                    }

                    ListItemText {
                        +(strings[MessageID.posts])
                    }
                }
            }

            props.uiState.posts.forEach { item ->
                ListItem {
                    disablePadding = true
                    secondaryAction = IconButton.create {
                        onClick = {
                            props.onDeleteClick(item)
                        }
                        Delete {}
                    }

                    ListItemButton {
                        ListItemIcon {
                            UstadBlankIcon { }
                        }

                        onClick = {
                            props.onClickPost(item)
                        }
                        ListItemText {
                            +(item.discussionPostTitle ?: "")
                        }
                    }
                }
            }
        }
    }
}

val CourseDiscussionBlockEditPreview = FC<Props> {
    CourseDiscussionBlockEditComponent2 {
        uiState = CourseDiscussionBlockEditUiState(

            courseDiscussion = CourseDiscussion().apply {
                courseDiscussionTitle = "Sales and Marketting Discussion"
                courseDiscussionDesc =
                    "This discussion group is for conversations and posts about Sales and Marketting course"
                courseDiscussionActive = true
            },
            posts = listOf(
                DiscussionPostWithDetails().apply {
                    discussionPostTitle = "Question about Homework A4"
                    discussionPostMessage = "How is marketting different from sales?"
                    discussionPostVisible = true
                    authorPersonFirstNames = "Ahmed"
                    authorPersonLastName = "Ismail"
                    postRepliesCount = 5
                    postLatestMessageTimestamp = currentTimeMillis()

                }
            ),
            courseBlockEditUiState = CourseBlockEditUiState(
                courseBlock = CourseBlock().apply {
                    cbMaxPoints = 0
                    cbCompletionCriteria = 0
                },
                gracePeriodVisible = true,
            ),

        )
    }
}
