package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.list.mListItemIcon
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.navigation.RouteManager.defaultRoute
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.clazzListItemSecondaryIcons
import com.ustadmobile.util.StyleManager.defaultMarginBottom
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.entryItemImageContainer
import com.ustadmobile.util.StyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import com.ustadmobile.util.StyleManager.personListItemAvatar
import com.ustadmobile.util.ext.formatDate
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
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
import kotlin.Float
import kotlin.js.Date

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

fun RBuilder.createListSectionTitle(titleText: String){
    styledDiv {
        css{
            +defaultMarginBottom
            +defaultMarginTop
        }
        mTypography(titleText,
            variant = MTypographyVariant.body2,
            color = MTypographyColor.textPrimary){
            css (StyleManager.alignTextToStart)
        }
    }
}

fun RBuilder.copyToClipboard(text: String, copyHandler:()-> Unit){
    val secure = js("typeof(navigator.clipboard)!='undefined' " +
            "&& window.isSecureContext").toString().toBoolean()
    if(secure){
        window.navigator.clipboard.writeText(text).then {
            copyHandler()
        }
    }else{
        val element = document.createElement("textarea")
        val textArea = element.asDynamic()
        textArea.value = text
        textArea.style.position = "fixed"
        textArea.style.left = "-999999px"
        textArea.style.top = "-999999px"
        document.body?.appendChild(textArea)
        textArea.focus()
        textArea.select()
        val copied = document.execCommand("copy")
        if(copied){
            copyHandler()
        }
        textArea.remove()
    }
}

fun RBuilder.createInformation(icon:String? = null, data: String?, label: String? = null, onClick:(() -> Unit)? = null){
    umGridContainer {
        css{
            +StyleManager.defaultMarginTop
            display = StyleManager.displayProperty(data != "0" || !data.isNullOrEmpty(), true)
        }
        umItem(MGridSize.cells2){
            if(icon != null){
                mIcon(icon, className = "${StyleManager.name}-detailIconClass")
            }
        }

        umItem(MGridSize.cells10){
            if(onClick != null){
                attrs.asDynamic().onClick = {
                    onClick()
                }
            }
            mTypography("$data",
                color = MTypographyColor.textPrimary,
                variant = MTypographyVariant.body1){
                css(StyleManager.alignTextToStart)
            }

            if(!label.isNullOrBlank()){
                mTypography(label,
                    color = MTypographyColor.textPrimary,
                    variant = MTypographyVariant.body2){
                    css(StyleManager.alignTextToStart)
                }
            }
        }
    }
}

fun RBuilder.circleIndicator(threshold: Float) {
    mIcon("circle",
        color = when {
            threshold > 0.8f -> MIconColor.primary
            threshold > 0.6f -> MIconColor.inherit
            else -> MIconColor.error
        }){
        css(clazzListItemSecondaryIcons)
    }
}

fun RBuilder.renderCreateNewItemView(createNewText: String){
    styledDiv {
        css(listItemCreateNewDiv)
        mListItemIcon("add","${StyleManager.name}-listCreateNewIconClass")
        mTypography(createNewText,variant = MTypographyVariant.button,
            color = MTypographyColor.textPrimary) {
            css{
                marginTop = 4.px
            }
        }
    }
}