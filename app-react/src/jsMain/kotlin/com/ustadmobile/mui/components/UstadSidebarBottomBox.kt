package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.hooks.useStringProvider
import mui.material.Box
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.kodein.di.direct
import org.kodein.di.instance
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useMemo
import react.useRequiredContext
import web.cssom.Position
import web.cssom.px

val UstadSidebarBottomBox = FC<Props> {
    val di = useRequiredContext(DIContext)
    val strings = useStringProvider()

    val version = useMemo(dependencies = emptyArray()) {
        di.direct.instance<GetVersionUseCase>().invoke().versionString
    }
    val showPoweredBy = useMemo(dependencies = emptyArray()) {
        di.direct.instance<GetShowPoweredByUseCase>().invoke()
    }

    Box {
        id = "drawer_version_info"
        sx {
            position = Position.absolute
            bottom = 0.px
            padding = 16.px
        }

        Typography {
            align = TypographyAlign.center
            variant = TypographyVariant.caption
            + "${strings[MR.strings.version]} $version"
        }

        ReactHTML.br()

        if(showPoweredBy) {
            UstadPoweredByLink()
        }
    }
}