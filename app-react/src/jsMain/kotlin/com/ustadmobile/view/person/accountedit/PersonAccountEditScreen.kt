package com.ustadmobile.view.person.accountedit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditUiState
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.accountedit.PersonUsernameAndPasswordModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadTextEditField
import mui.material.Container
import mui.material.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface PersonAccountEditScreenProps : Props {

    var uiState: PersonAccountEditUiState

    var onAccountChanged: (PersonUsernameAndPasswordModel?) -> Unit

}

val PersonAccountEditComponent2 = FC<PersonAccountEditScreenProps> { props ->

    val strings = useStringProvider()

    Container {
        Stack {
            spacing = responsive(3)

            if (props.uiState.usernameVisible){
                UstadTextEditField {
                    id = "username"
                    value = props.uiState.personAccount?.username ?: ""
                    label = strings[MR.strings.username]
                    error = props.uiState.usernameError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onAccountChanged(
                            props.uiState.personAccount?.copy(
                                username = it
                            )
                        )
                    }
                }
            }

            if (props.uiState.currentPasswordVisible){
                UstadTextEditField {
                    id = "currentpassword"
                    value = props.uiState.personAccount?.currentPassword ?: ""
                    label = strings[MR.strings.current_password]
                    error = props.uiState.currentPasswordError
                    enabled = props.uiState.fieldsEnabled
                    password = true
                    onChange = {
                        props.onAccountChanged(
                            props.uiState.personAccount?.copy(
                                currentPassword = it
                            )
                        )
                    }
                }
            }

            UstadTextEditField {
                id = "newpassword"
                value = props.uiState.personAccount?.newPassword
                label = strings[MR.strings.new_password]
                error = props.uiState.newPasswordError
                enabled = props.uiState.fieldsEnabled
                password = true
                onChange = {
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

    var uiStateVar : PersonAccountEditUiState by useState {
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

    var uiStateVar by viewModel.uiState.collectAsState(PersonAccountEditUiState())
    PersonAccountEditComponent2 {
        uiState = uiStateVar
        onAccountChanged = viewModel::onEntityChanged
    }
}
