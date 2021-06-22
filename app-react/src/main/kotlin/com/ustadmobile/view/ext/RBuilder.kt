package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.*
import com.ustadmobile.navigation.RouteManager.defaultRoute
import com.ustadmobile.navigation.RouteManager.destinationList
import com.ustadmobile.util.StyleManager.mainComponentErrorPaper
import react.RBuilder
import react.ReactElement
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch
import styled.StyledHandler
import styled.css
import styled.styledDiv

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
            route("/", defaultRoute, exact = true)
            destinationList.forEach {
                route("/${it.view}", it.component, exact = true)
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