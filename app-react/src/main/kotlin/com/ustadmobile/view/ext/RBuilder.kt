package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.*
import com.ustadmobile.navigation.RouteManager.defaultRoute
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.entryItemImageContainer
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import com.ustadmobile.util.StyleManager.personListItemAvatar
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch
import styled.StyledHandler
import styled.css
import styled.styledDiv
import styled.styledSpan

fun RBuilder.appBarSpacer() {
    themeContext.Consumer { theme ->
        styledDiv {
            css {
                toolbarJsCssToPartialCss(theme.mixins.toolbar)
            }
        }
    }
}

fun RBuilder.errorFallBack(text: String): ReactElement {
    // Note we purposely use a new RBuilder so we don't render into our normal display
    return RBuilder().mPaper {
        css(mainComponentErrorPaper)
        mTypography(text)
    }
}

fun RBuilder.renderRoutes() {
    hashRouter {
        switch{
            route(path = arrayOf("/"), defaultRoute, exact = true)
            destinationList.forEach {
                route(path = arrayOf("/${it.view}"), it.component, exact = true)
            }
        }
    }
}

fun RBuilder.umGridContainer(spacing: MGridSpacing = MGridSpacing.spacing0,
                             alignContent: MGridAlignContent = MGridAlignContent.stretch,
                             alignItems: MGridAlignItems = MGridAlignItems.stretch,
                             justify: MGridJustify = MGridJustify.flexStart,
                             wrap: MGridWrap = MGridWrap.wrap, className: String? = null, handler: StyledHandler<MGridProps>? = null) {
    mGridContainer(spacing,alignContent,alignItems,justify, wrap) {
        setStyledPropsAndRunHandler(className, handler)
    }
}

fun RBuilder.umItem(xs: MGridSize, sm: MGridSize? = null, lg: MGridSize? = null, className: String? = null, handler: StyledHandler<MGridProps>? = null) {
    mGridItem(xs = xs) {
        sm?.let { attrs.sm = it }
        lg?.let { attrs.md = it }
        setStyledPropsAndRunHandler(className, handler)
    }
}

fun RBuilder.umEntityAvatar (src: String? = null,
                             fallbackSrc: String? = "assets/account.jpg",
                             iconName: String = "add_a_photo",
                             imgProps: RProps? = null,
                             variant: MAvatarVariant = MAvatarVariant.rounded,
                             showIcon: Boolean = true,
                             listItem: Boolean = false,
                             className: String? = "${StyleManager.name}-entityImageClass",
                             clickEvent:((Event) -> Unit)? = null){

    styledDiv {
        css{
            +entryItemImageContainer
            if(!listItem){
                margin = "1.5%"
            }
        }
        mAvatar(src = if(src.isNullOrEmpty()) fallbackSrc else src,
            variant = variant, imgProps = imgProps, className = className) {
            styledSpan{
                css{
                    position = Position.absolute
                    cursor = Cursor.pointer
                    //padding = "20px"
                }
                if(clickEvent != null){
                    attrs.onClickFunction = clickEvent
                }

                if(showIcon){
                    mIcon(iconName, className = "${StyleManager.name}-entityImageIconClass")
                }
            }
        }
    }
}

//Handle this when attachment system is in place
fun RBuilder.umProfileAvatar(attachmentId: Long, fallback: String){
    val src = null
    mAvatar(src,variant = MAvatarVariant.circular){
        css (personListItemAvatar)
        if(src == null) mIcon(fallback, className= "${StyleManager.name}-fallBackAvatarClass")
    }
}


fun RBuilder.handleCall(phoneNumber: String?){

}

fun RBuilder.handleMail(email: String?){

}

fun RBuilder.handleSMS(phoneNumber: String?){

}