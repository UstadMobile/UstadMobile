package com.ustadmobile.view

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmState
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import react.ComponentType
import react.Props
import react.RBuilder
import react.RComponent
import styled.StyledHandler
import styled.StyledProps
import styled.css


@JsModule("react-linkify")
@JsNonModule
private external val reactLinkify: dynamic

@Suppress("UnsafeCastFromDynamic")
private val linkifyComponent: RComponent<Props, UmState> = reactLinkify.default

external interface LinkyProps: StyledProps {
    var id: String
    var label: String

}

fun RBuilder.linkifyMessage(
    message: String?,
    left: Boolean = true,
    systemImpl: UstadMobileSystemImpl,
    handler: StyledHandler<LinkyProps>? = null
) = createStyledComponent(linkifyComponent.unsafeCast<ComponentType<LinkyProps>>(),
    "Linkify", handler){
    attrs.id = "Linkify"
    umTypography(
        message,
        variant = TypographyVariant.body1
    ) {
        css {
            +StyleManager.chatMessageContent
            if (left) if (systemImpl.isRtlActive()) +StyleManager.chatRight else +StyleManager.chatLeft
            else if (systemImpl.isRtlActive()) +StyleManager.chatLeft else +StyleManager.chatRight
            if (left) {
                backgroundColor = Color(StyleManager.theme.palette.action.selected)
            } else {
                backgroundColor = Color(StyleManager.theme.palette.primary.dark)
                color = Color.white
            }
        }
    }
}
