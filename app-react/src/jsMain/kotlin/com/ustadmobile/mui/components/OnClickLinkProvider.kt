package com.ustadmobile.mui.components

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.hooks.useNavControllerEffect
import com.ustadmobile.core.impl.nav.CommandFlowUstadNavController
import react.FC
import react.PropsWithChildren
import react.createContext
import react.useMemo

/**
 * This is roughly modeled on the ViewModelUriHandlerLocalProvider used on Jetpack compose. The
 * context can be used by any child component that has links (e.g. UstadRawHtml). The child
 * component is responsible to intercept link clicks and send them to OnClickLinkUseCase
 */
val OnClickLinkContext = createContext<OnClickLinkUseCase>()

external interface OnClickLinkProviderProps : PropsWithChildren{

    var accountManager: UstadAccountManager

    var openExternalLinkUseCase: OpenExternalLinkUseCase


}

val OnClickLinkProvider  = FC<OnClickLinkProviderProps> { props ->
    console.log("ClickLinkProvider accountMAnager=${props.accountManager} linkUseCase=${props.openExternalLinkUseCase}")

    val navController = useMemo(dependencies = emptyArray()) {
        CommandFlowUstadNavController()
    }

    console.log("ClickLinkProvider accountMAnager=${props.accountManager} linkUseCase=${props.openExternalLinkUseCase}")
    useNavControllerEffect(navController.commandFlow)

    val onClickLinkUseCase = useMemo(dependencies = emptyArray()) {
        OnClickLinkUseCase(
            navController = navController,
            accountManager = props.accountManager,
            openExternalLinkUseCase = props.openExternalLinkUseCase,
            userCanSelectServer = false, //User cannot select server on Web/JS version
        )
    }

    OnClickLinkContext(onClickLinkUseCase) {
        + props.children
    }
}

