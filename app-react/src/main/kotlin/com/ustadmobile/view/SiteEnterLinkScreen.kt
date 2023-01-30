package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.useViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.SiteEnterLinkUiState
import com.ustadmobile.core.viewmodel.SiteEnterLinkViewModel
import react.dom.html.ReactHTML.img
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadFab
import csstype.px
import mui.icons.material.Add
import mui.material.*
import mui.system.Container
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

val SiteEnterLinkScreen = FC<UstadScreenProps> { props ->
    val viewModel = useViewModel(
        onAppUiStateChange = props.onAppUiStateChanged
    ) { di, savedSateHandle ->
        console.log("Creating SiteEnterLinkViewModel")
        SiteEnterLinkViewModel(di, savedSateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(SiteEnterLinkUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    SiteEnterLinkComponent2 {
        this.uiState = uiState
        onClickNext = viewModel::onClickNext
        onClickNewLearningEnvironment = { }
        onEditTextValueChange = viewModel::onSiteLinkUpdated
    }

}

val SiteEnterLinkComponent2 = FC <SiteEnterLinkProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)


            img {
                src = "${"img/illustration_connect.svg"}?fit=crop&auto=format"
                alt = "illustration connect"
                height = 300.0
            }

            Typography {
                + strings[MessageID.please_enter_the_linK]
            }

            UstadTextEditField {
                value = props.uiState.siteLink
                label = strings[MessageID.site_link]
                error = props.uiState.linkError?.toString()
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onEditTextValueChange(it)
                }
            }

            Box{
                sx {
                    height = 20.px
                }
            }

            Button {
                onClick = { props.onClickNext() }
                variant = ButtonVariant.contained
                disabled = !props.uiState.fieldsEnabled

                + strings[MessageID.next]
            }

            Typography {
                align = TypographyAlign.center
                + strings[MessageID.or].uppercase()
            }

            Button {
                onClick = { props.onClickNewLearningEnvironment }
                variant = ButtonVariant.text

                startIcon = Add.create()

                + strings[MessageID.create_a_new_learning_env].uppercase()
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