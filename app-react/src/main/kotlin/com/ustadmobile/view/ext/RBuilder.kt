package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.*
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.personListItemAvatar
import kotlinx.css.*
import kotlinx.html.onClick
import react.RBuilder
import react.RProps
import com.ccfraser.muirwik.components.spacingUnits
import com.ustadmobile.util.CssStyleManager.profileImageContainer
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import styled.*

fun RBuilder.renderAvatar(attachmentId: Long, fallback: String){
    val src = null
    mAvatar(src,variant = MAvatarVariant.circular){
        css (personListItemAvatar)
        if(src == null) mIcon(fallback, className= "${CssStyleManager.name}-fallBackAvatar")
    }
}

fun RBuilder.umGridContainer(spacing: MGridSpacing = MGridSpacing.spacing0,
                             alignContent: MGridAlignContent = MGridAlignContent.stretch,
                             alignItems: MGridAlignItems = MGridAlignItems.stretch,
                             justify: MGridJustify = MGridJustify.flexStart,
                             wrap: MGridWrap = MGridWrap.wrap,className: String? = null, handler: StyledHandler<MGridProps>? = null) {
    mGridContainer(spacing,alignContent,alignItems,justify, wrap) {
        setStyledPropsAndRunHandler(className, handler)
    }
}

fun RBuilder.umItem(xs: MGridSize, sm: MGridSize?, className: String? = null, handler: StyledHandler<MGridProps>? = null) {
    mGridItem(xs = xs) {
        sm?.let { attrs.sm = it }
        setStyledPropsAndRunHandler(className, handler)
    }
}

fun RBuilder.umProfileAvatar (src: String? = null, iconName: String = "add_a_photo", imgProps: RProps? = null,
                       variant: MAvatarVariant = MAvatarVariant.rounded,
                       className: String? = "${CssStyleManager.name}-profileImage", clickEvent:(Event) -> Unit){

    styledDiv {
        css(profileImageContainer)
        mAvatar(src = src,variant = variant, imgProps = imgProps, className = className) {
            styledSpan{
                css{
                    position = Position.absolute
                    cursor = Cursor.pointer
                    padding = "20px"
                }
                attrs{
                    onClickFunction = clickEvent
                }
                mIcon(iconName, className = "${CssStyleManager.name}-profileImageIcon")
            }
        }
    }
}

fun RBuilder.handleCall(phoneNumber: String?){

}

fun RBuilder.handleMail(email: String?){

}

fun RBuilder.handleSMS(phoneNumber: String?){

}
