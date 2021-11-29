package com.ustadmobile.view.ext


import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.toolbarJsCssToPartialCss
import com.ustadmobile.navigation.RouteManager.defaultRoute
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.defaultMarginBottom
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.entryItemImageContainer
import com.ustadmobile.util.StyleManager.gridListSecondaryItemIcons
import com.ustadmobile.util.StyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import com.ustadmobile.util.StyleManager.personListItemAvatar
import com.ustadmobile.util.Util.ASSET_ACCOUNT
import com.ustadmobile.util.ext.format
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import mui.material.GridProps
import mui.material.GridWrap
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.router.dom.HashRouter
import react.dom.html.ImgHTMLAttributes
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
    HashRouter {
        switch {
            route(path = arrayOf("/"), defaultRoute, exact = true)
            destinationList.forEach {
                route(path = arrayOf("/${it.view}"), it.component, exact = true)
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
    xs: GridSize,
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
            color = TypographyColor.textPrimary){
            css (StyleManager.alignTextToStart)
        }
    }
}

fun RBuilder.createInformation(icon:String? = null, data: String?, label: String? = null, onClick:(() -> Unit)? = null){
    umGridContainer {
        css{
            +defaultMarginTop
            display = displayProperty(data != "0" && !data.isNullOrEmpty(), true)
        }
        umItem(GridSize.column2){
            if(icon != null){
                umIcon(icon, className = "${StyleManager.name}-detailIconClass")
            }
        }

        umItem(GridSize.column10){
            if(onClick != null){
                attrs.asDynamic().onClick = {
                    onClick()
                }
            }
            umTypography("$data",
                color = TypographyColor.textPrimary,
                variant = TypographyVariant.body1){
                css(StyleManager.alignTextToStart)
            }

            if(!label.isNullOrBlank()){
                umTypography(label,
                    color = TypographyColor.textPrimary,
                    variant = TypographyVariant.body2){
                    css(StyleManager.alignTextToStart)
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

fun RBuilder.createCreateNewItem(createNewText: String){
    styledDiv {
        css(listItemCreateNewDiv)
        umListItemIcon("add","${StyleManager.name}-listCreateNewIconClass")
        umTypography(createNewText,variant = TypographyVariant.button,
            color = TypographyColor.textPrimary) {
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

fun RBuilder.createItemWithIconTitleAndDescription(
    iconName: String, title: String? = null,
    description: String? = null,
    scaleOnLargeSmall: Boolean = false){

    umGridContainer(GridSpacing.spacing4) {
        umItem(GridSize.column2, if(scaleOnLargeSmall) GridSize.column3 else GridSize.column1){
            umProfileAvatar(-1,iconName)
        }

        umItem(GridSize.column8, if(scaleOnLargeSmall) GridSize.column8 else GridSize.column10){
            css{
                marginTop = LinearDimension("5px")
                marginLeft = 2.spacingUnits
            }
            if(title != null){
                umItem(GridSize.column11){
                    umTypography(title,
                        variant = TypographyVariant.body1,
                        color = TypographyColor.textPrimary){
                        css (StyleManager.alignTextToStart)
                    }
                }
            }

            if(description != null){
                umItem(GridSize.column11){
                    umTypography(description,
                        variant = TypographyVariant.body2,
                        color = TypographyColor.textPrimary){
                        css (StyleManager.alignTextToStart)
                    }
                }
            }
        }
    }
}

fun RBuilder.createItemWithIconTitleDescriptionAndIconBtn(
    leftIcon: String,
    rightIcon: String,
    title: String?,
    description: String?,
    onClick:()-> Unit)
{
    umGridContainer(GridSpacing.spacing4) {
        umItem(GridSize.column2, GridSize.column1){
            umProfileAvatar(-1,leftIcon)
        }

        umItem(GridSize.column7, GridSize.column9){
            css{
                marginLeft = 2.spacingUnits
            }
            umItem(GridSize.column11){
                umTypography(title,
                    variant = TypographyVariant.body1,
                    color = TypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }

            umItem(GridSize.column11){
                umTypography(description,
                    variant = TypographyVariant.body2,
                    color = TypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }
        }

        umItem(GridSize.column2, GridSize.column1){
            css{
                alignContent = Align.center
                alignItems = Align.center
            }

            mIconButton(rightIcon, size = IconButtonSize.medium,
                onClick = {
                    it.stopPropagation()
                    onClick()
                }
            ){
                css(defaultMarginTop)
            }
        }
    }
}

fun RBuilder.createListItemWithPersonAttendanceAndPendingRequests(
    personUid: Long, fullName: String,
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

        umItem(GridSize.column3, GridSize.column2){
            umProfileAvatar(personUid, "person")
        }

        umItem(GridSize.column9, GridSize.column10){
            umItem(GridSize.column12){
                umTypography(fullName,
                    variant = TypographyVariant.h6,
                    color = TypographyColor.textPrimary){
                    css (StyleManager.alignTextToStart)
                }
            }

            umGridContainer{
                css{
                    display = displayProperty(student, true)
                }

                if(attendance >= 0f){
                    umItem(GridSize.column1){
                        circleIndicator(attendance)
                    }
                }

                umItem(GridSize.column8){
                    umTypography(attendanceLabel?.format(attendance * 100),
                        color = TypographyColor.textPrimary
                    ){
                        css{
                            +StyleManager.alignTextToStart
                            +StyleManager.gridListSecondaryItemDesc
                        }
                    }

                }

                umItem(GridSize.column3){
                    css{
                        display = displayProperty(pending, true)
                        paddingLeft = 5.spacingUnits
                    }
                    umGridContainer(GridSpacing.spacing4) {
                        umItem(GridSize.column4){
                            mIconButton("check",
                                onClick = {
                                    onClickAccept?.invoke()
                                },
                                className = "${StyleManager.name}-successClass",
                                size = IconButtonSize.small)
                        }

                        umItem(GridSize.column4){
                            mIconButton("close",
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
        umItem(GridSize.column2){
            umProfileAvatar(-1, iconName)
        }

        umItem(GridSize.column10){
            umItem(GridSize.column12){
                umTypography(title,
                    variant = TypographyVariant.body1){
                    css(StyleManager.alignTextToStart)
                }
            }

            umItem(GridSize.column12){
                umTypography(subTitle,
                    variant = TypographyVariant.body2){
                    css(StyleManager.alignTextToStart)
                }
            }

            umItem(GridSize.column12){
                umGridContainer{
                    umItem(GridSize.column1){
                        circleIndicator(attendance)
                    }

                    umItem(GridSize.column4){
                        umTypography(attendanceLabel.format(attendance * 100),
                            color = TypographyColor.textPrimary
                        ){
                            css{
                                +StyleManager.alignTextToStart
                                +StyleManager.gridListSecondaryItemDesc
                            }
                        }

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

fun RBuilder.mSpacer(
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
