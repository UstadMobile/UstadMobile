package com.ustadmobile.view.appearance

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.appearance.AppearanceDetailUiState
import com.ustadmobile.core.viewmodel.appearance.AppearanceDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import kotlinx.coroutines.Dispatchers
import mui.icons.material.AccountCircle
import mui.material.Avatar
import mui.material.Box
import mui.material.TextField
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.pct
import web.cssom.px

val AppearanceDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AppearanceDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(AppearanceDetailUiState(), Dispatchers.Main.immediate)

    AppearanceDetailComponent {
        this.uiState = uiState
    }
}

external interface AppearanceDetailProps : Props {
    var uiState: AppearanceDetailUiState
}

val AppearanceDetailComponent = FC<AppearanceDetailProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {

            spacing = responsive(2)

            ReactHTML.img {
                src = "assets/logo.svg"
                alt = "App Icon"
                height = 60.0
            }

            TextField {
                label = ReactNode(strings[MR.strings.organisation_name])
                value = props.uiState.organisationName ?: ""
                fullWidth = true
                disabled = true
            }


            Typography {
                variant = TypographyVariant.h6
                +strings[MR.strings.jetpack_compose_theme]
            }


            TextField {
                label = ReactNode(strings[MR.strings.jetpack_compose_theme])
                value = props.uiState.jetpackComposeTheme ?: strings[MR.strings.no_file_chosen]
                fullWidth = true
                disabled = true
            }

            Typography {
                variant = TypographyVariant.h6
                +strings[MR.strings.mui_theme]
            }


            TextField {
                label = ReactNode(strings[MR.strings.mui_theme])
                value = props.uiState.muiTheme ?: strings[MR.strings.no_file_chosen]
                fullWidth = true
                disabled = true
            }
        }
    }
}
