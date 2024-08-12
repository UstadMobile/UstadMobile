package com.ustadmobile.view.site.termsdetail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailUiState
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadRawHtml
import mui.material.Box
import mui.material.Button
import mui.system.sx
import react.FC
import react.Props
import react.useEffect
import react.useRef
import react.useState
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import com.ustadmobile.core.MR
import com.ustadmobile.mui.components.ThemeContext
import js.objects.jso
import mui.material.ButtonVariant
import react.useRequiredContext
import web.cssom.Display
import web.cssom.Width
import web.cssom.px
import web.html.HTMLElement

external interface SiteTermsDetailProps: Props{

    var uiState: SiteTermsDetailUiState

    var onClickAccept: () -> Unit

}

val SiteTermsDetailComponent = FC<SiteTermsDetailProps> { props ->
    val muiAppState = useMuiAppState()
    val buttonRef = useRef<HTMLElement>(null)
    var buttonHeight: Int by useState(0)
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)
    val buttonPaddingPx = 8

    useEffect(buttonRef.current?.clientHeight, props.uiState.acceptButtonVisible) {
        console.log("button height = ${buttonRef.current?.clientHeight}")
        buttonHeight = if(props.uiState.acceptButtonVisible) {
            (buttonRef.current?.clientHeight ?: 0) + (buttonPaddingPx * 2)
        }else {
            0
        }
    }

    Box {
        sx {
            height = "calc(100vh - ${(muiAppState.appBarHeight + buttonHeight)}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        UstadRawHtml {
            style = jso {
                padding = theme.spacing(1)
            }

            html = props.uiState.siteTerms?.termsHtml ?: ""
        }
    }

    //If the button is within an if, the ref fails to work
    Button {
        sx {
            display = if(props.uiState.acceptButtonVisible) {
                Display.block
            }else {
                "none".unsafeCast<Display>()
            }
            margin = buttonPaddingPx.px
            width = "calc(100% - ${buttonPaddingPx* 2}px)".unsafeCast<Width>()
        }
        id = "accept_button"
        ref = buttonRef
        variant = ButtonVariant.contained

        onClick = {
            props.onClickAccept()
        }

        + strings[MR.strings.accept]
    }
}

val SiteTermsDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SiteTermsDetailViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(SiteTermsDetailUiState())

    SiteTermsDetailComponent {
        uiState = uiStateVal
        onClickAccept = viewModel::onClickAccept
    }

}
