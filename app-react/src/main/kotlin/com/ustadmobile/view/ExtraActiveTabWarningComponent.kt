package com.ustadmobile.view

import IndexProps
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import kotlinx.css.*
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import org.kodein.di.DI
import org.kodein.di.instance
import react.RBuilder
import react.RComponent
import styled.css
import styled.styledDiv

class ExtraActiveTabWarningComponent (props: IndexProps): RComponent<IndexProps, UmState>(props){

    val impl : UstadMobileSystemImpl by props.di.instance()

    override fun RBuilder.render() {
        umCssBaseline()
        themeContext.Consumer { _ ->
            umGridContainer {
                css (StyleManager.centerContainer)
                styledDiv {
                    css{
                        +StyleManager.alignCenterItems
                        margin(top = 12.spacingUnits)
                        width = LinearDimension("30% !important")
                    }
                    umIcon("warning", className = "${StyleManager.name}-tabWarningIconClass")
                    umTypography(impl.getString(MessageID.extra_active_tab_warning, this),
                        variant = TypographyVariant.h5,
                        align = TypographyAlign.center
                    ){
                        css {
                            marginTop = 20.px
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.renderExtraActiveTabWarningComponent(
    di: DI) = child(ExtraActiveTabWarningComponent::class) {
    attrs.di = di
}