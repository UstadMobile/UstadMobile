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
    cellsFalse(false),
    cellsAuto("auto"),
    cellsTrue(true),
    cells1(1),
    cells2(2),
    cells3(3),
    cells4(4),
    cells5(5),
    cells6(6),
    cells7(7),
    cells8(8),
    cells9(9),
    cells10(10),
    cells11(11),
    cells12(12);
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
    columnSpacing: GridSpacing? = null,
    rowSpacing: GridSpacing? = null,
    className: String? = null,
    handler: StyledHandler<GridProps>? = null
) = createStyledComponent(Grid, className, handler) {
    attrs.asDynamic().alignContent = alignContent
    attrs.asDynamic().alignItems = alignItems
    attrs.container = true
    attrs.asDynamic().justify = justify
    attrs.spacing = spacing.size.asDynamic()
    columnSpacing?.let{
        attrs.columnSpacing = it.size.asDynamic()
    }
    rowSpacing?.let{
        attrs.rowSpacing = it.size.asDynamic()
    }
    attrs.wrap = wrap
}

fun RBuilder.gridItem(
    xs: GridSize? = GridSize.cellsFalse,
    sm: GridSize? = GridSize.cellsFalse,
    md: GridSize? = GridSize.cellsFalse,
    lg: GridSize? = GridSize.cellsFalse,
    xl: GridSize? = GridSize.cellsFalse,
    alignItems: GridAlignItems? = GridAlignItems.flexStart,
    zeroMinWidth: Boolean? = null,
    className: String? = null,
    handler: StyledHandler<GridProps>? = null
) = createStyledComponent(Grid, className, handler) {
    attrs.item = true
    sm?.let { attrs.sm = it.sizeVal }
    md?.let { attrs.md = it.sizeVal }
    xs?.let { attrs.xs = it.sizeVal }
    xl?.let { attrs.xl = it.sizeVal }
    lg?.let { attrs.lg = it.sizeVal }
    attrs.asDynamic().textAlign = alignItems.toString()
    zeroMinWidth?.let { attrs.zeroMinWidth = it }
}