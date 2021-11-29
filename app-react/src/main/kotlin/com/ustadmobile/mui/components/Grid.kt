package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.createStyledComponent
import mui.material.Grid
import mui.material.GridProps
import mui.material.GridWrap
import react.RBuilder
import styled.StyledHandler

fun String.toHyphenCase(): String {
var text = ""
    var isFirst = true
    this.forEach {
        if (it in 'A'..'Z') {
            if (!isFirst) text += "-"
            text += it.lowercaseChar()
        } else {
            text += it
        }
        isFirst = false
    }
    return text
}

@Suppress("EnumEntryName")
enum class GridAlignContent {
    stretch,
    center,
    flexStart,
    flexEnd,
    spaceBetween,
    spaceAround;

    override fun toString(): String {
        return super.toString().toHyphenCase()
    }
}

@Suppress("EnumEntryName")
enum class GridAlignItems {
    stretch,
    center,
    flexStart,
    flexEnd,
    baseline;

    override fun toString(): String {
        return super.toString().toHyphenCase()
    }
}

@Suppress("EnumEntryName")
enum class GridJustify {
    flexStart,
    center,
    flexEnd,
    spaceBetween,
    spaceAround;

    override fun toString(): String {
        return super.toString().toHyphenCase()
    }
}

@Suppress("EnumEntryName")
enum class GridSize(internal val sizeVal: Any) {
    columnFalse(false),
    columnAuto("auto"),
    columnTrue(true),
    column1(1),
    column2(2),
    column3(3),
    column4(4),
    column5(5),
    column6(6),
    column7(7),
    column8(8),
    column9(9),
    column10(10),
    column11(11),
    column12(12);
}

enum class GridSpacing(internal val size: Int) {
    spacing0(0),
    spacing1(1),
    spacing2(2),
    spacing3(3),
    spacing4(4),
    spacing5(5),
    spacing6(6),
    spacing7(7),
    spacing8(8),
    spacing9(9),
    spacing10(10)
}


fun RBuilder.gridContainer(
    spacing: GridSpacing = GridSpacing.spacing0,
    alignContent: GridAlignContent = GridAlignContent.stretch,
    alignItems: GridAlignItems = GridAlignItems.stretch,
    justify: GridJustify = GridJustify.flexStart,
    wrap: GridWrap = GridWrap.wrap,
    className: String? = null,
    handler: StyledHandler<GridProps>? = null
) = createStyledComponent(Grid, className, handler) {
    attrs.asDynamic().alignContent = alignContent
    attrs.asDynamic().alignItems = alignItems
    attrs.container = true
    attrs.asDynamic().justify = justify
    attrs.asDynamic().columnSpacing = spacing
    attrs.asDynamic().rowSpacing = spacing
    attrs.wrap = wrap
}

fun RBuilder.gridItem(
    xs: GridSize = GridSize.columnFalse,
    sm: GridSize? = GridSize.columnFalse,
    md: GridSize = GridSize.columnFalse,
    lg: GridSize? = GridSize.columnFalse,
    xl: GridSize = GridSize.columnFalse,
    alignItems: GridAlignItems? = GridAlignItems.flexStart,
    zeroMinWidth: Boolean? = null,
    className: String? = null,
    handler: StyledHandler<GridProps>? = null
) = createStyledComponent(Grid, className, handler) {
    attrs.item = true
    attrs.sm = sm
    attrs.md = md
    attrs.lg = lg
    attrs.xs = xs
    attrs.xl = xl
    sm?.let { attrs.sm = it }
    lg?.let { attrs.lg = it }
    attrs.asDynamic().alignItems = alignItems
    zeroMinWidth?.let { attrs.zeroMinWidth = it }
}