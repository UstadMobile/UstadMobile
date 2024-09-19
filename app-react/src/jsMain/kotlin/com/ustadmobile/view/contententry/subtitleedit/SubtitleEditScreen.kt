package com.ustadmobile.view.contententry.subtitleedit

import com.ustadmobile.core.MR
import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditUiState
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import kotlinx.coroutines.Dispatchers
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface SubtitleEditProps: Props {
    var uiState: SubtitleEditUiState
    var onEntityChanged: (SubtitleTrack?) -> Unit
}

val SubtitleEditComponent = FC<SubtitleEditProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(2)

            TextField {
                id = "subtitle_title"
                label = ReactNode(strings[MR.strings.title])
                value = props.uiState.entity?.title ?: ""
                helperText = ReactNode(props.uiState.titleError ?: strings[MR.strings.required])
                disabled = !props.uiState.fieldsEnabled
                error = props.uiState.titleError != null
                onTextChange = {
                    props.onEntityChanged(
                        props.uiState.entity?.copy(title = it)
                    )
                }
            }

            TextField {
                id = "langcode"
                label = ReactNode(strings[MR.strings.language])
                value = props.uiState.entity?.langCode ?: ""
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onEntityChanged(
                        props.uiState.entity?.copy(langCode = it)
                    )
                }
            }
        }
    }
}

val SubtitleEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SubtitleEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(SubtitleEditUiState(), Dispatchers.Main.immediate)

    SubtitleEditComponent {
        uiState = uiStateVal
        onEntityChanged = viewModel::onEntityChanged
    }
}


