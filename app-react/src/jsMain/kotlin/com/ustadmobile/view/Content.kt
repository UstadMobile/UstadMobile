package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.components.NavHost
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.components.NavResultReturnerModule
import com.ustadmobile.mui.components.OnClickLinkProvider
import web.cssom.px
import mui.system.Box
import mui.system.sx
import org.kodein.di.direct
import org.kodein.di.instance
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.router.Outlet
import react.useMemo
import react.useRequiredContext

val Content = FC<Props> {
    Box {
        component = ReactHTML.main
        sx {
            gridArea = Area.Content
            padding = 0.px
        }

        NavResultReturnerModule {
            NavHost {
                NavHostContentOutlet()
            }
        }
    }
}

/**
 * This needs to be a separate component so that the OnClickLinkProvider will be able to access
 * the NavHostContext
 */
private val NavHostContentOutlet = FC<Props> {
    val di = useRequiredContext(DIContext)
    val accountManagerVal = useMemo(dependencies = emptyArray()) {
        di.direct.instance<UstadAccountManager>()
    }

    val openExternalUseCaseVal = useMemo(dependencies = emptyArray()) {
        di.direct.instance<OpenExternalLinkUseCase>()
    }

    OnClickLinkProvider {
        accountManager = accountManagerVal
        openExternalLinkUseCase = openExternalUseCaseVal

        Outlet()
    }
}
