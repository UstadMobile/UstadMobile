package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.DiscussionPostEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.wrappers.quill.ReactQuill
import csstype.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props

external interface DiscussionPostEditProps: Props {
    var uiState: DiscussionPostEditUiState
    var onPostChanged: (DiscussionPost?) -> Unit
}

val DiscussionPostEditComponent2 = FC<DiscussionPostEditProps> { props ->

    val strings = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

            UstadTextEditField {
                value = props.uiState.discussionPost?.discussionPostTitle ?: ""
                label = strings[MessageID.title]
                error = props.uiState.discussionPostTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onPostChanged(
                        props.uiState.discussionPost?.shallowCopy {
                            discussionPostTitle = it
                        })
                }
            }

            ReactQuill {
                value = props.uiState.discussionPost?.discussionPostMessage ?: ""
                id = "add_a_new_post_message"
                placeholder = strings[MessageID.message]
                onChange = {
                    props.onPostChanged(
                        props.uiState.discussionPost?.shallowCopy {
                            discussionPostMessage = it
                        })
                }
            }

        }

    }
}


val DiscussionPostEditPreview = FC<Props> {
    DiscussionPostEditComponent2 {
        uiState = DiscussionPostEditUiState(

            discussionPost = DiscussionPost().apply {
                discussionPostTitle = "How to submit report A?"
                discussionPostMessage =
                    "For our sales report, do I upload or share a link? "
                discussionPostVisible = true

            },




            )
    }
}

val DiscussionPostEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        DiscussionPostEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(DiscussionPostEditUiState())

    DiscussionPostEditComponent2{
        uiState = uiStateVar
        onPostChanged = viewModel::onEntityChanged
    }
}

