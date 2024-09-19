package com.ustadmobile.view.person.child

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.person.child.EditChildProfileUiState
import com.ustadmobile.core.viewmodel.person.child.EditChildProfileViewModel
import com.ustadmobile.util.ext.onTextChange
import web.cssom.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadDateField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import web.cssom.Color


external interface EditChildProfileProps : Props {
    var uiState: EditChildProfileUiState

    var onPersonChanged: (Person?) -> Unit
}

val EditChildProfileScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        EditChildProfileViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(EditChildProfileUiState())

    EditChildProfileComponent2 {
        this.uiState = uiState
        onPersonChanged = viewModel::onEntityChanged
    }
}

private val EditChildProfileComponent2 = FC<EditChildProfileProps> { props ->

    val strings = useStringProvider()

    val theme by useRequiredContext(ThemeContext)


    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            UstadTextField {
                id = "first_names"
                value = props.uiState.person?.firstNames ?: ""
                label = ReactNode(strings[MR.strings.first_names] + "*")
                onTextChange = {
                    props.onPersonChanged(props.uiState.person?.copy(firstNames = it) ?: Person())
                }
                error = props.uiState.firstNameError != null
                helperText = props.uiState.firstNameError?.let { ReactNode(it) }
            }

            UstadTextField {
                id = "last_name"
                value = props.uiState.person?.lastName ?: ""
                label = ReactNode(strings[MR.strings.last_name] + "*")
                onTextChange = {
                    props.onPersonChanged(props.uiState.person?.copy(lastName = it) ?: Person())
                }
                error = props.uiState.lastNameError != null
                helperText = props.uiState.lastNameError?.let { ReactNode(it) }
            }

            FormControl {
                fullWidth = true
                error = props.uiState.genderError != null

                InputLabel {
                    id = "gender_label"
                    shrink = true
                    sx {
                        backgroundColor = Color(theme.palette.background.default)
                    }
                    + ReactNode(strings[MR.strings.gender_literal] + "*")
                }

                Select {
                    value = props.uiState.person?.gender?.toString() ?: "0"
                    id = "gender"
                    labelId = "gender_label"
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedVal = ("" + event.target.value)
                        props.onPersonChanged(
                            props.uiState.person?.shallowCopy {
                                gender = selectedVal.toInt()
                            }
                        )
                    }

                    props.uiState.genderOptions.filter {
                        it.stringResource != MR.strings.blank || props.uiState.person?.gender == 0
                    }.forEach { option ->
                        MenuItem {
                            value = option.value.toString()
                            + ReactNode(strings[option.stringResource])
                        }
                    }
                }

                FormHelperText {
                    +ReactNode(props.uiState.genderError?: strings[MR.strings.required])
                }
            }
            UstadDateField {
                id = "person_date_of_birth"
                timeInMillis = props.uiState.person?.dateOfBirth ?: 0
                label = ReactNode(strings[MR.strings.birthday])
                timeZoneId = UstadMobileConstants.UTC
                error = props.uiState.dateOfBirthError != null
                helperText = ReactNode(props.uiState.dateOfBirthError)
                onChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            dateOfBirth = it
                        })
                }
            }
        }
    }
}
