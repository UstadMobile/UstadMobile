package com.ustadmobile.view

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmState
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.marginTop
import react.ComponentType
import react.Props
import react.RBuilder
import react.RComponent
import styled.StyledHandler
import styled.StyledProps
import styled.css


@JsModule("linkify-react")
@JsNonModule
private external val linkifyReact: dynamic

@Suppress("UnsafeCastFromDynamic")
private val linkifyReactComponent: RComponent<Props, UmState> = linkifyReact.default

external interface LinkifyReactProps: StyledProps {
    var id: String
    var label: String
    var options: Array<Array<Any>>?
    var potato: String
}

fun RBuilder.linkifyReactMessage(
    message: String?,
    left: Boolean = true,
    options: Array<Array<Any>>?,
    systemImpl: UstadMobileSystemImpl,
    handler: StyledHandler<LinkifyReactProps>? = null
) = createStyledComponent(linkifyReactComponent.unsafeCast<ComponentType<LinkifyReactProps>>(),
    "Linkify", handler){
    attrs.id = "Linkify"
    attrs.options = options


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


fun RBuilder.linkifyReactTextView(
    message: String?,
    handler: StyledHandler<LinkifyReactProps>? = null
) = createStyledComponent(linkifyReactComponent.unsafeCast<ComponentType<LinkifyReactProps>>(),
    "Linkify", handler){
    attrs.id = "Linkify"

    umTypography(message,
        variant = TypographyVariant.body1) {
        css {
            +StyleManager.alignTextToStart
            marginTop = 1.spacingUnits
        }
    }

}
