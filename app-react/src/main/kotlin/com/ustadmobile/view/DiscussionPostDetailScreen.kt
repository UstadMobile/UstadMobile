package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.DiscussionPostDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionPostWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.wrappers.quill.ReactQuill
import csstype.px
import mui.icons.material.Delete
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface DiscussionPostDetailProps: Props {
    var uiState: DiscussionPostDetailUiState2
    var onClickMessage: (DiscussionPostWithPerson) -> Unit
    var onDeleteMessage: (DiscussionPostWithPerson) -> Unit
    var onClickAddMessage: (String) -> Unit
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


            val newReply = DiscussionPost()


            ReactQuill {
                value = newReply?.discussionPostMessage ?: ""
                id = "add_a_reply"
                placeholder = strings[MessageID.add_a_reply]
                onChange = {
                    // TODO this
                }
            }


        }


        List {

            props.uiState.replies.forEach { item ->

                val thisAuthorName = item.replyPerson?.fullName() ?: ""
                ListItem {
                    disablePadding = true
                    secondaryAction = IconButton.create {
                        onClick = {
                            props.onDeleteMessage(item)
                        }
                        if(props.uiState.discussionPost?.discussionPostStartedPersonUid ==
                            item.replyPerson?.personUid) {
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
                            labelText = item.discussionPostMessage?:""
                            icon = mui.icons.material.Person.create()
                        }

                    }
                }
            }
        }
    }
}

val DiscussionPostDetailPreview = FC<Props> {
    DiscussionPostDetailComponent2 {
        uiState = DiscussionPostDetailUiState2(

            discussionPost = DiscussionPostWithDetails().apply {
                discussionPostTitle = "Submitting the assignment - help in formatting"
                discussionPostMessage = "Hello everyone, can anyone help me how to do this too"
                discussionPostStartedPersonUid = 1
                authorPersonFirstNames = "Mohammed"
                authorPersonLastName = "Iqbaal"

            },
            replies = listOf(
                DiscussionPostWithPerson().apply {

                    discussionPostMessage = "I have the same question"
                    replyPerson = Person().apply {
                        firstNames = "Chahid"
                        lastName = "Dabir"
                        personUid = 2

                    }
                },
                DiscussionPostWithPerson().apply {
                    discussionPostMessage = "I think it is briefly explained in section 42"
                    replyPerson = Person().apply {
                        firstNames = "Daanesh"
                        lastName = "Dabish"
                        personUid = 3

                    }
                },

                DiscussionPostWithPerson().apply {
                    discussionPostMessage = "Thanks everyone, I got it working now!"
                    replyPerson = Person().apply {
                        firstNames = "Mohammed"
                        lastName = "Iqbaal"
                        personUid = 1

                    }
                },
            ),


            )
    }

}



val DiscussionPostDetailScreen = FC<Props>{

    val viewModel = useUstadViewModel{ di, savedStateHandle ->
        DiscussionPostDetailViewModel(di, savedStateHandle)
    }

    val uiState: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsState(
        DiscussionPostDetailUiState2()
    )
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab{
        fabState = appState.fabState
    }

    DiscussionPostDetailComponent2{
        this.uiState = uiState
        onClickMessage = viewModel::onClickEntry
        onDeleteMessage = viewModel::onClickDeleteEntry
        onClickAddMessage = viewModel::addMessage


    }
}

