package com.ustadmobile.view.clazz.joinwithcode

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeUiState
import com.ustadmobile.core.viewmodel.clazz.joinwithcode.JoinWithCodeViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import kotlinx.coroutines.Dispatchers
import web.cssom.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface JoinWithCodeScreenProps : Props {

    var uiState: JoinWithCodeUiState

    var onCodeValueChange: (String) -> Unit

    var onClickDone: () -> Unit

}


val JoinWithCodeScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        JoinWithCodeViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(
        JoinWithCodeUiState(), Dispatchers.Main.immediate,
    )

    JoinWithCodeScreenComponent2 {
        uiState = uiStateVal
        onCodeValueChange = viewModel::onCodeValueChange
        onClickDone = viewModel::onClickJoin
    }
}

private val JoinWithCodeScreenComponent2 = FC<JoinWithCodeScreenProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            Typography {
                + strings[MR.strings.join_code_instructions]
            }

            TextField {
                value = props.uiState.code
                label = ReactNode(strings[MR.strings.course_code] + "*")
                id = "course_code"
                error = props.uiState.codeError != null
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onCodeValueChange(it)
                }
                helperText = ReactNode(
                    props.uiState.codeError ?: strings[MR.strings.required]
                )
            }

            Button {
                id = "submit_button"
                onClick = { props.onClickDone() }
                variant = ButtonVariant.contained
                disabled = !props.uiState.fieldsEnabled
                + strings[MR.strings.submit]
            }
        }
    }
}