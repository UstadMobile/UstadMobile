package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionDetailUiState
import com.ustadmobile.core.viewmodel.SiteEditUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadCourseBlockEdit
import com.ustadmobile.mui.components.UstadDateTimeEditField
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadSwitchField
import csstype.px
import kotlinx.html.currentTimeMillis
import mui.icons.material.AccountCircle
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.icons.material.Visibility
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create

external interface CourseDiscussionDetailProps: Props {
    var uiState: CourseDiscussionDetailUiState
    var onClickPost: (DiscussionPostWithDetails) -> Unit
    var onDeleteClick: (DiscussionPostWithDetails) -> Unit
    var onClickAddItem: () -> Unit
}

val CourseDiscussionDetailComponent2 = FC<CourseDiscussionDetailProps> { props ->

    val strings = useStringsXml()

    Container {

        Typography {
            + props.uiState.courseDiscussion?.courseDiscussionTitle.toString()
        }

        Box{
            sx {
                height = 10.px
            }
        }

        Typography {
            variant = TypographyVariant.caption
            + strings[MessageID.description]
        }

        Typography {
            + props.uiState.courseDiscussion?.courseDiscussionDesc.toString()
        }


        maxWidth = "lg"

        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

//            UstadDetailField {
//                valueText = props.uiState.courseDiscussion?.courseDiscussionTitle.toString()
//                labelText = strings[MessageID.title]
//
//            }

//            UstadDetailField {
//                valueText = props.uiState.courseDiscussion?.courseDiscussionDesc.toString()
//                labelText = strings[MessageID.description]
//
//
//            }



        }

        Box{
            sx {
                height = 10.px
            }
        }

        Typography {
            variant = TypographyVariant.h6
            + strings[MessageID.posts]
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

val CourseDiscussionDetailPreview = FC<Props> {
    CourseDiscussionDetailComponent2 {
        uiState = CourseDiscussionDetailUiState(

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

                },
                DiscussionPostWithDetails().apply {
                    discussionPostTitle = "Introductions"
                    discussionPostMessage = "I am your supervisor for this module. Ask me anything."
                    discussionPostVisible = true
                    authorPersonFirstNames = "Bilal"
                    authorPersonLastName = "Zaik"
                    postRepliesCount = 16
                    postLatestMessageTimestamp = currentTimeMillis()

                }
            ),


        )
    }
}
