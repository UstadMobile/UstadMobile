package com.ustadmobile.view.person.addaccount

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingUserTypeUiState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingUserTypeViewModel
import com.ustadmobile.hooks.useUstadViewModel
import mui.icons.material.Add
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML

external interface AddAccountSelectNewOrExistingUserTypeProps : Props {
    var uiState: AddAccountSelectNewOrExistingUserTypeUiState
    var onClickPersonalAccount: () -> Unit
    var onClickJoinLearningSpace: () -> Unit
    var onClickNewLearningSpace: () -> Unit
}


val AddAccountSelectNewOrExistingUserTypeComponent2 =
    FC<AddAccountSelectNewOrExistingUserTypeProps> { props ->

        val strings = useStringProvider()

        List {
            if (props.uiState.showAddPersonalAccount) {
                ListItem {
                    key = "0"
                    ListItemButton {
                        id = "personal_account_button"
                        onClick = {
                            props.onClickPersonalAccount()
                        }

                        ListItemIcon {
                            ReactHTML.img {
                                src = "${"img/onboarding_existing.svg"}?fit=crop&auto=format"
                                alt = "illustration connect"
                                height = 40.0
                            }
                        }

                        ListItemText {
                            primary = ReactNode(strings[MR.strings.personal_account])
                            secondary =
                                ReactNode(strings[MR.strings.access_educational_content_download_offline])

                        }
                    }

                }
            }


            ListItem {
                key = "0"
                ListItemButton {
                    id = "learning_space_button"
                    onClick = {
                        props.onClickJoinLearningSpace()
                    }

                    ListItemIcon {
                        ReactHTML.img {
                            src = "${"img/onboarding_add_org.svg"}?fit=crop&auto=format"
                            alt = "illustration connect"
                            height = 40.0
                        }
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.join_learning_space])
                        secondary = ReactNode(strings[MR.strings.eg_for_your_school_organization])

                    }
                }

            }


            ListItem {
                key = "0"
                ListItemButton {
                    id = "new_learning_space_button"
                    onClick = {
                        props.onClickNewLearningSpace()
                    }

                    ListItemIcon {
                        ReactHTML.img {
                            src = "${"img/onboarding_individual.svg"}?fit=crop&auto=format"
                            alt = "illustration connect"
                            height = 40.0
                        }
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.new_learning_space])

                    }
                }

            }
        }

    }

val AddAccountSelectNewOrExistingUserTypeScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AddAccountSelectNewOrExistingUserTypeViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(AddAccountSelectNewOrExistingUserTypeUiState())

    AddAccountSelectNewOrExistingUserTypeComponent2 {
        this.uiState = uiState
        onClickPersonalAccount = viewModel::onClickPersonalAccount
        onClickJoinLearningSpace = viewModel::onClickJoinLearningSpace
        onClickNewLearningSpace = viewModel::onClickNewLearningSpace
    }
}