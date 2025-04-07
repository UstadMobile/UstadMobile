package com.ustadmobile.view.appearance

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.appearance.AppearanceEditUiState
import com.ustadmobile.core.viewmodel.appearance.AppearanceEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadImageSelectButton
import emotion.react.css
import kotlinx.coroutines.Dispatchers
import mui.material.Box
import mui.material.Button
import mui.material.IconButton
import mui.material.InputAdornment
import mui.material.InputAdornmentPosition
import mui.material.OutlinedInput
import mui.material.TextField
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.input
import react.dom.onChange
import react.useRef
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import web.html.InputType
import web.url.URL

val AppearanceEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AppearanceEditViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(AppearanceEditUiState(), Dispatchers.Main.immediate)

    val composeFileInputRef = useRef<web.html.HTMLInputElement>(null)
    val muiFileInputRef = useRef<web.html.HTMLInputElement>(null)

    input {
        ref = composeFileInputRef
        type = InputType.file
        id = "compose_theme_file"
        accept = ".zip"
        css {
            display = "none".unsafeCast<Display>()
        }
        onChange = {
            it.target.files?.item(0)?.also { file ->
                val blobUrl = URL.createObjectURL(file)
                viewModel.onJetpackComposeThemeChanged(blobUrl, file.name)
            }
        }
    }

    input {
        ref = muiFileInputRef
        type = InputType.file
        id = "mui_theme_file"
        accept = ".json"
        css {
            display = "none".unsafeCast<Display>()
        }
        onChange = {
            it.target.files?.item(0)?.also { file ->
                val blobUrl = URL.createObjectURL(file)
                viewModel.onMuiThemeChanged(blobUrl, file.name)
            }
        }
    }

    AppearanceEditComponent {
        this.uiState = uiState
        onOrganisationNameChanged = viewModel::onOrganisationNameChanged
        onOrganisationLogoChanged = viewModel::onOrganisationLogoChanged
        onJetpackComposeThemeChanged = { uri, name ->
            viewModel.onJetpackComposeThemeChanged(uri, name)
        }
        onMuiThemeChanged = { uri, name ->
            viewModel.onMuiThemeChanged(uri, name)
        }
        onClickChooseComposeFile = {
            composeFileInputRef.current?.click()
        }
        onClickChooseMuiFile = {
            muiFileInputRef.current?.click()
        }
    }
}

external interface AppearanceEditProps : Props {
    var uiState: AppearanceEditUiState
    var onOrganisationNameChanged: (String) -> Unit
    var onOrganisationLogoChanged: (String?) -> Unit
    var onJetpackComposeThemeChanged: (uri: String?, name: String?) -> Unit
    var onMuiThemeChanged: (uri: String?, name: String?) -> Unit
    var onClickChooseComposeFile: () -> Unit
    var onClickChooseMuiFile: () -> Unit
}

val AppearanceEditComponent = FC<AppearanceEditProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)

            Box {
                sx {
                    display = Display.flex
                    justifyContent = JustifyContent.center
                    width = 100.pct
                }
                UstadImageSelectButton {
                    imageUri = props.uiState.organisationLogo
                    onImageUriChanged = props.onOrganisationLogoChanged
                }
            }

            TextField {
                label = ReactNode(strings[MR.strings.organisation_name])
                value = props.uiState.organisationName ?: ""
                fullWidth = true
                onChange = { event -> props.onOrganisationNameChanged(event.target.asDynamic().value as String) }
                disabled = !props.uiState.fieldsEnabled
            }

            Typography {
                variant = TypographyVariant.h6
                +strings[MR.strings.jetpack_compose_theme]
            }

            OutlinedInput {
                fullWidth = true
                disabled = true
                value = props.uiState.jetpackComposeThemeName ?:
                        (props.uiState.jetpackComposeTheme?.substringAfterLast('/')?.substringBefore('?') ?:
                        strings[MR.strings.no_file_chosen])

                startAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.start
                    Button {
                        + strings[MR.strings.choose_file]
                        onClick = {
                            props.onClickChooseComposeFile()
                        }
                        disabled = !props.uiState.fieldsEnabled
                    }
                }

                endAdornment = if (props.uiState.jetpackComposeTheme != null) {
                    InputAdornment.create {
                        position = InputAdornmentPosition.end
                        IconButton {
                            onClick = {
                                props.onJetpackComposeThemeChanged(null, null)
                            }
                            disabled = !props.uiState.fieldsEnabled
                            mui.icons.material.Close()
                        }
                    }
                } else null
            }

            Typography {
                variant = TypographyVariant.h6
                +strings[MR.strings.mui_theme]
            }

            OutlinedInput {
                fullWidth = true
                disabled = true
                value = props.uiState.muiThemeName ?:
                        (props.uiState.muiTheme?.substringAfterLast('/')?.substringBefore('?') ?:
                        strings[MR.strings.no_file_chosen])

                startAdornment = InputAdornment.create {
                    position = InputAdornmentPosition.start
                    Button {
                        +strings[MR.strings.choose_file]
                        onClick = {
                            props.onClickChooseMuiFile()
                        }
                        disabled = !props.uiState.fieldsEnabled
                    }
                }

                endAdornment = if (props.uiState.muiTheme != null) {
                    InputAdornment.create {
                        position = InputAdornmentPosition.end
                        IconButton {
                            onClick = {
                                props.onMuiThemeChanged(null, null)
                            }
                            disabled = !props.uiState.fieldsEnabled
                            mui.icons.material.Close()
                        }
                    }
                } else null
            }
        }
    }
}