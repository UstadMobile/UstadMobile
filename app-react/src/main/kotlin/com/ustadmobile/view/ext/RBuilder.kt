package com.ustadmobile.view.ext


import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.util.ext.isContentComplete
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.toolbarJsCssToPartialCss
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.navigation.RouteManager.defaultDestination
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.defaultMarginBottom
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.entryItemImageContainer
import com.ustadmobile.util.StyleManager.gridListSecondaryItemDesc
import com.ustadmobile.util.StyleManager.gridListSecondaryItemIcons
import com.ustadmobile.util.StyleManager.listItemCreateNewDiv
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import com.ustadmobile.util.StyleManager.mainComponentProfileInnerAvatar
import com.ustadmobile.util.StyleManager.mainComponentSearch
import com.ustadmobile.util.StyleManager.mainComponentSearchIcon
import com.ustadmobile.util.StyleManager.personListItemAvatar
import com.ustadmobile.util.StyleManager.toolbarTitle
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.ASSET_ACCOUNT
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.util.ext.*
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ChartOptions
import com.ustadmobile.view.ChartType
import com.ustadmobile.view.ContentEntryListComponent
import com.ustadmobile.view.umChart
import kotlinx.browser.window
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import mui.material.GridProps
import mui.material.GridWrap
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.events.Event
import react.*
import react.dom.html.ImgHTMLAttributes
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import styled.*
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json
import kotlin.reflect.KClass

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

/**
 * Prevent unauthorized access
 */
private fun guardRoute(
    component: KClass<out Component<UmProps, *>>,
    systemImpl: UstadMobileSystemImpl
): ReactElement?  = createElement {
    val viewName = getViewNameFromUrl()
    val accessibleViews = listOf(Login2View.VIEW_NAME, PersonEditView.VIEW_NAME_REGISTER,
        RegisterAgeRedirectView.VIEW_NAME, SiteTermsDetailView.VIEW_NAME_ACCEPT_TERMS)
    val activeSession = systemImpl.getAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, this)
    val logout = activeSession == null && viewName != null
            && accessibleViews.indexOf(viewName) == -1 && viewName != "/"
    //Protest access to app's content without being logged in.
    if(logout){
        window.location.href = "./"
    }
    child(component){}
}

fun RBuilder.renderRoutes(systemImpl: UstadMobileSystemImpl) {
    HashRouter{
        Routes{
            Route{
                attrs.path = "/"
                attrs.element = guardRoute(defaultDestination.component, systemImpl)
            }
            destinationList.forEach {
                Route{
                    attrs.path = "/${it.view}"
                    attrs.element = guardRoute(it.component, systemImpl)
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
    columnSpacing: GridSpacing = GridSpacing.spacing0,
    rowSpacing: GridSpacing = GridSpacing.spacing0,
    wrap: GridWrap = GridWrap.wrap, className: String? = null,
    handler: StyledHandler<GridProps>? = null) {
    gridContainer(spacing,alignContent,alignItems,justify, wrap, columnSpacing, rowSpacing,
        handler = handler, className = className)
}

/**
 * Simplest version of the GridItem used by the app
 */
fun RBuilder.umItem(
    xs: GridSize = GridSize.cells12,
    sm: GridSize? = null,
    lg: GridSize? = null,
    className: String? = null,
    alignItems: GridAlignItems? = null,
    display: Display = Display.flex,
    flexDirection: FlexDirection = FlexDirection.column,
    handler: StyledHandler<GridProps>? = null) {
    gridItem(
        xs = xs,
        sm = sm,
        lg = lg,
        alignItems = alignItems ,
        className = className,
        handler = handler,
        display = display,
        flexDirection = flexDirection)
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
    clickEvent:((Event) -> Unit)? = null
) = styledDiv {
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

//Handle this when attachment system is in place
fun RBuilder.umProfileAvatar(attachmentId: Long, fallback: String){
    val src = null
    umAvatar(src,variant = AvatarVariant.circular){
        css (personListItemAvatar)
        if(src == null) umIcon(fallback, className= "${StyleManager.name}-fallBackAvatarClass")
    }
}

fun RBuilder.umItemThumbnail(
    iconName: String,
    src: String? = null,
    iconColor: Color = Color(StyleManager.theme.palette.background.paper),
    avatarBackgroundColor: Color = Color(StyleManager.theme.palette.action.disabled),
    width: Int = 50,
    marginTop: LinearDimension = 1.spacingUnits,
    avatarVariant: AvatarVariant = AvatarVariant.square,
    className: String? = "${StyleManager.name}-${if(width <= 50) "defaultThumbnailClass"  
    else if(width in 51..69) "mediumThumbnailClass" else "maxThumbnailClass"}",
    onClick: (() -> Unit)? = null
){
    umAvatar(src,variant = avatarVariant){
        attrs.onClick = {
            stopEventPropagation(it)
            onClick?.invoke()
        }
        css {
            this.marginTop = marginTop
            this.width = LinearDimension("${width}px")
            this.height = LinearDimension("${width}px")
            backgroundColor = avatarBackgroundColor
            color = iconColor
        }

        if(src == null){
            umIcon(iconName, className = className)
        }
    }
}


fun RBuilder.createListSectionTitle(titleText: String, variant: TypographyVariant? = null){
    styledDiv {
        css{
            +defaultMarginBottom
            +defaultMarginTop
        }
        umTypography(titleText, variant = variant ?: TypographyVariant.body2){
            css (alignTextToStart)
        }
    }
}

fun RBuilder.createInformation(icon:String? = null, data: String?, label: String? = null,
                               shrink: Boolean = false, onClick:(() -> Unit)? = null){
    umGridContainer {
        css{
            +defaultMarginTop
            display = displayProperty(data != "0" && !data.isNullOrEmpty(), true)
        }
        umItem(GridSize.cells2, if(shrink) GridSize.cells1 else GridSize.cells2){
            if(icon != null){
                umIcon(icon, className = "${StyleManager.name}-detailIconClass")
            }
        }

        umItem(GridSize.cells10, if(shrink) GridSize.cells11 else GridSize.cells10){
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

fun RBuilder.statusCircleIndicator(threshold: kotlin.Float) {
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
        umTypography(createNewText,variant = TypographyVariant.button,) {
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

fun RBuilder.createListItemWithLeftIconTitleAndDescription(
    iconName: String,
    title: String? = null,
    description: String? = null,
    onMainList: Boolean = false,
    avatarVariant: AvatarVariant = AvatarVariant.circle){

    umGridContainer {
        umItem(GridSize.cells3,  if(onMainList) GridSize.cells1 else GridSize.cells2){
            umItemThumbnail(iconName, avatarVariant = avatarVariant)
        }

        umItem(GridSize.cells9, if(onMainList) GridSize.cells11 else GridSize.cells10){
            umGridContainer {
                if(title != null){
                    umItem(GridSize.cells12){
                        umTypography(title,
                            variant = TypographyVariant.body1,
                        ){
                            css (alignTextToStart)
                        }
                    }
                }

                if(description != null){
                    umItem(GridSize.cells12){
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
}

fun RBuilder.createItemWithLeftIconTitleDescriptionAndIconBtnOnRight(
    leftIcon: String,
    iconName: String,
    title: String?,
    description: String?,
    onClick:(Boolean, Event)-> Unit) {
    umGridContainer {
        attrs.onClick = {
            stopEventPropagation(it)
            onClick.invoke(false, it.nativeEvent)
        }
        umItem(GridSize.cells2, GridSize.cells2){
            umProfileAvatar(-1,leftIcon)
        }

        umItem(GridSize.cells7, GridSize.cells9){
            umGridContainer {
                umItem(GridSize.cells12){
                    umTypography(title,
                        variant = TypographyVariant.body1,
                    ){
                        css (alignTextToStart)
                    }
                }

                umItem(GridSize.cells12){
                    umTypography(description,
                        variant = TypographyVariant.body2,
                    ){
                        css (alignTextToStart)
                    }
                }
            }
        }

        umItem(GridSize.cells2, GridSize.cells1){
            css(alignCenterItems)
            styledSpan {
                css{
                    width = 40.px
                }
                umIconButton(iconName, size = IconButtonSize.medium,
                    onClick = {
                        stopEventPropagation(it)
                        onClick.invoke(true, it)
                    }
                ){
                    css(defaultMarginTop)
                }
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
    umGridContainer {
        umItem(GridSize.cells3, GridSize.cells1){
            umProfileAvatar(personUid, "person")
        }

        umItem(GridSize.cells9, GridSize.cells11){
            umGridContainer {
                umItem(GridSize.cells12){
                    umTypography(fullName,variant = TypographyVariant.h6){
                        css (alignTextToStart)
                    }
                }

                if(student){
                    umGridContainer{

                        if(attendance >= 0){

                            umItem(GridSize.cells8, flexDirection = FlexDirection.row){
                                styledSpan {
                                    css{
                                        padding(right = 2.spacingUnits)
                                    }
                                    statusCircleIndicator(attendance)
                                }
                                umTypography(attendanceLabel?.format(attendance.roundTo())){
                                    css{
                                        +alignTextToStart
                                        +gridListSecondaryItemDesc
                                    }
                                }

                            }
                        }

                        if(pending){
                            umItem(GridSize.cells3){

                                umGridContainer(columnSpacing = GridSpacing.spacing2) {
                                    umItem(GridSize.cells4){
                                        styledSpan {
                                            css{
                                                width = 50.px
                                            }
                                            umIconButton("check",
                                                onClick = {
                                                    stopEventPropagation(it)
                                                    onClickAccept?.invoke()
                                                },
                                                className = "${StyleManager.name}-successClass",
                                                size = IconButtonSize.small)
                                        }
                                    }

                                    umItem(GridSize.cells4){
                                        styledSpan {
                                            css{
                                                width = 50.px
                                            }
                                            umIconButton("close",
                                                onClick = {
                                                    stopEventPropagation(it)
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
            }
        }
    }
}

fun RBuilder.createPersonWithAttemptProgress(
    item: PersonWithAttemptsSummary,
    systemImpl: UstadMobileSystemImpl,
    onMainList: Boolean = false){
    umGridContainer{
        val padding = LinearDimension("4px")
        css{
            padding(top = padding, bottom = padding)
        }

        umItem(GridSize.cells3, if(onMainList) GridSize.cells1 else GridSize.cells2){
            umProfileAvatar(item.personUid, "person")
        }

        umItem(GridSize.cells9, if(onMainList) GridSize.cells11 else GridSize.cells10){
            umGridContainer {
                umItem(GridSize.cells12){
                    umTypography("${item.firstNames} ${item.lastName}",
                        variant = TypographyVariant.h6){
                        css (alignTextToStart)
                    }
                }

                umItem(GridSize.cells12, flexDirection = FlexDirection.row){

                    styledSpan {
                        css{
                            padding(right = 4.spacingUnits)
                        }
                        umTypography("${item.attempts} ${systemImpl.getString(MessageID.attempts, this)}",
                            variant = TypographyVariant.body1,
                            paragraph = true){
                            css(alignTextToStart)
                        }
                    }

                    if(item.duration > 60000){

                        styledSpan {
                            css{
                                padding(right = 2.spacingUnits)
                            }
                            umIcon("timer", fontSize = IconFontSize.small){
                                css{
                                    marginTop = 1.px
                                }
                            }
                        }

                        styledSpan {
                            css{
                                padding(right = 2.spacingUnits)
                            }

                            umTypography(item.duration.formatToStringHoursMinutesSeconds(systemImpl),
                                variant = TypographyVariant.body1,
                                paragraph = true){
                                css(alignTextToStart)
                            }
                        }
                    }

                }

                if(item.startDate > 0L){
                    umItem (GridSize.cells12){
                        umTypography(item.startDate.formatDateRange(item.endDate),
                            variant = TypographyVariant.body1){
                            css (alignTextToStart)
                        }
                    }
                }

                if(item.scoreProgress?.progress ?: 0 > 0){
                    umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                        umLinearProgress(item.scoreProgress?.progress?.toDouble(),
                            variant = ProgressVariant.determinate){
                            css (StyleManager.studentProgressBar)
                        }

                        styledSpan {
                            css{
                                padding(left = 4.spacingUnits)
                            }
                            umTypography(systemImpl.getString(MessageID.percentage_complete, this)
                                .format(item.scoreProgress?.progress ?: 0),
                                variant = TypographyVariant.body1,
                                paragraph = true){
                                css(alignTextToStart)
                            }
                        }
                    }
                }

                if(item.scoreProgress?.resultMax ?: 0 > 0){
                    umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                        umLinearProgress(item.scoreProgress?.resultMax?.toDouble(),
                            variant = ProgressVariant.determinate){
                            css (StyleManager.studentProgressBar)
                        }
                        styledSpan {
                            css {
                                padding(left = 4.spacingUnits)
                            }

                            umTypography(
                                systemImpl.getString(MessageID.percentage_score, this)
                                    .format(item.scoreProgress?.calculateScoreWithPenalty() ?: 0),
                                variant = TypographyVariant.body1,
                                paragraph = true
                            ) {
                                css(alignTextToStart)
                            }
                        }

                    }
                }

                if(!item.latestPrivateComment.isNullOrEmpty()){
                    umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                        styledSpan {
                            css {
                                padding(right = 4.spacingUnits)
                            }
                            umIcon("comment", fontSize = IconFontSize.small){
                                css{
                                    marginTop = 1.px
                                }
                            }
                        }

                        umTypography(item.latestPrivateComment,
                            variant = TypographyVariant.body2,
                            paragraph = true){
                            css(alignTextToStart)
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.createPersonListItemWithNameAndUserName(item: Person){
    umGridContainer(GridSpacing.spacing5) {
        val padding = LinearDimension("4px")
        css{
            padding(top = padding, bottom = padding)
        }

        umItem(GridSize.cells3, GridSize.cells1){
            umProfileAvatar(item.personUid, "person")
        }

        umItem(GridSize.cells9, GridSize.cells11){
            umItem(GridSize.cells12){
                umTypography(item.fullName(),
                    variant = TypographyVariant.h6){
                    css (alignTextToStart)
                }
            }

            umItem(GridSize.cells12){
                umTypography(if(item.username.isNullOrEmpty()) "" else "@${item.username}",
                    variant = TypographyVariant.body1,
                    paragraph = true){
                    css(alignTextToStart)
                }
            }
        }
    }
}


fun RBuilder.createListItemWithPersonTitleDescriptionAndAvatarOnLeft(
    title: String,
    subTitle: String? = null,
    iconName: String,
    personUid: Long = -1L,
    onClick: (() -> Unit)? = null){
    umGridContainer {
        attrs.onClick = {
            onClick?.invoke()
        }
        val padding = LinearDimension("4px")
        css{
            padding(top = padding, bottom = padding)
        }

        umItem(GridSize.cells3 , GridSize.cells1){
            umProfileAvatar(personUid, iconName)
        }

        umItem(GridSize.cells9, GridSize.cells11){
            umItem(GridSize.cells12){
                umTypography(title,
                    variant = TypographyVariant.h6){
                    css (alignTextToStart)
                }
            }

            umItem(GridSize.cells12){
                umTypography(subTitle,
                    variant = TypographyVariant.body1,
                    paragraph = true){
                    css(alignTextToStart)
                }
            }
        }
    }
}

fun RBuilder.createListItemWithTitleDescriptionAndRightAction(
    title: String,
    iconName: String,
    withAction: Boolean = false,
    description: String? = null,
    onActionClick: ((Event) -> Unit)? = null){
    umGridContainer {
        umItem(if(withAction) GridSize.cells10 else GridSize.cells12, if(withAction) GridSize.cells11 else GridSize.cells12){
            css{
                paddingTop = LinearDimension("8px")
            }
            umItem(GridSize.cells12) {
                umTypography(title,
                    variant = TypographyVariant.body1,
                    gutterBottom = true){
                    css(alignTextToStart)
                }
            }

            if(description != null){
                umItem(GridSize.cells12) {
                    umTypography(description,
                        variant = TypographyVariant.body2,
                        gutterBottom = true){
                        css(alignTextToStart)
                    }
                }
            }
        }

        if(withAction){
            umItem(GridSize.cells2, GridSize.cells1){
                css(alignCenterItems)
               styledSpan {
                   css{
                       width = 40.px
                   }

                   umIconButton(iconName,
                       onClick = {
                           Util.stopEventPropagation(it)
                           onActionClick?.invoke(it)
                       })
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
        umItem(GridSize.cells3, GridSize.cells2){
            umProfileAvatar(-1, iconName)
        }

        umItem(GridSize.cells9, GridSize.cells10){
            umItem(GridSize.cells12, GridSize.cells12){
                umTypography(title,
                    variant = TypographyVariant.body1){
                    css(alignTextToStart)
                }
            }

            if(subTitle.isNotEmpty()){
                umItem(GridSize.cells12, GridSize.cells12){
                    umTypography(subTitle,
                        variant = TypographyVariant.body2){
                        css(alignTextToStart)
                    }
                }
            }

            umItem(GridSize.cells12, GridSize.cells12){
                umGridContainer{
                    umItem(GridSize.cells1){
                        statusCircleIndicator(attendance)
                    }

                    umItem(GridSize.cells4){
                        umTypography(attendanceLabel.format(attendance.roundTo())){
                            css{
                                +alignTextToStart
                                +gridListSecondaryItemDesc
                            }
                        }

                    }
                }
            }
        }
    }
}


fun RBuilder.createListItemWithIconAndTitle(
    iconName: String,
    title: String,
    onClick: (() -> Unit)? = null
){
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
                    textColor = Color.white,
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
                    css (mainComponentProfileInnerAvatar)
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

fun RBuilder.createTopMainAction(
    icon: String,
    title: String,
    xs: GridSize,
    sm: GridSize? = null,
    visible: Boolean = false,
    variant: TypographyVariant = TypographyVariant.body1,
    textAlign: TypographyAlign = TypographyAlign.left,
    textClassName: String? = null,
    action:() -> Unit){
   if(visible){
       umItem(xs, sm){
           umPaper(variant = PaperVariant.elevation) {
               attrs.onClick = {
                   action()
               }
               css {
                   +StyleManager.personDetailComponentActions
               }

               umIcon(icon){
                   css{
                       +StyleManager.personDetailComponentActionIcon
                   }
               }

               umTypography(title,
                   variant = variant,
                   align = textAlign,
                   gutterBottom = true, className = textClassName)
           }
       }
   }
}

fun RBuilder.createListItemWithTitleAndSwitch(title: String, enabled: Boolean, onClick: (Event) -> Unit){
    umGridContainer {
        attrs.onClick = {
            onClick.invoke(it.nativeEvent)
        }
        umItem(GridSize.cells10, GridSize.cells11){
            umTypography(title,
                variant = TypographyVariant.body1,
                gutterBottom = true){
                css{
                    +alignTextToStart
                    marginTop = LinearDimension("3px")
                }
            }
        }

        umItem(GridSize.cells2, GridSize.cells1){
            css{
                +StyleManager.switchMargin
            }
            umSwitch(enabled, color = UMColor.secondary)
        }

        css{
            marginLeft = LinearDimension("20px")
            marginTop = LinearDimension("16px")
            marginBottom = LinearDimension("16px")
        }
    }
}

fun RBuilder.umPartner(logo: String){
    styledSpan {
        css{
            +alignCenterItems
            padding(left = 3.spacingUnits)
        }
        styledImg {
            css(StyleManager.partnerItem)
            attrs.src = "assets/$logo"
        }
    }
}

fun RBuilder.createContentEntryListItem(
    item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    systemImpl: UstadMobileSystemImpl,
    showSelectBtn: Boolean = false,
    showStatus: Boolean = false,
    downloaded: Boolean = true,
    onClick: ((ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit)? = null,
    mainList: Boolean = true,
    onSecondaryAction: (() -> Unit)? = null){

    umGridContainer(columnSpacing = GridSpacing.spacing4) {
        if(onClick != null){
            attrs.onClick = {
                stopEventPropagation(it)
                onClick.invoke(item)
            }
        }

        umItem(GridSize.cells4, if(mainList) GridSize.cells2 else GridSize.cells1){
            umItemThumbnail( if(item.leaf) "class" else "folder", item.thumbnailUrl,width = 80,
                iconColor = Color(StyleManager.theme.palette.action.disabled),
                avatarBackgroundColor = Color.transparent)
            val progress = (item.scoreProgress?.progress ?: 0).toDouble()
            if(progress > 0){
                umLinearProgress(progress,
                    variant = ProgressVariant.determinate){
                    css (StyleManager.itemContentProgress)
                }
            }
        }

        umItem(GridSize.cells8, if(mainList) GridSize.cells10 else GridSize.cells11){
            umGridContainer {
                umItem(GridSize.cells12){
                    umTypography(item.title,
                        variant = TypographyVariant.h6){
                        css {
                            +alignTextToStart
                            marginBottom = LinearDimension("10px")
                        }
                    }
                }

                umItem(GridSize.cells12){
                    umTypography(item.description?.wordBreakLimit(if(Util.isMobile()) 8 else 50),
                        variant = TypographyVariant.body1,
                        paragraph = true){
                        css(alignTextToStart)
                    }
                }

                umItem(GridSize.cells12){
                    umGridContainer(columnSpacing = GridSpacing.spacing1){
                        val messageId = ContentEntryListComponent.CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag] ?: MessageID.untitled
                        val icon = ContentEntryListComponent.CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag] ?: ""

                        umItem(GridSize.cells2, GridSize.cells1) {
                            if(item.leaf){
                                umAvatar(className = "${StyleManager.name}-contentEntryListContentAvatarClass") {
                                    umIcon(icon, className= "${StyleManager.name}-contentEntryListContentTyeIconClass"){
                                        css{marginTop = 4.px}
                                    }
                                }
                            }
                        }

                        umItem(GridSize.cells8, GridSize.cells9) {
                            if(item.leaf){
                                umTypography(systemImpl.getString(messageId, this),
                                    variant = TypographyVariant.body2,
                                    gutterBottom = true)
                            }
                        }

                        umItem(GridSize.cells2) {
                            umGridContainer {
                                if(showStatus){
                                    umItem(GridSize.cells12) {
                                        css(alignCenterItems)
                                        styledSpan{
                                            css{
                                                width = 45.px
                                            }
                                            umIconButton(if(downloaded) "check_circle" else "download",
                                                size = IconButtonSize.medium, onClick = {
                                                    stopEventPropagation(it)
                                                    //onSecondaryAction?.invoke()
                                                }){
                                                css(StyleManager.secondaryActionBtn)
                                            }
                                        }
                                    }
                                }
                                if(showSelectBtn){
                                    umItem(GridSize.cells12){
                                        css(alignCenterItems)
                                        umButton(systemImpl.getString(MessageID.select_item, this).format(""),
                                            variant = ButtonVariant.outlined,
                                            color = UMColor.secondary,
                                            onClick = {
                                                stopEventPropagation(it)
                                                onSecondaryAction?.invoke()
                                            })
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}


fun RBuilder.renderCreateCommentSection(label: String,onClick: () -> Unit){
    umItem(GridSize.cells12, flexDirection = FlexDirection.row){
        styledSpan {
            css{
                padding(right = 4.spacingUnits)
            }
            umIcon("person"){
                css {
                    marginTop = LinearDimension("20px")
                }
            }
        }

        umItem(GridSize.cells11){
            umTextField(
                disabled = true,
                label = label,
                variant = FormControlVariant.outlined){
                attrs.onClick = {
                    onClick.invoke()
                }
            }
        }

    }
}

private fun RBuilder.iconProgress(progress: ContentEntryStatementScoreProgress?): String{
    return when (progress?.isContentComplete()) {
        StatementEntity.CONTENT_COMPLETE, StatementEntity.CONTENT_PASSED -> "check_circle"
        StatementEntity.CONTENT_FAILED -> "cancel"
        else -> ""
    }
}

fun isContentCompleteImage(person: PersonWithSessionsDisplay): String{
    return if(person.resultComplete){
        when(person.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> "done"
            StatementEntity.RESULT_FAILURE -> "close"
            else -> "query_builder"
        }
    }else{
        "query_builder"
    }
}

fun setContentComplete(systemImpl: UstadMobileSystemImpl,person: PersonWithSessionsDisplay): String{
    val context = Any()
    return  if(person.resultComplete){
        when(person.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> systemImpl.getString(MessageID.passed, context)
            StatementEntity.RESULT_FAILURE -> systemImpl.getString(MessageID.failed, context)
            StatementEntity.RESULT_UNSET ->systemImpl.getString(MessageID.completed, context)
            else -> ""
        }
    }else{
        systemImpl.getString(MessageID.incomplete, context)
    } + " - "
}

fun setStatementQuestionAnswer(statementEntity: StatementEntity): String{
    try{
        val fullStatementJson = statementEntity.fullStatement ?: return ""
        val statement = JSON.parse<Statement>(fullStatementJson)
        var statementText = statement. asDynamic()["object"]?.definition?.description["en-US"].toString()
        val answerResponse = statement.result?.response
        if(answerResponse?.isNotEmpty() == true || answerResponse?.contains("[,]") == true){
            val responses = answerResponse.split("[,]")
            val choiceMap = statement.`object`?.definition?.choices
            val sourceMap = statement.`object`?.definition?.source
            val targetMap = statement.`object`?.definition?.target
            statementText += "\n"
            responses.forEachIndexed { i, it ->
                var description = choiceMap?.find { choice -> choice.id == it }?.description?.get("en-US")
                if(it.contains("[.]")){
                    val dragResponse = it.split("[.]")
                    description = ""
                    description += sourceMap?.find { source -> source.id == dragResponse[0] }?.description?.get("en-US")
                    description += " on "
                    description += targetMap?.find { target -> target.id == dragResponse[1] }?.description?.get("en-US")
                }
                statementText += "${i+1}: ${if(description.isNullOrEmpty()) it else description} \n"
            }

        }
        return statementText
    }catch (e: Exception){
        return ""
    }
}



fun RBuilder.createSummaryCard(title: Any?, subTitle: String){
    umItem(GridSize.cells12, GridSize.cells4){

        umPaper(variant = PaperVariant.elevation) {
            css {
                +StyleManager.personDetailComponentActions
                +alignCenterItems
            }

            umTypography(title?.toString() ?: "",
                variant = TypographyVariant.h4,
                gutterBottom = true){
                css(StyleManager.alignTextCenter)
            }

            umTypography(subTitle,
                variant = TypographyVariant.body1,
                gutterBottom = true){
                css(alignTextToStart)
            }
        }
    }
}

fun RBuilder.drawChart(
    chartData: ChartData ? = null,
    height: Int = 400,
    chartType: ChartType = ChartType.ComboChart,
    onChartRendered: ((Boolean) -> Unit)? = null){
    if(chartData != null){
        val dataTable = mutableListOf<MutableList<Any>>()
        val chartOption: ChartOptions = ChartOptions().apply {
            colors = arrayOf("#009999", "#FF9900", "#0099FF", "#FF3333", "#663399", "#669999",
                "#FF3366", "#990099", "#996666", "#339933", "#FFCC00", "#9966CC", "#FFCC99",
                "#99FFCC", "#0066CC", "#66CCFF", "#FF66FF", "#4D4D4D", "#0066FF", "#FF6600", "#33FFFF",
                "#669933","#808080", "#AF4CAB", "#0040FF","#99CC66","#B1DEFB","#FF7FAA", "#FF8000",
                "#F0AA89", "#6AFF6A", "#339999", "#CCCCCC")
        }
        var distinctXAxisSet = chartData.seriesData.flatMap { it.dataList }
            .mapNotNull {
                it.xAxis
            }.toSet()
        val xAxisData = chartData.reportWithFilters.xAxis

        if(xAxisData == Report.MONTH){
            distinctXAxisSet = distinctXAxisSet.sortedBy {
                Date(it).formatDate(DATE_FORMAT_MM_YYYY)
            }.toSet()
        }

        val labels = mutableListOf<Any>("")
        labels.addAll(chartData.seriesData.map{it.series.reportSeriesName ?: ""}.toList())
        dataTable.add(labels)
        val options: Json = json("" to "")

        val dataSet: MutableMap<String, MutableList<Any>> = mutableMapOf()
        distinctXAxisSet.forEach {
            dataSet[it] = mutableListOf(chartData.xAxisValueFormatter?.format(it) ?: "")
        }

        chartData.seriesData.forEachIndexed { index, data ->
            val seriesType = if(data.series.reportSeriesVisualType == ReportSeries.BAR_CHART)
                "bars" else "line"
            if(chartData.seriesData.size == 1 && index == 0){
                chartOption.seriesType = seriesType
            }

            if(chartData.seriesData.size > 1){
                options[index.toString()] = json("type" to seriesType)
            }
            val groupedByXAxis = data.dataList.filter { it.xAxis != null }.groupBy { it.xAxis }
            val distinctSubgroups = data.dataList.mapNotNull { it.subgroup }.toSet()
            distinctXAxisSet.forEach { xAxisKey ->
                dataSet[xAxisKey]?.add(groupedByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f)
            }

            if(distinctSubgroups.isNotEmpty()){
                distinctSubgroups.forEach { subGroup ->
                    val label = "${data.series.reportSeriesName} - " +
                            data.subGroupFormatter?.format(subGroup)
                    dataTable.first().add(label)
                    distinctXAxisSet.forEach { xAxisKey ->
                        val valData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                        if(valData != null){
                            dataSet[xAxisKey]?.add(valData.yAxis)
                        }
                    }
                }
            }
        }

        dataSet.values.forEach { dataTable.add(it)}

        val dataSetTable = dataTable.map { it.toTypedArray() }.toTypedArray()
        val drawChart = dataSetTable.first().size == dataSetTable.last().size && dataSetTable.size > 1
        if(drawChart){
            umChart(
                data = dataSetTable,
                height = height.px,
                chartType = chartType,
                options = chartOption){}
        }

        onChartRendered?.invoke(drawChart)
    }
}
