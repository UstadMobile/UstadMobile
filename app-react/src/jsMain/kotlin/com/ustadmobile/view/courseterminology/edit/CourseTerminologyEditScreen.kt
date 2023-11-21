package com.ustadmobile.view.courseterminology.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditUiState
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface CourseTerminologyEditScreenProps : Props {

    var uiState: CourseTerminologyEditUiState

    var onTerminologyTermChanged: (TerminologyEntry) -> Unit

    var onTerminologyChanged: (CourseTerminology?) -> Unit

}

val CourseTerminologyEditScreenComponent2 = FC <CourseTerminologyEditScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(2)

            UstadTextField {
                id = "terms_title"
                value = props.uiState.entity?.ctTitle ?: ""
                label = ReactNode(strings[MR.strings.name_key])
                error = props.uiState.titleError != null
                helperText = props.uiState.titleError?.let { ReactNode(it) }
                disabled = !props.uiState.fieldsEnabled
                fullWidth = true
                onTextChange = {
                    props.onTerminologyChanged(props.uiState.entity?.shallowCopy {
                        ctTitle = it
                    })
                }
            }

            + strings[MR.strings.your_words_for]

            props.uiState.terminologyTermList.forEach { terminologyTerm ->
                UstadTextField {
                    fullWidth = true
                    value = terminologyTerm.term ?: ""
                    label = ReactNode(strings[terminologyTerm.stringResource])
                    error = terminologyTerm.errorMessage != null
                    disabled = !props.uiState.fieldsEnabled
                    id = terminologyTerm.id
                    onTextChange = {
                        props.onTerminologyTermChanged(
                            terminologyTerm.copy(
                                term = it
                            )
                        )
                    }
                }
            }
        }
    }
}

val CourseTerminologyEditScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseTerminologyEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CourseTerminologyEditUiState())

    CourseTerminologyEditScreenComponent2 {
        uiState = uiStateVal
        onTerminologyTermChanged = viewModel::onTerminologyTermChanged
        onTerminologyChanged = viewModel::onEntityChanged

    }
}
