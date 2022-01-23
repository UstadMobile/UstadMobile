package com.ustadmobile.util

import Breakpoint
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.theme.styledModule
import com.ustadmobile.redux.ReduxAppStateManager
import down
import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.Timing
import kotlinx.css.properties.Transition
import kotlinx.css.properties.ms
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import styled.StyleSheet
import up

/**
 * Responsible for styling HTML elements, to customize particular
 * element just check the defined style constants.
 * They are named as per component
 */
object StyleManager: StyleSheet("ComponentStyles", isStatic = true), DIAware {

    val theme = ReduxAppStateManager.getCurrentState().appTheme?.theme!!

    private val systemImpl : UstadMobileSystemImpl by instance()

    private val fullWidth = 100.pct

    const val drawerWidth = 240

    val tabletAndHighEnd = Breakpoint.sm

    val alignTextToStart by css {
        textAlign = TextAlign.start
    }

    val contentAfterIconMarginLeft by css {
        marginLeft = LinearDimension("2%")
    }

    val umItemWithIconAndText by css {
        display = Display.flex
        flexDirection = FlexDirection.row
    }

    val alignTextCenter by css{
        textAlign = TextAlign.center
    }

    val defaultFullWidth by css {
        width = LinearDimension("100%")
    }

    val defaultMarginTop  by css{
        marginTop = 2.spacingUnits
    }

    val defaultMarginBottom  by css{
        marginBottom = 2.spacingUnits
    }

    val defaultPaddingTop by css{
        paddingTop = 3.spacingUnits
    }

    val defaultPaddingTopBottom by css{
        paddingTop = 2.spacingUnits
        paddingBottom = 2.spacingUnits
    }

    val defaultDoubleMarginTop  by css{
        marginTop = 4.spacingUnits
    }

    val errorTextClass by css{
        color = Color(theme.palette.error.main)
        marginLeft = LinearDimension("${if(systemImpl.isRtlActive()) 0 else 16}px")
        marginRight= LinearDimension("${if(systemImpl.isRtlActive()) 16 else 0}px")
    }

    val errorClass by css {
        color = Color(theme.palette.error.main)
    }

    val successClass by css {
        color = Color.green.lighten(500)
    }

    val splashComponentContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
    }

    val splashComponentPreloadContainer by css{
        left = LinearDimension("50%")
        top = LinearDimension("50%")
        marginLeft = (-100).px
        marginTop = (-50).px
        position =  Position.fixed
        height = 200.px
        width = 200.px
    }

    val mainComponentErrorPaper by css{
        padding(2.spacingUnits)
        marginBottom = 2.spacingUnits
        color = Color.red
    }

    val mainComponentProgressIndicator by css {
        width = 100.pct
        display = Display.none
    }

    val mainComponentContainer by css {
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        width = 100.pct
    }

    val mainComponentWrapperContainer by css {
        flexGrow = 1.0
        width = 100.pct
        zIndex = 1
        overflow = Overflow.hidden
        position = Position.relative
        display = Display.flex
        flexDirection = FlexDirection.column
        backgroundColor = Color(theme.palette.background.paper)
    }

    val mainComponentAppBar by css{
        position = Position.absolute
        marginLeft = when {
            systemImpl.isRtlActive() -> 0.px
            else -> drawerWidth.px
        }
        width = fullWidth
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            width = fullWidth - when {
                systemImpl.isRtlActive() -> 0.px
                else -> drawerWidth.px
            }
        }
    }


    val detailPaddingBottom by css {
        padding(bottom = 3.spacingUnits)
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            padding(bottom = 2.spacingUnits)
        }
    }

    val entryDetailRightSection by css {
        padding(bottom = 3.spacingUnits, top = 8.spacingUnits)
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            padding(bottom = 2.spacingUnits)
        }
    }

    val switchMargin by css {
        paddingRight = 5.spacingUnits
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            paddingRight = 1.spacingUnits
        }
    }

    val screenWithChartOnLeft by css {
        marginRight = 0.spacingUnits
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            marginRight = 2.spacingUnits
        }
    }

    val mainComponentAppBarWithNoNav by css{
        position = Position.absolute
        marginLeft = 0.px
        width = fullWidth
    }

    val mainComponentContentContainer by css {
        height = LinearDimension("100vh")
        flexGrow = 1.0
        minWidth = 0.px
        backgroundColor = Color(theme.palette.background.default)
        media(theme.breakpoints.down(Breakpoint.xs)){
            height = LinearDimension("91vh")
        }
    }

    val mainComponentBottomNav by css {
        position = Position.fixed
        bottom = 0.px
        left = 0.px
        right = 0.px
    }

    val mainComponentBrandIconContainer by css {
        height = 43.px
        marginTop = 20.px
        width = LinearDimension("100%")
        padding = "0px ${if(systemImpl.isRtlActive()) 20 else 0 }px 0 ${if(systemImpl.isRtlActive()) 0 else 20 }px"
    }

    val mainComponentBrandIcon by css{
        width = LinearDimension("80%")
        height = LinearDimension("60%")
    }

    val mainComponentSideNavMenuList by css {
        backgroundColor = Color(theme.palette.background.paper)
        width = drawerWidth.px
    }

    val toolbarTitle by css {
        if(systemImpl.isRtlActive()){
            textAlign = TextAlign.start
            marginRight = drawerWidth.px
            media(theme.breakpoints.down(tabletAndHighEnd)) {
                marginRight = 0.px

            }
        }
    }

    val mainComponentProfileOuterAvatar by css {
        width = 40.px
        height = 40.px
        cursor = Cursor.pointer
        margin = "0px ${if(systemImpl.isRtlActive()) 10 else 0 }% 0 ${if(systemImpl.isRtlActive()) 0 else 10 }%"
        media(theme.breakpoints.down(tabletAndHighEnd)) {
            margin = "0px ${if(systemImpl.isRtlActive()) 10 else 3 }% 0 ${if(systemImpl.isRtlActive()) 0 else 10 }%"
        }
        backgroundColor = Color(theme.palette.primary.light)
        alignItems = Align.center
        alignContent = Align.center
    }

    val mainComponentProfileInnerAvatar by css {
        width = 36.px
        height = 36.px
        color = Color.white
        if(systemImpl.isRtlActive()){
            marginRight = (2.5).px
        }else{
            marginLeft = (2.5).px
        }
        marginTop = 2.px
        backgroundColor = Color(theme.palette.primary.dark)
    }

    val mainComponentInputSearchClass by css {
        padding = "8px 8px 8px 50px"
        transition += Transition("width", theme.transitions.duration.standard.ms, Timing.easeInOut, 0.ms)
        width = LinearDimension("30ch")
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            width = LinearDimension("40ch")
            focus {
                width = LinearDimension("50ch")
            }
        }
        flexGrow = 1.0
        color = Color.inherit
        paddingRight = (if(systemImpl.isRtlActive()) 60 else 0).px
    }

    val mainComponentSearch by css {
        position = Position.relative
        borderRadius = theme.shape.borderRadius.px
        backgroundColor = styledModule.alpha(theme.palette.common.white, 0.15)
        hover {
            backgroundColor =  styledModule.alpha(theme.palette.common.white, 0.25)
        }
        marginLeft = 0.px
        width = LinearDimension("100%")
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            marginLeft = 1.spacingUnits
            width = LinearDimension("auto")
        }

        media(theme.breakpoints.down(tabletAndHighEnd)) {
            width = LinearDimension("80%")
        }
    }

    val mainComponentSearchIcon by css {
        padding = "0 16px"
        height = LinearDimension("100%")
        position = Position.absolute
        pointerEvents = PointerEvents.none
        display = Display.flex
        alignItems = Align.center
        justifyContent = JustifyContent.center
    }

    val mainComponentToolbarMargins by css{
        if(systemImpl.isRtlActive()){
            paddingRight = 30.px
        }else{
            paddingLeft = 30.px
        }
    }

    val mainComponentFab by css{
        if(!systemImpl.isRtlActive()) right = 15.px
        if(systemImpl.isRtlActive()) left = 15.px
        position = Position.fixed
        bottom = 70.px
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            bottom = 15.px
        }
    }


    val languageComponentLanguageSelectorFormControl by css {
        margin(1.spacingUnits)
        minWidth = (drawerWidth * 0.9).px
        width = LinearDimension.auto
        display = Display.flex
        position = Position.fixed
        height = LinearDimension.auto
        bottom = 16.px
    }

    fun displayProperty(visible:Boolean, isFlexLayout:Boolean = false): Display {
       return if(visible)
           if(isFlexLayout) Display.flex else Display.block
       else Display.none
    }

    fun maxLines(builder: CssBuilder,maxLine: Int) {
        val baseLineHeight = 1.5
        builder.lineHeight = LineHeight("${baseLineHeight}em")
        builder.height = LinearDimension("${baseLineHeight * maxLine}em")
        builder.textOverflow = TextOverflow.ellipsis
        builder.overflow = Overflow.hidden
    }

    val tabsContainer by css {
        flexGrow = 1.0
        height = LinearDimension("100%")
        +defaultFullWidth
    }


    val fieldsOnlyFormScreen by css {
        paddingLeft = 4.spacingUnits
        paddingRight = 4.spacingUnits
        paddingTop = 4.spacingUnits
        height = LinearDimension("100vh")
        overflow = Overflow.scroll
        paddingBottom = 16.spacingUnits
        width = LinearDimension("99.5%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            width = LinearDimension("99.5%")
        }
    }

    val scrollOnMobile by css {
        overflow = Overflow.scroll
        media(theme.breakpoints.up(tabletAndHighEnd)){
            overflow = Overflow.hidden
        }
    }

    val contentContainer by css {
        marginLeft = 1.spacingUnits
        marginRight = 1.spacingUnits
        height = LinearDimension("100vh")
        overflow = Overflow.scroll
        paddingLeft = 2.spacingUnits
        paddingRight = 2.spacingUnits
        paddingBottom = 16.spacingUnits
        width = LinearDimension("95.5%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            width = LinearDimension("96.5%")
            marginLeft = 3.spacingUnits
            marginRight = 3.spacingUnits
        }
    }


    val reportActionText by css {
        fontSize = LinearDimension("1em")
        textAlign = TextAlign.center
    }

    val startIcon by css{
        marginRight = 6.spacingUnits
        media(theme.breakpoints.up(tabletAndHighEnd)){
            marginRight = 2.spacingUnits
        }
    }

    val endIcon by css{
        marginLeft = 4.spacingUnits
        media(theme.breakpoints.up(tabletAndHighEnd)){
            marginLeft = 2.spacingUnits
        }
    }


    val centerContainer by css {
        display = Display.flex
        justifyContent = JustifyContent.center
        height = LinearDimension("70vh")
        width = LinearDimension("100wh")
        alignItems = Align.center
    }

    val alignCenterItems by css {
        alignItems = Align.center
        textAlign = TextAlign.center
        flexShrink = 0.0
    }

    val alignStartItems by css {
        alignItems = Align.start
        textAlign = TextAlign.start
        flexShrink = 0.0
    }

    val alignEndItems by css {
        alignItems = Align.end
        textAlign = TextAlign.end
        flexShrink = 0.0
    }


    val listComponentContainer by css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
    }

    val listComponentContainerWithScroll by css {
        display = Display.inlineFlex
        flexDirection = FlexDirection.column
        height = LinearDimension("100vh")
        overflow = Overflow.scroll
        padding(top = 2.spacingUnits, horizontal = 2.spacingUnits, 0.spacingUnits)
        width = LinearDimension("100%")
    }

    val entryListItemContainer by css {
        width = LinearDimension("100%")
        display = Display.flex
        flexDirection = FlexDirection.row
    }

    val listCreateNewContainer by css {
        padding = "10px"
        marginBottom = 2.spacingUnits
    }

    val contentEntryListContentAvatarClass by css {
        height = 3.spacingUnits
        width = 3.spacingUnits
    }

    val contentEntryListContentTyeIconClass by css {
        fontSize = LinearDimension("0.65em")
        marginBottom = 4.px
    }

    val horizontalList by css {
        width = LinearDimension("100%")
        backgroundColor = Color(theme.palette.background.paper)
    }

    val horizontalListEmpty by css {
        width = LinearDimension("100%")
    }

    val listItemCreateNewDiv by css {
        display = Display.inlineFlex
        marginLeft = 16.px
        paddingTop = 10.px
        paddingBottom = 10.px
    }

    val listCreateNewIconClass by css {
        fontSize = LinearDimension("2.5em")
        marginTop = 5.px
    }

    val chipSetFilter by css{
        display = Display.flex
        justifyContent = JustifyContent.start
        flexWrap = FlexWrap.wrap
    }

    val selectionContainer by css{
        paddingTop = 12.px
        paddingBottom = 12.px
        width = LinearDimension("100%")
        backgroundColor = Color(theme.palette.background.default)
    }

    val entityImageClass by css {
        textAlign = TextAlign.center
        position = Position.relative
        width = LinearDimension("98%")
        height = 300.px
    }

    val entityThumbnailClass by css {
        textAlign = TextAlign.center
        position = Position.relative
        width = LinearDimension("100%")
        height = 80.px
        media(theme.breakpoints.up(tabletAndHighEnd)){
            width = LinearDimension("70%")
            height = 120.px
        }
    }

    val entityImageIconClass by css {
        fontSize = LinearDimension("3em")
    }

    val emptyListIcon by css {
        display = Display.table
        margin = "0 auto"
        fontSize = LinearDimension("7em")
    }

    val entryItemImageContainer by css {
        width = LinearDimension("100%")
        textAlign = TextAlign.center
    }

    val fallBackAvatarClass by css {
        fontSize = LinearDimension("2em")
        marginBottom = 4.px
    }

    val maxThumbnailClass by css {
        fontSize = LinearDimension("5em")
    }

    val mediumThumbnailClass by css {
        fontSize = LinearDimension("2em")
    }

    val defaultThumbnailClass by css {
        fontSize = LinearDimension("1.2em")
    }

    val secondaryActionBtn by css{
        width = 60.px
        padding = "16px"
    }

    val personListItemAvatar by css {
        width = 50.px
        height = 50.px
        margin = "2px ${if(systemImpl.isRtlActive()) 2.4 else 0 }px 0 ${if(systemImpl.isRtlActive()) 0 else 2.4 }px"
        color = Color(theme.palette.background.paper)
        backgroundColor = Color(theme.palette.action.disabled)
    }

    val contentEntryDetailOverviewComponentOpenBtn by css {
        margin = "3% 1.5% 3% 1.5%"
        width = LinearDimension("98%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            margin = "2% 1.5% 0% 1.5%"
        }
    }

    val detailContentProgress by css{
        margin(left = LinearDimension("1.5%"), right = LinearDimension("1.5%"))
        width = LinearDimension("98%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            margin(left = LinearDimension("1.5%"), right = LinearDimension("1.5%"))
        }
    }

    val itemContentProgress by css{
        margin(top = 2.spacingUnits, left = LinearDimension("1.5%"), right = LinearDimension("1.5%"))
        width = LinearDimension("43%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            margin(top = 1.spacingUnits,left = LinearDimension("1.5%"), right = LinearDimension("1.5%"))
        }
    }

    val detailIconClass by css {
        fontSize = LinearDimension("2em")
        marginTop = 3.px
    }

    val iframeComponentResponsiveIframe by css{
        overflow = Overflow.hidden
        width = LinearDimension("100%")
        backgroundColor = Color.transparent
        border = "0px"
        minHeight = LinearDimension("75%")
        media(theme.breakpoints.up(tabletAndHighEnd)){
            minHeight = LinearDimension("100%")
        }
    }

    val personDetailComponentActions by css{
        display = Display.flex
        flexDirection = FlexDirection.column
        alignContent = Align.center
        alignItems = Align.center
        paddingBottom = 16.px
        padding = "16px 30px 16px 30px"
        cursor = Cursor.pointer
        width = LinearDimension("100%")
        hover {
            backgroundColor = Color(theme.palette.action.selected)
        }
    }

    val personDetailComponentActionIcon by css{
        marginBottom = 10.px
    }

    val videoComponentResponsiveMedia by css{
        overflow = Overflow.hidden
        width = LinearDimension("100%")
        minHeight = LinearDimension("100%")
        height = LinearDimension("100%")
        margin(top = LinearDimension("50%"))
        backgroundColor = Color.transparent
        media(theme.breakpoints.up(tabletAndHighEnd)){
            margin(top = 0.px)
        }
    }

    val clazzItemClass by css {
        height = 200.px
        width = LinearDimension("100%")
    }

    val clazzDetailExtraInfo by css {
        width = LinearDimension("100%")
        margin = "0 2% 0 2%"
        paddingBottom = 10.spacingUnits
    }

    val clazzListRoleChip by css{
        position = Position.absolute
        right = 10.px
        top = 10.px
    }

    val gridListSecondaryItemIcons by css {
        marginTop = 4.px
        fontSize = LinearDimension("1em")
    }

    val gridListSecondaryItemDesc by css {
        marginTop = 4.px
        fontSize = LinearDimension("0.68rem")
    }

    val hideOnMobile by css {
        display = Display.none
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            display = Display.block
        }
    }

    val showOnMobile by css {
        display = Display.block
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            display = Display.none
        }
    }


    val partnerItem by css {
        height = 50.px
        padding(left = 1.spacingUnits)
        verticalAlign = VerticalAlign.middle
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            padding(left = 2.spacingUnits)
            height = 80.px
        }
    }

    val partnersList by css {
        position = Position.fixed
        right = 2.spacingUnits
        bottom = 2.spacingUnits
        width = LinearDimension("100vh")
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            bottom = 4.spacingUnits
            right = 3.spacingUnits
        }
    }

    val studentProgressBar by css{
        width = LinearDimension("50%")
        paddingRight = 2.spacingUnits
        marginTop = 10.px
        media(theme.breakpoints.up(tabletAndHighEnd)) {
            width = LinearDimension("80%")
        }
    }

    override val di: DI
        get() = ReduxAppStateManager.getCurrentState().di.instance

}