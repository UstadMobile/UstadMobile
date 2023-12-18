package com.ustadmobile.view.person.accountedit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditUiState
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonUsernameAndPasswordModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadPasswordTextField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.useState

external interface PersonAccountEditScreenProps : Props {

    var uiState: PersonAccountEditUiState

    var onAccountChanged: (PersonUsernameAndPasswordModel?) -> Unit

}

val PersonAccountEditComponent2 = FC<PersonAccountEditScreenProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(3)

            if (props.uiState.usernameVisible){
                UstadTextField {
                    id = "username"
                    value = props.uiState.personAccount?.username ?: ""
                    label = ReactNode(strings[MR.strings.username] + "*")
                    helperText = ReactNode(props.uiState.usernameError ?: strings[MR.strings.required])
                    error = props.uiState.usernameError != null
                    disabled = !props.uiState.fieldsEnabled
                    onTextChange = {
                        props.onAccountChanged(
                            props.uiState.personAccount?.copy(
                                username = it
                            )
                        )
                    }
                }
            }

            if (props.uiState.currentPasswordVisible){
                UstadPasswordTextField {
                    id = "currentpassword"
                    value = props.uiState.personAccount?.currentPassword ?: ""
                    label = ReactNode(strings[MR.strings.current_password] + "*")
                    helperText = ReactNode(props.uiState.currentPasswordError ?: strings[MR.strings.required])
                    disabled = !props.uiState.fieldsEnabled
                    onTextChange = {
                        props.onAccountChanged(
                            props.uiState.personAccount?.copy(
                                currentPassword = it
                            )
                        )
                    }
                }
            }

            UstadPasswordTextField {
                id = "newpassword"
                value = props.uiState.personAccount?.newPassword
                label = ReactNode(strings[MR.strings.new_password] + "*")
                error = props.uiState.newPasswordError != null
                helperText = ReactNode(props.uiState.newPasswordError ?: strings[MR.strings.required])
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onAccountChanged(
                        props.uiState.personAccount?.copy(
                            newPassword = it
                        )
                    )
                }
            }

        }
    }
}

val PersonAccountEditPreview = FC<Props> {

    val uiStateVar : PersonAccountEditUiState by useState {
        PersonAccountEditUiState(
            personAccount = PersonUsernameAndPasswordModel(
                username = "",
                currentPassword = null,
            )
        )
    }

    PersonAccountEditComponent2 {
        uiState = uiStateVar
        onAccountChanged = {

        }
    }
}

val PersonAccountEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PersonAccountEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(PersonAccountEditUiState())
    PersonAccountEditComponent2 {
        uiState = uiStateVar
        onAccountChanged = viewModel::onEntityChanged
    }
}
