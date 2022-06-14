package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.TypographyVariant
import com.ustadmobile.mui.components.UMListItemProps
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.mui.ext.createStyledComponent
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmState
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.marginTop
import org.w3c.dom.events.Event
import react.*
import styled.css

/**
 * Refer to linkify-react documentation to expand / edit functionality:
 * https://linkify.js.org/docs/linkify-react.html
 */

@JsModule("linkify-react")
@JsNonModule
private external val linkifyReact: dynamic

@Suppress("UnsafeCastFromDynamic")
private val linkifyReactComponent: RComponent<UMListItemProps, UmState> = linkifyReact.default

external interface LinkifyReactProps: UMListItemProps {
    var options: LinkifyOptions
}

class LinkifyOptionsAttributes(){
    var onClick: ((Event) -> Unit)? = null
    var title: String? = null

}
class LinkifyOptions(){
    var tagName: String? = "a"
    var attributes: LinkifyOptionsAttributes? = null

}

fun RBuilder.linkifyReactMessage(
    message: String?,
    left: Boolean = true,
    options: LinkifyOptions,
    systemImpl: UstadMobileSystemImpl,
    accountManager: UstadAccountManager,
    context: Any
) = createStyledComponent(linkifyReactComponent.unsafeCast<ComponentType<LinkifyReactProps>>(),
    "Linkify"){

    val attributes = LinkifyOptionsAttributes()
    attributes.onClick = {
        it.preventDefault()
        it.stopPropagation()
        systemImpl.handleClickLink(it.target.toString(), accountManager, context)

    }
    options.attributes = attributes

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
    systemImpl: UstadMobileSystemImpl,
    accountManager: UstadAccountManager,
    context: Any,
    options: LinkifyOptions = LinkifyOptions()
) = createStyledComponent(linkifyReactComponent.unsafeCast<ComponentType<LinkifyReactProps>>(),
    "Linkify"){

    val attributes = LinkifyOptionsAttributes()
    attributes.onClick = {
        it.preventDefault()
        it.stopPropagation()
        systemImpl.handleClickLink(it.target.toString(), accountManager, context)

    }
    options.attributes = attributes
    attrs.options = options

    umTypography(message,
        variant = TypographyVariant.body1) {
        css {
            +StyleManager.alignTextToStart
            marginTop = 1.spacingUnits
        }
    }

}

