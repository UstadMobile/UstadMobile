package com.ustadmobile.view.clazzlog.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditUiState
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadDateTimeField
import com.ustadmobile.mui.components.UstadStandardContainer
import web.cssom.px
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface ClazzLogEditScreenProps : Props {

    var uiState: ClazzLogEditUiState

    var onChangeClazzLog: (ClazzLog?) -> Unit

}


private val ClazzLogEditScreenComponent2 = FC<ClazzLogEditScreenProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            UstadDateTimeField {
                timeInMillis = props.uiState.clazzLog?.logDate ?: 0
                label = ReactNode(strings[MR.strings.date] + "*")
                disabled = !props.uiState.fieldsEnabled
                helperText = ReactNode(props.uiState.dateError ?: strings[MR.strings.required])
                error = props.uiState.dateError != null
                timeZoneId = props.uiState.timeZone
                onChange = {
                    props.onChangeClazzLog(props.uiState.clazzLog?.shallowCopy {
                        logDate = it
                    })
                }
            }
        }
    }
}

val ClazzLogEditScreenPreview = FC<Props> {


    var clazzLogUiState by useState {
        ClazzLogEditUiState(clazzLog = ClazzLog())
    }

    ClazzLogEditScreenComponent2 {
        uiState = clazzLogUiState
        onChangeClazzLog = {
            clazzLogUiState = clazzLogUiState.copy(
                clazzLog = it
            )
        }
    }
}

val ClazzLogEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzLogEditViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(ClazzLogEditUiState())

    ClazzLogEditScreenComponent2 {
        uiState = uiStateVal
        onChangeClazzLog = viewModel::onEntityChanged
    }
}

