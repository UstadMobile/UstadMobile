package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MIconButtonSize
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.list.mListItemIcon
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.lib.db.entities.Clazz
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
import com.ustadmobile.util.Util.ASSET_ACCOUNT
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
import kotlin.Float

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
                             fallbackSrc: String? = ASSET_ACCOUNT,
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

fun RBuilder.createInformation(icon:String? = null, data: String?, label: String? = null, onClick:(() -> Unit)? = null){
    umGridContainer {
        css{
            +defaultMarginTop
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

fun RBuilder.setBitmaskListText(systemImpl: UstadMobileSystemImpl,textBitmaskValue: Long?): String {
    return BitmaskEditPresenter.FLAGS_AVAILABLE.filter {
        (it.flagVal and (textBitmaskValue ?:0) ) == it.flagVal
    }.joinToString { systemImpl.getString(it.messageId, this) }
}

fun RBuilder.createItemWithIconTitleAndDescription(iconName: String, title: String?, description: String?){
    umGridContainer(MGridSpacing.spacing4) {
        umItem(MGridSize.cells2, MGridSize.cells1){
            umProfileAvatar(-1,iconName)
        }

        umItem(MGridSize.cells10, MGridSize.cells11){
            umItem(MGridSize.cells11){
                mTypography(title,
                    variant = MTypographyVariant.body1,
                    color = MTypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }

            umItem(MGridSize.cells11){
                mTypography(description,
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }
        }
    }
}

fun RBuilder.createItemWithIconTitleDescriptionAndIconBtn(leftIcon: String,rightIcon: String, title: String?, description: String?, onClick:()-> Unit){
    umGridContainer(MGridSpacing.spacing4) {
        umItem(MGridSize.cells2){
            umProfileAvatar(-1,leftIcon)
        }

        umItem(MGridSize.cells8){
            umItem(MGridSize.cells11){
                mTypography(title,
                    variant = MTypographyVariant.body1,
                    color = MTypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }

            umItem(MGridSize.cells11){
                mTypography(description,
                    variant = MTypographyVariant.body2,
                    color = MTypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }
        }

        umItem(MGridSize.cells2){
            css{
                alignContent = Align.center
                alignItems = Align.center
            }

            mIconButton(rightIcon, size = MIconButtonSize.medium,
                onClick = {
                    it.stopPropagation()
                    onClick.invoke()
                }
            ){
                css(defaultMarginTop)
            }
        }
    }
}

fun RBuilder.permissionListText(systemImpl: UstadMobileSystemImpl,tableId: Int, bitmaskValue: Long): String? {
    val flagMessageIds = ScopedGrantEditPresenter.PERMISSION_LIST_MAP[tableId]
    return flagMessageIds?.map { it.toBitmaskFlag(bitmaskValue) }
        ?.filter { it.enabled }
        ?.joinToString { systemImpl.getString(it.messageId, this) }
}