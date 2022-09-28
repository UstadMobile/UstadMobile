package com.ustadmobile.mui.components

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import kotlinx.css.*
import mui.material.Grid
import mui.material.GridProps
import mui.material.GridWrap
import react.RBuilder
import styled.StyledHandler
import styled.css

@Suppress("EnumEntryName")
enum class GridAlignContent {
    stretch,
    center,
    flexStart,
    flexEnd,
    spaceBetween,
    spaceAround;

    override fun toString(): String {
        return super.toString().hyphenize()
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
        return super.toString().hyphenize()
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
        return super.toString().hyphenize()
    }
}

/**
 * The Material Design responsive layout grid adapts to screen size and orientation,
 * ensuring consistency across layouts.
 */
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


/**
 * This is used to control space between grid container children (grid items),
 * It applies on both rowSpacing and columnSpacing properties
 *
 */
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

/**
 * The grid creates visual consistency between layouts
 * while allowing flexibility across a wide variety of designs.
 * Material Design's responsive UI is based on a 12-column grid layout (GridSize cells).
 */
fun RBuilder.gridContainer(
    spacing: GridSpacing = GridSpacing.spacing0,
    alignContent: GridAlignContent = GridAlignContent.stretch,
    alignItems: GridAlignItems = GridAlignItems.stretch,
    direction: FlexDirection = FlexDirection.row,
    wrap: GridWrap = GridWrap.wrap,
    columnSpacing: GridSpacing? = null,
    rowSpacing: GridSpacing? = null,
    className: String? = null,
    handler: StyledHandler<GridProps>? = null
) = convertFunctionalToClassElement(Grid, className, handler) {
    attrs.asDynamic().alignContent = alignContent
    attrs.asDynamic().alignItems = alignItems
    attrs.container = true
    attrs.asDynamic().direction = direction.toString()
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
    display: Display = Display.flex,
    flexDirection: FlexDirection = FlexDirection.column,
    handler: StyledHandler<GridProps>? = null
) = convertFunctionalToClassElement(Grid, className, handler) {
    attrs.item = true
    sm?.let { attrs.asDynamic().sm = it.sizeVal }
    md?.let { attrs.asDynamic().md = it.sizeVal }
    xs?.let { attrs.asDynamic().xs = it.sizeVal }
    xl?.let { attrs.asDynamic().xl = it.sizeVal }
    lg?.let { attrs.asDynamic().lg = it.sizeVal }
    attrs.asDynamic().textAlign = alignItems.toString()
    zeroMinWidth?.let { attrs.zeroMinWidth = it }
    css{
        this.display = display
        this.flexDirection = flexDirection
    }
}