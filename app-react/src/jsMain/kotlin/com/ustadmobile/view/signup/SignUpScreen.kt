package com.ustadmobile.view.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.validateusername.ValidateUsernameUseCase
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.signup.SignUpUiState
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadImageSelectButton
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import web.cssom.pct
import web.cssom.px
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import web.cssom.Color

external interface SignUpScreenProps : Props {
    var uiState: SignUpUiState
    var onPersonChanged: (Person?) -> Unit
    var onPersonPictureUriChanged: (String?) -> Unit
    var onTeacherCheckChanged: (Boolean) -> Unit
    var onParentCheckChanged: (Boolean) -> Unit
    var onClickSignUpWithPasskey: () -> Unit
    var onFullNameValueChange: (String) -> Unit
    var onFullNameFocusedChanged: (Boolean) -> Unit
    var onUsernameValueChange: (String) -> Unit

}

val SignUpScreenComponent2 = FC<SignUpScreenProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val errorStr = props.uiState.errorText
    val strings = useStringProvider()
    UstadStandardContainer {
        Stack {
            if(errorStr != null) {
                Typography {
                    sx {
                        color = theme.palette.error.main
                    }

                    variant = TypographyVariant.body1

                    + errorStr
                }
            }
            spacing = responsive(2)

            UstadImageSelectButton {
                imageUri = props.uiState.personPicture?.personPictureUri
                onImageUriChanged = {
                    props.onPersonPictureUriChanged(it)
                }
            }
            TextField {
                sx { width = 100.pct; marginTop = 16.px }
                label = ReactNode("${strings[MR.strings.full_name]}*")
                value = props.uiState.fullName ?: ""
                onTextChange = {
                    props.onFullNameValueChange(it)
                }
                onFocus = {

                }
                onFocus = {
                    props.onFullNameFocusedChanged(true)
                }
                onBlur = {
                    props.onFullNameFocusedChanged(false)
                }
                error = props.uiState.fullNameError != null
                helperText = props.uiState.fullNameError?.let { ReactNode(it) }
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

                UstadTextField {
                    sx { marginTop = 16.px }
                    id = "username"
                    value = props.uiState.person?.username?:""
                    label = ReactNode(strings[MR.strings.username])
                    onTextChange = {
                        props.onUsernameValueChange(it)
                    }
                    onKeyDown = { event ->
                        val char = event.key.singleOrNull()
                        if(char != null && !ValidateUsernameUseCase.isValidUsernameChar(char)) {
                            event.preventDefault()
                        }
                    }
                    error = props.uiState.usernameError != null
                    helperText = props.uiState.usernameError?.let { ReactNode(it) }
                }

                FormHelperText {
                    +ReactNode(props.uiState.genderError?: strings[MR.strings.required])
                }
            }
            Stack{
                direction = responsive(StackDirection.row)

                if (props.uiState.isPersonalAccount) {
                    FormControlLabel {
                        control = Checkbox.create {
                            checked = props.uiState.isTeacher
                            onChange = { _, checked ->
                                props.onTeacherCheckChanged(checked)
                            }
                        }
                        label = ReactNode(strings[MR.strings.i_am_teacher])
                    }

                    FormControlLabel {
                        control = Checkbox.create {
                            checked = props.uiState.isParent
                            onChange = { _, checked ->
                                props.onParentCheckChanged(checked)
                            }
                        }
                        label = ReactNode(strings[MR.strings.i_am_parent])
                    }
                }
            }

            Box{
                sx {
                    height = 10.px
                }
            }
            Button {
                variant = ButtonVariant.contained
                id = "next_button"
                onClick = { props.onClickSignUpWithPasskey() }
                + strings[MR.strings.next]
            }
        }
    }

}

val SignUpScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SignUpViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(SignUpUiState())

    SignUpScreenComponent2 {
        this.uiState = uiState
        onPersonChanged = viewModel::onEntityChanged
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged
        onTeacherCheckChanged = viewModel::onTeacherCheckChanged
        onParentCheckChanged = viewModel::onParentCheckChanged
        onClickSignUpWithPasskey = viewModel::onClickSignup
        onFullNameValueChange = viewModel::onFullNameValueChange
        onFullNameFocusedChanged = viewModel::onFullNameFocusedChanged
        onUsernameValueChange = viewModel::onUsernameChanged
    }
}
