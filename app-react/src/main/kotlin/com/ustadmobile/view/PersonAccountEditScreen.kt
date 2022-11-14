package com.ustadmobile.view

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
    Container {
        Stack {
            spacing = responsive(3)

            UstadTextEditField {
                value = props.uiState.personUsernameAndPassword.username
                label = "username"
                onChange = {
                    props.onUsernameAndPasswordChanged(
                        props.uiState.personUsernameAndPassword.copy(
                            username = it
                        )
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.personUsernameAndPassword.currentPassword
                label = "current password"
                onChange = {
                    props.onUsernameAndPasswordChanged(
                        props.uiState.personUsernameAndPassword.copy(
                            currentPassword = it
                        )
                    )
                }
            }

            UstadTextEditField {
                value = props.uiState.personUsernameAndPassword.passwordConfirmed
                label = "confirm password"
                onChange = {
                    props.onUsernameAndPasswordChanged(
                        props.uiState.personUsernameAndPassword.copy(
                            passwordConfirmed = it
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
            personUsernameAndPassword = PersonUsernameAndPasswordModel(
                username = "mullahnasruddin",
                passwordConfirmed = "secret"
            )
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


