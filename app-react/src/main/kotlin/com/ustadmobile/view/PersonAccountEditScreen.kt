package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.PersonAccountEditUiState
import com.ustadmobile.core.viewmodel.PersonUsernameAndPasswordModel
import com.ustadmobile.mui.components.UstadTextEditField
import mui.material.Container
import mui.material.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.useState

external interface PersonAccountEditScreenProps : Props {

    var uiState: PersonAccountEditUiState

    var onUsernameAndPasswordChanged: (PersonUsernameAndPasswordModel) -> Unit

}

val PersonAccountEditComponent2 = FC<PersonAccountEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        Stack {
            spacing = responsive(3)

            if (props.uiState.usernameVisible){
                UstadTextEditField {
                    value = props.uiState.personUsernameAndPassword.username
                    label = strings[MessageID.username]
                    error = props.uiState.usernameError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onUsernameAndPasswordChanged(
                            props.uiState.personUsernameAndPassword.copy(
                                username = it
                            )
                        )
                    }
                }
            }

            if (props.uiState.currentPasswordVisible){
                UstadTextEditField {
                    value = props.uiState.personUsernameAndPassword.currentPassword
                    label = strings[MessageID.current_password]
                    error = props.uiState.currentPasswordError
                    enabled = props.uiState.fieldsEnabled
                    password = true
                    onChange = {
                        props.uiState.personUsernameAndPassword.copy(
                            currentPassword = it
                        )
                    }
                }
            }

            UstadTextEditField {
                value = props.uiState.personUsernameAndPassword.newPassword
                label = strings[MessageID.new_password]
                error = props.uiState.newPasswordError
                enabled = props.uiState.fieldsEnabled
                password = true
                onChange = {
                    props.uiState.personUsernameAndPassword.copy(
                        newPassword = it
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.personUsernameAndPassword.passwordConfirmed
                label = strings[MessageID.confirm_password]
                error = props.uiState.passwordConfirmedError
                enabled = props.uiState.fieldsEnabled
                password = true
                onChange = {
                    props.uiState.personUsernameAndPassword.copy(
                        passwordConfirmed = it
                    )
                }
            }
        }
    }
}

val PersonAccountEditPreview = FC<Props> {

    var uiStateVar : PersonAccountEditUiState by useState {
        PersonAccountEditUiState(
            personUsernameAndPassword = PersonUsernameAndPasswordModel(
                username = "Bob12",
                currentPassword = "current",
                newPassword = "secret",
                passwordConfirmed = "secret",
            ),
            usernameVisible = true,
            currentPasswordVisible = true
        )
    }

    PersonAccountEditComponent2 {
        uiState = uiStateVar
        onUsernameAndPasswordChanged = {
            uiStateVar = uiStateVar.copy(
                personUsernameAndPassword = it
            )
        }
    }
}


