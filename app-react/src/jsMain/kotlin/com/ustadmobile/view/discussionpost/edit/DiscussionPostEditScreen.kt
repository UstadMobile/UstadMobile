package com.ustadmobile.view.discussionpost.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.wrappers.quill.ReactQuill
import web.cssom.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useRequiredContext

external interface DiscussionPostEditProps: Props {
    var uiState: DiscussionPostEditUiState
    var onPostChanged: (DiscussionPost?) -> Unit
}

val DiscussionPostEditComponent2 = FC<DiscussionPostEditProps> { props ->

    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            TextField {
                value = props.uiState.discussionPost?.discussionPostTitle ?: ""
                id = "discussion_post_title"
                label = ReactNode(strings[MR.strings.title])
                error = props.uiState.discussionPostTitleError != null
                helperText = props.uiState.discussionPostTitleError?.let { ReactNode(it) }
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onPostChanged(
                        props.uiState.discussionPost?.shallowCopy {
                            discussionPostTitle = it
                        })
                }
            }

            ReactQuill {
                value = props.uiState.discussionPost?.discussionPostMessage ?: ""
                id = "discussion_post_message"
                onChange = {
                    props.onPostChanged(
                        props.uiState.discussionPost?.shallowCopy {
                            discussionPostMessage = it
                        }
                    )
                }
                readOnly = !props.uiState.fieldsEnabled
            }

            props.uiState.discussionPostDescError?.also { discussionPostError ->
                Typography {
                    variant = TypographyVariant.caption
                    sx {
                        color = theme.palette.error.main
                    }
                    + discussionPostError
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
                discussionPostMessage = "For our sales report, do I upload or share a link? "
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

