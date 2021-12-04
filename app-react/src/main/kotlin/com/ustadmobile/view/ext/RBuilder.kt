package com.ustadmobile.view.ext


import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.toolbarJsCssToPartialCss
import com.ustadmobile.navigation.RouteManager.defaultRoute
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultMarginBottom
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.entryItemImageContainer
import com.ustadmobile.util.StyleManager.gridListSecondaryItemIcons
import com.ustadmobile.util.StyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import com.ustadmobile.util.StyleManager.mainComponentSearch
import com.ustadmobile.util.StyleManager.mainComponentSearchIcon
import com.ustadmobile.util.StyleManager.personListItemAvatar
import com.ustadmobile.util.StyleManager.toolbarTitle
import com.ustadmobile.util.Util.ASSET_ACCOUNT
import com.ustadmobile.util.ext.format
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import mui.material.GridProps
import mui.material.GridWrap
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.events.Event
import react.Props
import react.RBuilder
import react.createElement
import react.dom.html.ImgHTMLAttributes
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
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

fun RBuilder.errorFallBack(text: String) {
    // Note we purposely use a new RBuilder so we don't render into our normal display
    RBuilder().umPaper {
        css(mainComponentErrorPaper)
        umTypography(text)
    }
}

fun RBuilder.renderRoutes() {
    HashRouter{
        Routes{
            Route{
                attrs.path = "/"
                attrs.element = createElement {
                    child(defaultRoute){}
                }
            }
            destinationList.forEach {
                Route{
                    attrs.path = "/${it.view}"
                    attrs.element = createElement {
                        child(it.component){}
                    }
                }
            }
        }
    }
}

/**
 * Simplest version of the grid container that is frequently used by the app
 */
fun RBuilder.umGridContainer(
    spacing: GridSpacing = GridSpacing.spacing0,
    alignContent: GridAlignContent = GridAlignContent.stretch,
    alignItems: GridAlignItems = GridAlignItems.stretch,
    justify: GridJustify = GridJustify.flexStart,
    wrap: GridWrap = GridWrap.wrap, className: String? = null,
    handler: StyledHandler<GridProps>? = null) {
    gridContainer(spacing,alignContent,alignItems,justify, wrap,
        handler = handler, className = className)
}

/**
 * Simplest version of the GridItem used by the app
 */
fun RBuilder.umItem(
    xs: GridSize? = null,
    sm: GridSize? = null,
    lg: GridSize? = null,
    className: String? = null,
    alignItems: GridAlignItems? = null,
    handler: StyledHandler<GridProps>? = null) {
    gridItem(xs = xs,sm = sm, lg = lg, alignItems = alignItems , className = className, handler = handler)
}

fun RBuilder.umEntityAvatar (
    src: String? = null,
    fallbackSrc: String? = ASSET_ACCOUNT,
    iconName: String = "add_a_photo",
    imgProps: ImgHTMLAttributes<HTMLImageElement>? = null,
    variant: AvatarVariant = AvatarVariant.rounded,
    showIcon: Boolean = true,
    listItem: Boolean = false,
    className: String? = "${StyleManager.name}-entityImageClass",
    iconClassName: String? = "${StyleManager.name}-entityImageIconClass",
    clickEvent:((Event) -> Unit)? = null)
{

    styledDiv {
        css{
            +entryItemImageContainer
            if(!listItem){
                margin = "1.5%"
            }
        }
        umAvatar(src = if(src.isNullOrEmpty()) fallbackSrc else src,
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
                    umIcon(iconName, className = iconClassName)
                }
            }
        }
    }
}

//Handle this when attachment system is in place
fun RBuilder.umProfileAvatar(attachmentId: Long, fallback: String){
    val src = null
    umAvatar(src,variant = AvatarVariant.circular){
        css (personListItemAvatar)
        if(src == null) umIcon(fallback, className= "${StyleManager.name}-fallBackAvatarClass")
    }
}


fun RBuilder.onClickCall(phoneNumber: String?){

}

fun RBuilder.onClickEmail(email: String?){

}

fun RBuilder.onClickSMS(phoneNumber: String?){

}

fun RBuilder.createListSectionTitle(titleText: String){
    styledDiv {
        css{
            +defaultMarginBottom
            +defaultMarginTop
        }
        umTypography(titleText,
            variant = TypographyVariant.body2,
            ){
            css (alignTextToStart)
        }
    }
}

fun RBuilder.createInformation(icon:String? = null, data: String?, label: String? = null, onClick:(() -> Unit)? = null){
    umGridContainer {
        css{
            +defaultMarginTop
            display = displayProperty(data != "0" && !data.isNullOrEmpty(), true)
        }
        umItem(GridSize.cells2){
            if(icon != null){
                umIcon(icon, className = "${StyleManager.name}-detailIconClass")
            }
        }

        umItem(GridSize.cells10){
            if(onClick != null){
                attrs.asDynamic().onClick = {
                    onClick()
                }
            }
            umTypography("$data",
                variant = TypographyVariant.body1){
                css(alignTextToStart)
            }

            if(!label.isNullOrBlank()){
                umTypography(label,
                    variant = TypographyVariant.body2){
                    css(alignTextToStart)
                }
            }
        }
    }
}

fun RBuilder.circleIndicator(threshold: kotlin.Float) {
    umIcon("circle",
        color = when {
            threshold > 0.8f -> IconColor.primary
            threshold > 0.6f -> IconColor.inherit
            else -> IconColor.error
        }){
        css(gridListSecondaryItemIcons)
    }
}

fun RBuilder.createCreateNewItem(createNewText: String, iconName: String = "add"){
    styledDiv {
        css(listItemCreateNewDiv)
        umListItemIcon(iconName,"${StyleManager.name}-listCreateNewIconClass")
        umTypography(createNewText,variant = TypographyVariant.button,
            ) {
            css{
                marginTop = 4.px
            }
        }
    }
}

fun RBuilder.setBitmaskListText(systemImpl: UstadMobileSystemImpl,textBitmaskValue: Long?): String {
    return BitmaskEditPresenter.FLAGS_AVAILABLE.filter { (it.flagVal and (textBitmaskValue ?: -1)) == it.flagVal }
        .joinToString { systemImpl.getString(it.messageId, this) }
}

fun RBuilder.createItemWithIconTitleAndDescription(
    iconName: String, title: String? = null,
    description: String? = null,
    scaleOnLargeSmall: Boolean = false){

    umGridContainer(GridSpacing.spacing4) {
        umItem(GridSize.cells2, if(scaleOnLargeSmall) GridSize.cells2 else GridSize.cells1){
            umProfileAvatar(-1,iconName)
        }

        umItem(GridSize.cells8, if(scaleOnLargeSmall) GridSize.cells8 else GridSize.cells10){
            css{
                marginTop = LinearDimension("5px")
                marginLeft = 2.spacingUnits
            }
            if(title != null){
                umItem(GridSize.cells11){
                    umTypography(title,
                        variant = TypographyVariant.body1,
                        ){
                        css (alignTextToStart)
                    }
                }
            }

            if(description != null){
                umItem(GridSize.cells11){
                    umTypography(description,
                        variant = TypographyVariant.body2,
                        ){
                        css (alignTextToStart)
                    }
                }
            }
        }
    }
}

fun RBuilder.createItemWithIconTitleDescriptionAndIconBtn(
    leftIcon: String,
    iconName: String,
    title: String?,
    description: String?,
    onClick:(Boolean, Event)-> Unit) {
    umGridContainer(GridSpacing.spacing4) {
        attrs.onClick = {
            it.stopPropagation()
            onClick.invoke(false, it.nativeEvent)
        }
        umItem(GridSize.cells2, GridSize.cells1){
            umProfileAvatar(-1,leftIcon)
        }

        umItem(GridSize.cells7, GridSize.cells9){
            css{
                marginLeft = 2.spacingUnits
            }
            umItem(GridSize.cells11){
                umTypography(title,
                    variant = TypographyVariant.body1,
                    ){
                    css (alignTextToStart)
                }
            }

            umItem(GridSize.cells11){
                umTypography(description,
                    variant = TypographyVariant.body2,
                    ){
                    css (alignTextToStart)
                }
            }
        }

        umItem(GridSize.cells2, GridSize.cells1){
            css{
                alignContent = Align.center
                alignItems = Align.center
            }

            umIconButton(iconName, size = IconButtonSize.medium,
                onClick = {
                    it.stopPropagation()
                    onClick.invoke(true, it)
                }
            ){
                css(defaultMarginTop)
            }
        }
    }
}

fun RBuilder.createListItemWithPersonAttendanceAndPendingRequests(
    personUid: Long,
    fullName: String,
    pending: Boolean = false,
    attendance: kotlin.Float = -1f,
    attendanceLabel: String? = null,
    student: Boolean = true,
    onClickDecline: (() -> Unit)? = null,
    onClickAccept: (() -> Unit)? = null){
    umGridContainer(GridSpacing.spacing5) {
        css{
            paddingTop = 4.px
            paddingBottom = 4.px
            width = LinearDimension("100%")
        }

        umItem(GridSize.cells3, GridSize.cells2){
            umProfileAvatar(personUid, "person")
        }

        umItem(GridSize.cells9, GridSize.cells10){
            umItem(GridSize.cells12){
                umTypography(fullName,
                    variant = TypographyVariant.h6,
                    ){
                    css (alignTextToStart)
                }
            }

            umGridContainer{
                css{
                    display = displayProperty(student, true)
                }

                if(attendance >= 0f){
                    umItem(GridSize.cells1){
                        circleIndicator(attendance)
                    }
                }

                umItem(GridSize.cells8){
                    umTypography(attendanceLabel?.format(attendance * 100)
                        
                    ){
                        css{
                            +alignTextToStart
                            +StyleManager.gridListSecondaryItemDesc
                        }
                    }

                }

                umItem(GridSize.cells3){
                    css{
                        display = displayProperty(pending, true)
                        paddingLeft = 5.spacingUnits
                    }
                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.cells4){
                            umIconButton("check",
                                onClick = {
                                    onClickAccept?.invoke()
                                },
                                className = "${StyleManager.name}-successClass",
                                size = IconButtonSize.small)
                        }

                        umItem(GridSize.cells4){
                            umIconButton("close",
                                onClick = {
                                    onClickDecline?.invoke()
                                },
                                className = "${StyleManager.name}-errorClass",
                                size = IconButtonSize.small)
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.createListItemWithAttendance(
    iconName: String, title: String,
    subTitle: String, attendance: kotlin.Float = -1f,
    attendanceLabel: String){
    umGridContainer {
        umItem(GridSize.cells3, GridSize.cells3){
            umProfileAvatar(-1, iconName)
        }

        umItem(GridSize.cells9, GridSize.cells9){
            umItem(GridSize.cells12){
                umTypography(title,
                    variant = TypographyVariant.body1){
                    css(alignTextToStart)
                }
            }

            umItem(GridSize.cells12){
                umTypography(subTitle,
                    variant = TypographyVariant.body2){
                    css(alignTextToStart)
                }
            }

            umItem(GridSize.cells12){
                umGridContainer{
                    umItem(GridSize.cells1){
                        circleIndicator(attendance)
                    }

                    umItem(GridSize.cells4){
                        umTypography(attendanceLabel.format(attendance * 100)){
                            css{
                                +alignTextToStart
                                +StyleManager.gridListSecondaryItemDesc
                            }
                        }

                    }
                }
            }
        }
    }
}


fun RBuilder.createItemWithIconAndTitle(
    iconName: String, title: String, onClick: (() -> Unit)? = null){
    umGridContainer(GridSpacing.spacing1) {
        attrs.onClick = {
            onClick?.invoke()
        }
        umItem(GridSize.cells2, GridSize.cells1){
            umAvatar(variant = AvatarVariant.circle) {
                umIcon(iconName)
            }
        }

        umItem(GridSize.cells10, GridSize.cells11){
            umItem(GridSize.cells12){
                umTypography(title,
                    variant = TypographyVariant.body1){
                    css{
                        justifyContent = JustifyContent.left
                        alignItems = Align.center
                        +alignTextToStart
                    }
                }
            }
        }
    }
}

fun RBuilder.permissionListText(
    systemImpl: UstadMobileSystemImpl,
    tableId: Int, bitmaskValue: Long): String? {

    val flagMessageIds = ScopedGrantEditPresenter.PERMISSION_LIST_MAP[tableId]
    return flagMessageIds?.map { it.toBitmaskFlag(bitmaskValue) }
        ?.filter { it.enabled }
        ?.joinToString { systemImpl.getString(it.messageId, this) }
}

fun RBuilder.umSpacer(
    left: LinearDimension? = null, right: LinearDimension? = null,
    top: LinearDimension? = 1.spacingUnits,
    bottom: LinearDimension? = 1.spacingUnits) {
    styledDiv {
        css {
            if (left != null) {
                marginLeft = left
            }

            if (right != null) {
                marginRight = right
            }

            if (top != null) {
                marginTop = top
            }

            if (bottom != null) {
                marginBottom = bottom
            }
        }
    }
}

fun RBuilder.umTopBar(
    appState: ReduxAppState,
    currentDestination: UstadDestination,
    searchLabel: String,
    name: String? = null,
    onClick: (() -> Unit)?){
    umAppBar(position = AppBarPosition.fixed) {
        css (if(currentDestination.showNavigation) StyleManager.mainComponentAppBar
        else StyleManager.mainComponentAppBarWithNoNav)

        umToolbar {
            attrs.asDynamic().id = "um-toolbar"
            umTypography(appState.appToolbar.title ?: "",variant = TypographyVariant.h6, noWrap = true, component = "div") {
                val props = { }
                props.asDynamic().style = kotlinext.js.js {
                    flexGrow = 1; display = { sm = "block" } }
                attrs.asDynamic().sx = props
                css{
                    flexGrow = 1.0
                    +toolbarTitle
                }
            }

            styledDiv {
                css {
                    +mainComponentSearch
                    display = displayProperty(currentDestination.showSearch)
                }
                styledDiv {
                    css(mainComponentSearchIcon)
                    umIcon("search")
                }

                umInput(placeholder = searchLabel,
                    disableUnderline = true) {
                    attrs.asDynamic().inputProps = object: Props {
                        val className = "${StyleManager.name}-mainComponentInputSearchClass"
                        val id = "um-search"
                    }
                }
            }

            umAvatar {
                css {
                    display = displayProperty(currentDestination.showNavigation)
                    +StyleManager.mainComponentProfileOuterAvatar
                }
                attrs.onClick = { onClick?.invoke() }

                umAvatar{
                    css (StyleManager.mainComponentProfileInnerAvatar)
                    umTypography("${name?.first()}",
                        align = TypographyAlign.center,
                        variant = TypographyVariant.h5){
                        css{
                            marginTop = LinearDimension("1.5px")
                        }
                    }
                }
            }
        }
    }
}
