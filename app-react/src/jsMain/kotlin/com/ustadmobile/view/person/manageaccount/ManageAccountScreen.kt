package com.ustadmobile.view.person.manageaccount

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.viewmodel.person.manageaccount.ManageAccountUiState
import com.ustadmobile.core.viewmodel.person.manageaccount.ManageAccountViewModel
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import kotlinx.datetime.TimeZone
import mui.material.*
import mui.system.*
import mui.system.Stack
import mui.system.StackDirection
import react.*
import web.cssom.px

val ManageAccountScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ManageAccountViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ManageAccountUiState())

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }


    ManageAccountComponent2 {
        this.uiState = uiState
        onClickChangePassword = viewModel::navigateToEditAccount

    }

}

external interface ManageAccountProps : Props {
    var uiState: ManageAccountUiState
    var onClickChangePassword: () -> Unit

}


val ManageAccountComponent2 = FC<ManageAccountProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)
            mui.material.Box {
                ListItem {
                    ListItemText {
                        secondary = ReactNode(strings[MR.strings.name_key])
                        primary = ReactNode(props.uiState.personName ?: "")

                    }
                }
                ListItem {

                    ListItemText {
                        secondary = ReactNode(strings[MR.strings.username])
                        primary = ReactNode(props.uiState.personUsername ?: "")

                    }
                }

                val lastUpdatedDate = useFormattedDateAndTime(
                    timeInMillis = props.uiState.personAuth?.pauthLct ?: 0,
                    timezoneId = TimeZone.currentSystemDefault().id
                )
                ListItem {

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.password])
                        secondary =
                            ReactNode(strings[MR.strings.last_updated] + " " + lastUpdatedDate)

                    }

                    ListItemButton {
                        onClick = {
                            props.onClickChangePassword()
                        }
                        Typography {
                            +ReactNode(strings[MR.strings.change_password])
                        }
                    }
                }
            }
        }
    }
}

