package com.ustadmobile.view.ext


import com.ustadmobile.core.account.UstadAccountManager.Companion.ACCOUNTS_ACTIVE_SESSION_PREFKEY
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.util.ext.calculateScoreWithPenalty
import com.ustadmobile.core.util.ext.isContentComplete
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.ext.toolbarJsCssToPartialCss
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.navigation.RouteManager.defaultDestination
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.navigation.UstadDestination
import com.ustadmobile.redux.ReduxAppState
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.centerItem
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
import com.ustadmobile.util.StyleManager.secondaryActionBtn
import com.ustadmobile.util.StyleManager.studentProgressBar
import com.ustadmobile.util.StyleManager.toolbarTitle
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.Util.ASSET_ACCOUNT
import com.ustadmobile.util.Util.stopEventPropagation
import com.ustadmobile.util.ext.*
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ContentEntryListComponent
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    val activeSession = systemImpl.getAppPref(ACCOUNTS_ACTIVE_SESSION_PREFKEY, this)
    if(activeSession == null && viewName != null && viewName != Login2View.VIEW_NAME){
        window.setTimeout({
            window.location.href = "./"
        }, 0)
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
    xs: GridSize? = null,
    sm: GridSize? = null,
    lg: GridSize? = null,
    className: String? = null,
    alignItems: GridAlignItems? = null,
    display: Display = Display.flex,
    flexDirection: FlexDirection = FlexDirection.column,
    handler: StyledHandler<GridProps>? = null) {
    gridItem(xs = xs,sm = sm, lg = lg,
        alignItems = alignItems , className = className,
        handler = handler, display = display, flexDirection = flexDirection)
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
    else if(width in 51..69) "mediumThumbnailClass" else "maxThumbnailClass"}"
){
    umAvatar(src,variant = avatarVariant){
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


fun RBuilder.onClickCall(phoneNumber: String?){

}

fun RBuilder.onClickEmail(email: String?){

}

fun RBuilder.onClickSMS(phoneNumber: String?){

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
    iconName: String, title: String? = null,
    description: String? = null,
    avatarVariant: AvatarVariant = AvatarVariant.circle){

    umGridContainer {
        umItem(GridSize.cells3,  GridSize.cells1){
            umItemThumbnail(iconName, avatarVariant = avatarVariant)
        }

        umItem(GridSize.cells9, GridSize.cells10){
            css{
                marginTop = LinearDimension("5px")
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

fun RBuilder.createItemWithLeftIconTitleDescriptionAndIconBtnOnRight(
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
            css(centerItem)
            styledSpan {
                css{
                    width = 40.px
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
                                    circleIndicator(attendance)
                                }
                                umTypography(attendanceLabel?.format(attendance * 100)){
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
                                        umIconButton("check",
                                            onClick = {
                                                stopEventPropagation(it)
                                                onClickAccept?.invoke()
                                            },
                                            className = "${StyleManager.name}-successClass",
                                            size = IconButtonSize.small)
                                    }

                                    umItem(GridSize.cells4){
                                        styledSpan {
                                            css{
                                                width = 40.px
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
                css(centerItem)
               styledSpan {
                   css{
                       width = 40.px
                   }

                   umIconButton(iconName,
                       onClick = {
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
                        circleIndicator(attendance)
                    }

                    umItem(GridSize.cells4){
                        umTypography(attendanceLabel.format(attendance * 100)){
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

fun RBuilder.createProfileAction(
    icon: String, title: String, xs: GridSize,
    sm: GridSize? = null, visible: Boolean = false,
    action:() -> Unit){
    umItem(xs, sm){
        css{
            display = displayProperty(visible, true)
        }

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
                variant = TypographyVariant.body1,
                gutterBottom = true){
                css(alignTextToStart)
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
    umItem(GridSize.cells3) {
        styledImg {
            css(StyleManager.partnerItem)
            attrs.src = "assets/$logo"
        }
    }
}

fun RBuilder.createContentEntryListItem(
    component: dynamic,
    appDatabase: UmAppDatabase,
    item: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    systemImpl: UstadMobileSystemImpl,
    showSelectBtn: Boolean = false,
    onClick: ((ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null){

    var downloadStatus: ContentJobItemProgress? = null

    val statusObserver = ObserverFnWrapper<ContentJobItemProgress?>{
        (component as Component<*,*>).setState {
            downloadStatus = ContentJobItemProgress().apply {
                total = 1000
                progress = 100
            }
        }
    }

    umGridContainer(columnSpacing = GridSpacing.spacing4) {
        if(onClick != null){
            attrs.onClick = {
                stopEventPropagation(it)
                onClick.invoke(item)
            }
        }

        umItem(GridSize.cells4, GridSize.cells2){
            umItemThumbnail( if(item.leaf) "class" else "folder", item.thumbnailUrl,width = 80,
                iconColor = Color(StyleManager.theme.palette.action.disabled),
                avatarBackgroundColor = Color.transparent)
        }

        umItem(GridSize.cells8, GridSize.cells10){
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
                        css{
                            display = displayProperty(item.leaf, true)
                        }
                        val messageId = ContentEntryListComponent.CONTENT_ENTRY_TYPE_LABEL_MAP[item.contentTypeFlag] ?: MessageID.untitled
                        val icon = ContentEntryListComponent.CONTENT_ENTRY_TYPE_ICON_MAP[item.contentTypeFlag] ?: ""
                        umItem(GridSize.cells2, GridSize.cells1) {
                            umAvatar(className = "${StyleManager.name}-contentEntryListContentAvatarClass") {
                                umIcon(icon, className= "${StyleManager.name}-contentEntryListContentTyeIconClass"){
                                    css{marginTop = 4.px}
                                }
                            }
                        }

                        umItem(GridSize.cells8, GridSize.cells9) {
                            umTypography(systemImpl.getString(messageId, this),
                                variant = TypographyVariant.body2,
                                gutterBottom = true)
                        }

                        if(!showSelectBtn && item.leaf){
                            val downloadLength = downloadStatus?.total ?: 0
                            val downloadedSoFar = downloadStatus?.progress ?: 0
                            val progress = if(downloadLength > 0) {
                                ((downloadedSoFar.toFloat() / downloadLength) * 100).toInt()
                            }else {
                                0
                            }
                            umItem(GridSize.cells2) {
                                css(centerItem)
                                styledSpan{
                                    css{
                                        width = 45.px
                                    }
                                    umIconButton(if(progress == 100) "check_circle" else "download",
                                        size = IconButtonSize.medium, onClick = {
                                            stopEventPropagation(it)
                                            onSecondaryAction?.invoke()
                                        }){
                                        css(secondaryActionBtn)
                                    }
                                }
                            }
                        }

                        if(showSelectBtn){
                            umItem(GridSize.cells2){
                                css(StyleManager.alignTextCenter)
                                umButton(systemImpl.getString(MessageID.select_item, this).format(""),
                                    variant = ButtonVariant.outlined,
                                    color = UMColor.secondary,
                                    onClick = {
                                        it.stopPropagation()
                                        onSecondaryAction?.invoke()
                                    }){
                                    css {
                                        display = displayProperty(showSelectBtn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    GlobalScope.launch {
        val downloadStatus: RateLimitedLiveData<ContentJobItemProgress?> = RateLimitedLiveData(appDatabase, listOf("ContentJobItem"), 1000) {
            appDatabase.contentJobItemDao.findProgressForActiveContentJobItem(item.contentEntryUid)
        }
        downloadStatus.observe(component as DoorLifecycleOwner, statusObserver)
    }
}

fun RBuilder.createListItemWithPersonAndAttendanceProgress(
    systemImpl: UstadMobileSystemImpl,
    item: PersonWithAttemptsSummary,
    onClick: (() -> Unit)? = null){
    umGridContainer(columnSpacing = GridSpacing.spacing2) {
        attrs.onClick = {
            stopEventPropagation(it)
            onClick?.invoke()
        }
        val padding = LinearDimension("4px")
        css{
            padding(top = padding, bottom = padding)
        }

        umItem(GridSize.cells2, GridSize.cells1){
            umProfileAvatar(item.personUid, "person")
        }
        umItem(GridSize.cells10, GridSize.cells11){
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

               umItem (GridSize.cells12){
                   val endDate = if(item.endDate == 0L) "" else " - ${item.endDate.toDate().standardFormat()}"
                   umTypography("${item.startDate.toDate().standardFormat()}$endDate",
                       variant = TypographyVariant.body1){
                       css (alignTextToStart)
                   }
               }

               if(item.scoreProgress?.progress ?: 0 > 0){
                   umItem (GridSize.cells12, flexDirection = FlexDirection.row){
                       umLinearProgress(item.scoreProgress?.progress?.toDouble(),
                           variant = LinearProgressVariant.determinate){
                           css (studentProgressBar)
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
                           variant = LinearProgressVariant.determinate){
                           css (studentProgressBar)
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
    val fullStatementJson = statementEntity.fullStatement ?: return ""
    val statement = JSON.parse<Statement>(fullStatementJson)
    var statementText = statement.`object`?.definition?.description?.get("en-US")
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
    return statementText ?: ""
}

fun RBuilder.createContentEntryListItemWithAttemptsAndProgress(
    systemImpl: UstadMobileSystemImpl,
    item: ContentWithAttemptSummary,
    onClick: ((ContentWithAttemptSummary) -> Unit)? = null
){
    umGridContainer {
        attrs.onClick = {
            onClick?.invoke(item)
        }
        umItem(GridSize.cells4, GridSize.cells2){
            umGridContainer {
                umItem(GridSize.cells12) {
                    umItemThumbnail("class",item.contentEntryThumbnailUrl, width = 80,
                        iconColor = Color(StyleManager.theme.palette.action.disabled),
                        avatarBackgroundColor = Color.transparent)
                }

                umItem(GridSize.cells12) {
                    if(item.scoreProgress?.progress ?: 0 > 0){
                        umLinearProgress(item.scoreProgress?.progress?.toDouble(),
                            variant = LinearProgressVariant.determinate){
                            css{
                                marginTop = 1.spacingUnits
                                width = LinearDimension("80px")
                            }
                        }
                    }
                }
            }
        }

        umItem(GridSize.cells8, GridSize.cells10){
            umGridContainer {

                umItem(GridSize.cells12){
                    umTypography(item.contentEntryTitle,
                        variant = TypographyVariant.h6){
                        css(alignTextToStart)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells3, flexDirection = FlexDirection.row){
                    styledSpan {
                        css{
                            padding(right = 3.spacingUnits)
                        }
                        umIcon("restore", fontSize = IconFontSize.small){
                            css{
                                marginTop = 1.px
                            }
                        }
                    }

                    umTypography("${item.attempts} ${systemImpl.getString(MessageID.attempts, this)}",
                        variant = TypographyVariant.body2){
                        css(alignTextToStart)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells3) {
                    val endDate = if(item.endDate == 0L) "" else " - ${item.endDate.toDate().standardFormat()}"
                    umTypography("${item.startDate.toDate().standardFormat()}$endDate",
                        variant = TypographyVariant.body2){
                        css (alignTextToStart)
                    }
                }

                if(item.duration > 60000){
                    umItem (GridSize.cells12, flexDirection = FlexDirection.row){
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

                umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                    styledSpan {
                        css{
                            padding(right = 3.spacingUnits)
                        }
                        umIcon("emoji_events", fontSize = IconFontSize.small){
                            css{
                                marginTop = 1.px
                            }
                        }
                    }

                    umTypography("${item.scoreProgress?.calculateScoreWithPenalty()}%",
                        variant = TypographyVariant.body2){
                        css(alignTextToStart)
                    }

                    val hideScore = item.scoreProgress == null || (item.scoreProgress?.resultScore == 0 && item.scoreProgress?.progress == 0)

                    if(!hideScore){
                        styledSpan {
                            css{
                                padding(left = 3.spacingUnits)
                            }
                            umTypography("(${item.scoreProgress?.resultScore} / ${item.scoreProgress?.resultMax})",
                                variant = TypographyVariant.body2){
                                css(alignTextToStart)
                            }
                        }
                    }


                    if(item.scoreProgress?.penalty ?:0 > 0){
                        styledSpan {
                            css{
                                padding(left = 3.spacingUnits)
                            }
                            umTypography(systemImpl.getString(MessageID.late_penalty, this).format(item.scoreProgress?.penalty ?: 0),
                                variant = TypographyVariant.body2){
                                css(alignTextToStart)
                            }
                        }
                    }

                }
            }
        }
    }
}


fun RBuilder.createSummaryCard(title: Any?, subTitle: String){
    umItem(GridSize.cells12, GridSize.cells4){

        umPaper(variant = PaperVariant.elevation) {
            css {
                +StyleManager.personDetailComponentActions
                +StyleManager.centerItem
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
