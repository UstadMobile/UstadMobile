package com.ustadmobile.view.siteenterlink

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField
import react.dom.html.ReactHTML.img
import com.ustadmobile.util.ext.onTextChange
import web.cssom.px
import mui.material.*
import mui.system.Stack
import mui.system.responsive
import mui.system.sx
import react.*

external interface SiteEnterLinkProps : Props {
    var uiState: LearningSpaceEnterLinkUiState

    var onClickNext: () -> Unit

    var onClickNewLearningEnvironment: () -> Unit

    var onEditTextValueChange: (String) -> Unit
}

val SiteEnterLinkScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LearningSpaceEnterLinkViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(LearningSpaceEnterLinkUiState())

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

            UstadTextField {
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
        }
    }
}
