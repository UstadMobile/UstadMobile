package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseDiscussionBlockEditUiState
import com.ustadmobile.core.viewmodel.DiscussionPostDetailUiState
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
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.icons.material.LocationOn
import mui.icons.material.SupervisedUserCircle
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface DiscussionPostDetailProps: Props {
    var uiState: DiscussionPostDetailUiState
    var onClickMessage: (MessageWithPerson) -> Unit
    var onDeleteMessage: (MessageWithPerson) -> Unit
    var onClickAddMessage: (MessageWithPerson) -> Unit
}

val DiscussionPostDetailComponent2 = FC<DiscussionPostDetailProps> { props ->

    val strings = useStringsXml()



    Container {
        maxWidth = "lg"

        val authorFullName = props.uiState.discussionPost?.authorPersonFirstNames + " " +
                props.uiState.discussionPost?.authorPersonLastName



        Typography {
            + props.uiState.discussionPost?.discussionPostTitle.toString()
        }

        Box{
            sx {
                height = 10.px
            }
        }

        UstadDetailField {
            valueText = ReactNode(authorFullName?:"")

            labelText = props.uiState.discussionPost?.discussionPostMessage?:""
            //icon = SupervisedUserCircle.create()
            icon = mui.icons.material.Person.create()
        }



        Box{
            sx {
                height = 10.px
            }
        }


        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            val newMessage = Message()
            val newMessageError: String? = null
            UstadTextEditField {
                value = newMessage.messageText
                label = strings[MessageID.add_a_reply]
                error = newMessageError
                enabled = true
                onChange = {
                    //TODO
                }
            }


        }





        List {

            props.uiState.replies.forEach { item ->

                val thisAuthorName = item.messagePerson?.fullName() ?: ""
                ListItem {
                    disablePadding = true
                    secondaryAction = IconButton.create {
                        onClick = {
                            props.onDeleteMessage(item)
                        }
                        if(props.uiState.discussionPost?.discussionPostStartedPersonUid ==
                            item.messagePerson?.personUid) {
                            Delete {}
                        }
                    }

                    ListItemButton {
                        ListItemIcon {
                            UstadBlankIcon { }
                        }

                        onClick = {
                            props.onClickMessage(item)
                        }


                        UstadDetailField {
                            valueText = ReactNode(thisAuthorName ?: "")
                            labelText = item.messageText?:""
                            //icon = SupervisedUserCircle.create()
                            icon = mui.icons.material.Person.create()
                        }

//                        ListItemText {
//                            +(item.messageText ?: "")
//                        }
                    }
                }
            }
        }
    }
}

val DiscussionPostDetailPreview = FC<Props> {
    DiscussionPostDetailComponent2 {
        uiState = DiscussionPostDetailUiState(

            discussionPost = DiscussionPostWithDetails().apply {
                discussionPostTitle = "Submitting the assignment - help in formatting"
                discussionPostMessage = "Hello everyone, can anyone help me how to do this too"
                discussionPostStartedPersonUid = 1
                authorPersonFirstNames = "Mohammed"
                authorPersonLastName = "Iqbaal"

            },
            replies = listOf(
                MessageWithPerson().apply {
                    messageText = "I have the same question"
                    messagePerson = Person().apply {
                        firstNames = "Chahid"
                        lastName = "Dabir"
                        personUid = 2

                    }
                },
                MessageWithPerson().apply {
                    messageText = "I think it is briefly explained in section 42"
                    messagePerson = Person().apply {
                        firstNames = "Daanesh"
                        lastName = "Dabish"
                        personUid = 3

                    }
                },

                MessageWithPerson().apply {
                    messageText = "Thanks everyone, I got it working!"
                    messagePerson = Person().apply {
                        firstNames = "Mohammed"
                        lastName = "Iqbaal"
                        personUid = 1

                    }
                },
            ),


            )
    }
}
