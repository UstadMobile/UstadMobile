package com.ustadmobile.view.siteenterlink

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import react.dom.html.ReactHTML.img
import com.ustadmobile.util.ext.onTextChange
import web.cssom.px
import mui.icons.material.Add
import mui.material.*
import mui.system.Stack
import mui.system.responsive
import mui.system.sx
import react.*

external interface SiteEnterLinkProps : Props {
    var uiState: SiteEnterLinkUiState

    var onClickNext: () -> Unit

    var onClickNewLearningEnvironment: () -> Unit

    var onEditTextValueChange: (String) -> Unit
}

val SiteEnterLinkScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SiteEnterLinkViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(SiteEnterLinkUiState())

    SiteEnterLinkComponent2 {
        this.uiState = uiState
        onClickNext = viewModel::onClickNext
        onClickNewLearningEnvironment = { }
        onEditTextValueChange = viewModel::onSiteLinkUpdated
    }

}

val SiteEnterLinkComponent2 = FC <SiteEnterLinkProps> { props ->

    val strings: StringProvider = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)


            img {
                src = "${"img/illustration_connect.svg"}?fit=crop&auto=format"
                alt = "illustration connect"
                height = 300.0
            }

            Typography {
                + strings[MR.strings.please_enter_the_linK]
            }

            TextField {
                id = "sitelink_textfield"
                value = props.uiState.siteLink
                label = ReactNode(strings[MR.strings.site_link])
                helperText = props.uiState.linkError?.let { ReactNode(it) }
                error = helperText != null
                disabled = !props.uiState.fieldsEnabled
                onTextChange = {
                    props.onEditTextValueChange(it)
                }
            }

            Box{
                sx {
                    height = 20.px
                }
            }

            Button {
                id = "next_button"
                onClick = { props.onClickNext() }
                variant = ButtonVariant.contained
                disabled = !props.uiState.fieldsEnabled

                + strings[MR.strings.next]
            }

            Typography {
                align = TypographyAlign.center
                + strings[MR.strings.or].uppercase()
            }

            Button {
                id = "new_env_button"
                onClick = { props.onClickNewLearningEnvironment() }
                variant = ButtonVariant.text

                startIcon = Add.create()

                + strings[MR.strings.create_a_new_learning_env].uppercase()
            }
        }
    }
}

val SiteEnterLinkScreenPreview = FC<Props> {

    val uiStateVar by useState {
        SiteEnterLinkUiState()
    }

    SiteEnterLinkComponent2 {
        uiState = uiStateVar
    }
}